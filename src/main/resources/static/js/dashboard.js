(() => {
  const map = document.querySelector('.map-daylight');
  const context = map.getContext('2d');
  if (!context) return;
  const radians = Math.PI / 180;

  function drawDaylight() {
    const now = new Date();
    const startOfYear = Date.UTC(now.getUTCFullYear(), 0, 1);
    const days = (now - startOfYear) / 86400000;
    const minutes = now.getUTCHours() * 60 + now.getUTCMinutes();
    const declination = 23.44 * radians * Math.sin(2 * Math.PI * (days - 80) / 365.25);
    const noonLongitude = 180 - minutes / 4;
    const pixels = context.createImageData(map.width, map.height);

    for (let y = 0; y < map.height; y++) {
      const latitude = Math.max(-90, Math.min(90, (610 - y * 938 / map.height) / 4.65)) * radians;
      for (let x = 0; x < map.width; x++) {
        const longitude = x * 360 / map.width - 180;
        const hourAngle = (longitude - noonLongitude) * radians;
        const sunHeight = Math.sin(latitude) * Math.sin(declination)
          + Math.cos(latitude) * Math.cos(declination) * Math.cos(hourAngle);
        const daylight = Math.max(0, Math.min(1, (sunHeight + 0.1) / 0.2));
        const index = (y * map.width + x) * 4;
        pixels.data[index] = pixels.data[index + 1] = pixels.data[index + 2] = 255;
        pixels.data[index + 3] = Math.round(daylight * 210);
      }
    }
    context.putImageData(pixels, 0, 0);
  }

  drawDaylight();
  setInterval(drawDaylight, 60000);
})();

const mapTime = document.querySelector('.map-time');
const virginiaTime = new Intl.DateTimeFormat('en-US', {
  timeZone: 'America/New_York',
  hour: 'numeric',
  minute: '2-digit',
  timeZoneName: 'short'
});

function updateMapTime() {
  const now = new Date();
  mapTime.textContent = virginiaTime.format(now);
  mapTime.dateTime = now.toISOString();
}

updateMapTime();
setInterval(updateMapTime, 1000);

// Connection status bar: asks the server every 5 seconds if this user's VPN is connected
let connectedAt = null;

async function refreshStatus() {
  const start = performance.now();
  const status = await (await fetch('/api/status')).json();
  const ping = Math.round(performance.now() - start);

  // The server stores when the session started, so the timer survives page reloads
  connectedAt = status.connected ? Date.now() - status.sessionSeconds * 1000 : null;

  document.querySelector('.app-shell').dataset.status = status.connected ? 'connected' : 'disconnected';
  document.querySelector('.status-title').textContent = status.connected ? 'Connected' : 'Not connected';
  document.querySelector('.vpn-ip').textContent = status.connected ? status.ip : '—';
  document.querySelector('.ping-value').textContent = status.connected ? ping + ' ms' : '—';
}

function updateSession() {
  const secs = connectedAt ? Math.floor((Date.now() - connectedAt) / 1000) : 0;
  const pad = n => String(n).padStart(2, '0');
  document.querySelector('.session-time').textContent =
    `${pad(Math.floor(secs / 3600))}:${pad(Math.floor(secs / 60) % 60)}:${pad(secs % 60)}`;
}

refreshStatus();
setInterval(refreshStatus, 5000);
setInterval(updateSession, 1000);
