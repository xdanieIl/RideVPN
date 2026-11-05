const passwordButton = document.querySelector('.password-toggle');
const password = document.querySelector('#password');

passwordButton.addEventListener('click', () => {
  const showPassword = password.type === 'password';
  password.type = showPassword ? 'text' : 'password';
  passwordButton.setAttribute('aria-pressed', String(showPassword));
  passwordButton.setAttribute('aria-label', showPassword ? 'Hide password' : 'Show password');
  passwordButton.querySelector('.eye-open').hidden = showPassword;
  passwordButton.querySelector('.eye-closed').hidden = !showPassword;
});
