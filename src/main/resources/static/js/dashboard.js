(() => {
  const button = document.querySelector('.connect-button');
  const status = document.querySelector('.status-text');
  button.addEventListener('click', () => {
    status.textContent = 'Client download coming soon.';
  });
})();
