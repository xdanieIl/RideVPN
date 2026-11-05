(() => {
  const form = document.querySelector('.config-form');
  const button = form.querySelector('button');
  const status = document.querySelector('.status-text');
  form.addEventListener('submit', async event => {
    event.preventDefault();
    button.disabled = true;
    button.textContent = 'Preparing config…';
    status.textContent = 'Generating your WireGuard configuration…';
    try {
      const response = await fetch(form.action, {
        method: 'POST', body: new URLSearchParams(new FormData(form)),
        credentials: 'same-origin', headers: { Accept: 'text/plain' }
      });
      if (response.redirected || response.status === 401 || response.status === 403) {
        status.textContent = 'Your session expired. Sign in again to download a config.';
        return;
      }
      if (!response.ok || !response.headers.get('content-type')?.startsWith('text/plain')) {
        throw new Error('download');
      }
      const config = await response.text();
      if (!config.startsWith('[Interface]')) throw new Error('config');
      const url = URL.createObjectURL(new Blob([config], { type: 'text/plain' }));
      const link = document.createElement('a');
      link.href = url;
      link.download = 'ridevpn.conf';
      document.body.append(link);
      link.click();
      link.remove();
      setTimeout(() => URL.revokeObjectURL(url), 10000);
      const address = config.match(/^Address = ([0-9.]+)\/32$/m);
      if (address) document.querySelector('.vpn-ip').textContent = address[1];
      status.textContent = 'Config downloaded. Import ridevpn.conf into WireGuard and activate it there.';
    } catch {
      status.textContent = 'Could not download a config. Please try again or contact the server administrator.';
    } finally {
      button.disabled = false;
      button.textContent = 'Download config';
    }
  });
})();
