// These controls only change the appearance of the page.
const shell = document.querySelector('.app-shell');
const themeButton = document.querySelector('.theme-toggle');
const passwordButton = document.querySelector('.password-toggle');
const password = document.querySelector('#password');

themeButton.addEventListener('click', () => {
  const lightMode = shell.dataset.theme !== 'light';
  shell.dataset.theme = lightMode ? 'light' : 'dark';
  themeButton.setAttribute('aria-pressed', String(lightMode));
  const label = lightMode ? 'Switch to dark mode' : 'Switch to light mode';
  themeButton.setAttribute('aria-label', label);
  themeButton.title = label;
});

passwordButton.addEventListener('click', () => {
  const showPassword = password.type === 'password';
  password.type = showPassword ? 'text' : 'password';
  passwordButton.setAttribute('aria-pressed', String(showPassword));
  passwordButton.setAttribute('aria-label', showPassword ? 'Hide password' : 'Show password');
  passwordButton.querySelector('.eye-open').hidden = showPassword;
  passwordButton.querySelector('.eye-closed').hidden = !showPassword;
});
