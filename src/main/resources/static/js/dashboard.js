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
