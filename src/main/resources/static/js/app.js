// ClassifiedAds - Main Application JavaScript

// ===========================
// Auth Utilities
// ===========================

function getToken() {
    return localStorage.getItem('jwt_token');
}

function logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_id');
    localStorage.removeItem('user_name');
    localStorage.removeItem('user_roles');
    window.location.href = '/auth/login';
}

function isLoggedIn() {
    return !!getToken();
}

function isAdmin() {
    const roles = JSON.parse(localStorage.getItem('user_roles') || '[]');
    return roles.includes('ADMIN');
}

// ===========================
// Alert Utilities
// ===========================

function showAlert(type, message, duration = 4000) {
    const container = document.getElementById('alertContainer');
    if (!container) return;
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button>`;
    container.appendChild(alert);
    if (duration > 0) {
        setTimeout(() => {
            if (alert.parentNode) alert.remove();
        }, duration);
    }
}

// ===========================
// Image Preview
// ===========================

function previewImages(files, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    container.innerHTML = '';
    Array.from(files).forEach((file, index) => {
        if (!file.type.startsWith('image/')) return;
        const reader = new FileReader();
        reader.onload = function(e) {
            const wrapper = document.createElement('div');
            wrapper.className = 'col-auto image-preview';
            wrapper.innerHTML = `
                <img src="${e.target.result}" alt="Preview">
                <button type="button" class="remove-btn" onclick="removeImagePreview(this, ${index})">
                    <i class="bi bi-x"></i>
                </button>`;
            container.appendChild(wrapper);
        };
        reader.readAsDataURL(file);
    });
}

function removeImagePreview(btn, index) {
    btn.closest('.image-preview').remove();
}

// ===========================
// Ad Actions
// ===========================

async function deleteAd(adId) {
    if (!confirm('Are you sure you want to delete this ad? This cannot be undone.')) return;
    const token = getToken();
    if (!token) { window.location.href = '/auth/login'; return; }
    try {
        const res = await fetch('/api/ads/' + adId, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (res.ok) {
            showAlert('success', 'Ad deleted successfully.');
            setTimeout(() => {
                if (document.referrer.includes('/admin')) {
                    location.reload();
                } else {
                    window.location.href = '/user/dashboard';
                }
            }, 1500);
        } else {
            const err = await res.json();
            showAlert('danger', err.error || 'Failed to delete ad.');
        }
    } catch (e) {
        showAlert('danger', 'Connection error.');
    }
}

async function toggleFavorite(adId, isCurrentlyFavorited) {
    const token = getToken();
    if (!token) { window.location.href = '/auth/login'; return; }
    try {
        const method = isCurrentlyFavorited ? 'DELETE' : 'POST';
        const res = await fetch('/api/favorites/' + adId, {
            method,
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (res.ok || res.status === 204) {
            location.reload();
        } else {
            const err = await res.json();
            showAlert('danger', err.error || 'Failed to update favorites.');
        }
    } catch (e) {
        showAlert('danger', 'Connection error.');
    }
}

// ===========================
// Auth Token Injection for API Calls
// ===========================

// Automatically add JWT to all fetch requests if token exists
const originalFetch = window.fetch;
window.fetch = function(url, options = {}) {
    const token = getToken();
    if (token && typeof url === 'string' && url.startsWith('/api/')) {
        options.headers = options.headers || {};
        if (!options.headers['Authorization']) {
            options.headers['Authorization'] = 'Bearer ' + token;
        }
    }
    return originalFetch(url, options);
};

// ===========================
// Init
// ===========================

document.addEventListener('DOMContentLoaded', function() {
    // Check auth status and update UI
    const token = getToken();
    
    // If on a protected page without token, redirect
    const protectedPaths = ['/user/dashboard', '/user/profile', '/user/favorites', '/admin/'];
    const currentPath = window.location.pathname;
    if (!token && protectedPaths.some(p => currentPath.startsWith(p))) {
        window.location.href = '/auth/login';
    }
});
