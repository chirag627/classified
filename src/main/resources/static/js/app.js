// Oxxlo - Main Application JavaScript

const MAX_UPLOAD_IMAGES = 5;

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
    document.cookie = "JWT_TOKEN=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=Strict";
    showAlert('success', '👋 You have been logged out successfully.');
    setTimeout(() => window.location.href = '/auth/login', 800);
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

function showAlert(type, message, duration = 5000) {
    let container = document.getElementById('alertContainer');
    if (!container) {
        // Create a floating container if none exists
        container = document.createElement('div');
        container.id = 'alertContainer';
        document.body.appendChild(container);
    }
    const alert = document.createElement('div');
    const icons = { success: 'bi-check-circle-fill', danger: 'bi-exclamation-circle-fill', warning: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill' };
    const icon = icons[type] || 'bi-info-circle-fill';
    alert.className = `alert alert-${type} alert-dismissible fade show d-flex align-items-center gap-2`;
    alert.innerHTML = `<i class="bi ${icon}"></i><span>${message}</span><button type="button" class="btn-close ms-auto" data-bs-dismiss="alert" aria-label="Close"></button>`;
    container.appendChild(alert);
    if (duration > 0) {
        setTimeout(() => {
            if (alert.parentNode) {
                alert.classList.remove('show');
                setTimeout(() => { if (alert.parentNode) alert.remove(); }, 300);
            }
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
    const maxFiles = MAX_UPLOAD_IMAGES;
    const validFiles = Array.from(files).filter(f => f.type.startsWith('image/')).slice(0, MAX_UPLOAD_IMAGES);
    if (validFiles.length < files.length) {
        showAlert('warning', `⚠️ Only image files are accepted. Non-image files were skipped.`);
    }
    if (files.length > MAX_UPLOAD_IMAGES) {
        showAlert('info', `ℹ️ Only the first ${MAX_UPLOAD_IMAGES} images will be uploaded.`);
    }
    validFiles.forEach((file, index) => {
        const reader = new FileReader();
        reader.onload = function(e) {
            const wrapper = document.createElement('div');
            wrapper.className = 'col-auto image-preview';
            wrapper.innerHTML = `
                <img src="${e.target.result}" alt="Preview ${index + 1}">
                <button type="button" class="remove-btn" onclick="removeImagePreview(this)" title="Remove image">
                    <i class="bi bi-x"></i>
                </button>`;
            container.appendChild(wrapper);
        };
        reader.readAsDataURL(file);
    });
}

function removeImagePreview(btn) {
    btn.closest('.image-preview').remove();
}

// ===========================
// Ad Actions
// ===========================

async function deleteAd(adId) {
    if (!confirm('🗑️ Are you sure you want to delete this ad?\n\nThis action cannot be undone.')) return;
    const token = getToken();
    if (!token) { showAlert('warning', '⚠️ Please log in to delete ads.'); window.location.href = '/auth/login'; return; }
    try {
        const res = await fetch('/api/ads/' + adId, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (res.ok) {
            showAlert('success', '✅ Ad deleted successfully.');
            setTimeout(() => {
                if (document.referrer && document.referrer.includes('/admin')) {
                    location.reload();
                } else {
                    window.location.href = '/user/dashboard';
                }
            }, 1500);
        } else {
            let errMsg = 'Failed to delete ad.';
            try { const err = await res.json(); errMsg = err.error || errMsg; } catch(e) {}
            if (res.status === 403) errMsg = 'You are not authorized to delete this ad.';
            showAlert('danger', '❌ ' + errMsg);
        }
    } catch (e) {
        showAlert('danger', '⚠️ Connection error. Please check your internet and try again.');
    }
}

async function boostAdPlacement(adId) {
    const token = getToken();
    if (!token) {
        showAlert('warning', '⚠️ Please log in to boost your ad.');
        setTimeout(() => window.location.href = '/auth/login', 1000);
        return;
    }

    const amountInput = prompt('Enter boost amount in ₹', '99');
    if (amountInput === null) return;
    const daysInput = prompt('Enter boost duration in days (1-30)', '7');
    if (daysInput === null) return;

    const amount = parseFloat(amountInput);
    const days = parseInt(daysInput, 10);
    if (Number.isNaN(amount) || amount <= 0 || Number.isNaN(days) || days <= 0 || days > 30) {
        showAlert('warning', '⚠️ Please enter a valid amount and duration.');
        return;
    }

    try {
        const res = await fetch('/api/ads/' + adId + '/boost', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ amount, days })
        });
        if (res.ok) {
            showAlert('success', `🚀 Ad boosted for ${days} day(s). It will now appear at the top.`);
            setTimeout(() => location.reload(), 1200);
        } else {
            let errMsg = 'Failed to boost ad.';
            try { const err = await res.json(); errMsg = err.error || errMsg; } catch(e) {}
            showAlert('danger', '❌ ' + errMsg);
        }
    } catch (e) {
        showAlert('danger', '⚠️ Connection error. Please try again.');
    }
}

async function sendMessageToSeller() {
    const token = getToken();
    if (!token) {
        showAlert('warning', '⚠️ Please log in to send a message.');
        setTimeout(() => window.location.href = '/auth/login', 1000);
        return;
    }

    const contentEl = document.getElementById('adMessageContent');
    if (!contentEl) return;

    const content = contentEl.value.trim();
    if (!content) {
        showAlert('warning', '⚠️ Please enter a message.');
        return;
    }

    const button = document.getElementById('sendAdMessageBtn');
    if (!button) return;

    const receiverId = button.getAttribute('data-seller-id');
    const adId = button.getAttribute('data-ad-id');

    try {
        const response = await fetch('/api/messages', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ receiverId, adId, content })
        });

        if (response.ok) {
            contentEl.value = '';
            showAlert('success', '✅ Message sent to seller. They can read it in their messages.');
            setTimeout(() => window.location.href = '/chat/messages', 800);
        } else {
            let errorText = '❌ Failed to send message.';
            try {
                const errorBody = await response.json();
                errorText = errorBody.error || errorBody.message || errorText;
            } catch (e) {
            }
            showAlert('danger', errorText);
        }
    } catch (e) {
        showAlert('danger', '⚠️ Connection error. Could not send message.');
    }
}

