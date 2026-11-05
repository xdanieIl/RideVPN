(() => {
  const shell = document.querySelector('.app-shell');
  const toggle = document.querySelector('.theme-toggle');
  const select = document.querySelector('[aria-label="Theme"]');
  const feedback = document.querySelector('.settings-feedback');
  function applyTheme(theme, save = false) {
    const light = theme === 'light';
    shell.dataset.theme = light ? 'light' : 'dark';
    toggle.setAttribute('aria-pressed', String(light));
    const label = light ? 'Switch to dark mode' : 'Switch to light mode';
    toggle.setAttribute('aria-label', label);
    toggle.title = label;
    if (select) select.value = shell.dataset.theme;
    if (save) {
      try {
        localStorage.setItem('ridevpn-theme', shell.dataset.theme);
        if (feedback) feedback.textContent = 'Theme saved in this browser.';
      } catch {
        if (feedback) feedback.textContent = 'Theme applied. Browser storage is unavailable, so it will reset when you leave.';
      }
    }
  }
  let theme = 'dark';
  try { theme = localStorage.getItem('ridevpn-theme') || 'dark'; } catch {}
  applyTheme(theme);
  toggle.addEventListener('click', () => applyTheme(shell.dataset.theme === 'light' ? 'dark' : 'light', true));
  if (select) select.addEventListener('change', () => applyTheme(select.value, true));
})();
