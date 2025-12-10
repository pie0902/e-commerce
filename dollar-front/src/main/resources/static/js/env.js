// env.js
// Derive API endpoints based on current host.
// - Local dev: uses localhost ports
// - Remote: uses subdomains of the current base domain (e.g., user.example.com)

(function () {
  const origin = window.location.origin; // e.g., http://localhost:8080 or https://thunderdev.site
  const API_BASE = origin + '/api/';

  window.API = {
    BASE: API_BASE,
    USER: API_BASE + 'user/',
    PRODUCT: API_BASE + 'product/',
    ORDER: API_BASE + 'order/',
    REVIEW: API_BASE + 'review/',
    FRONT: origin + '/',
  };
})();