async function toggleFavorite(adId, isCurrentlyFavorited) {
    const token = getToken();
    if (!token) {
        showAlert('info', '💡 Please log in to save ads to your favorites.');
        setTimeout(() => window.location.href = '/auth/login', 1500);
        return;
    }
    try {
        const method = isCurrentlyFavorited ? 'DELETE' : 'POST';
        const res = await fetch('/api/favorites/' + adId, {
            method,
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (res.ok || res.status === 204) {
            showAlert('success', isCurrentlyFavorited ? '💔 Removed from saved ads.' : '❤️ Ad saved to your favorites!');
            setTimeout(() => location.reload(), 1000);
        } else {
            let errMsg = 'Failed to update favorites.';
            try { const err = await res.json(); errMsg = err.error || errMsg; } catch(e) {}
            showAlert('danger', '❌ ' + errMsg);
        }
    } catch (e) {
        showAlert('danger', '⚠️ Connection error. Please try again.');
    }
}

// ===========================
// Auth Token Injection for API Calls
// ===========================

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
    const token = getToken();

    // Redirect unauthenticated users from protected pages
    const protectedPaths = ['/user/dashboard', '/user/profile', '/user/favorites', '/admin/'];
    const currentPath = window.location.pathname;
    if (!token && protectedPaths.some(p => currentPath.startsWith(p))) {
        showAlert('warning', '⚠️ Please log in to access this page.');
        setTimeout(() => window.location.href = '/auth/login', 500);
    }
});
