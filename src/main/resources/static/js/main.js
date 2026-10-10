/**
 * SVJ MART - Frontend Interactions & AI Assistant Client
 */

document.addEventListener('DOMContentLoaded', function () {
  initLiveSearch();
  initAiAssistant();
  initPasswordToggles();
  initNewsletterForm();
  initDemoCredentials();
});

/* -------------------------------------------------------------
 * 1. Live Search Autocomplete
 * ------------------------------------------------------------- */
function initLiveSearch() {
  const searchInput = document.getElementById('globalSearchInput');
  const dropdown = document.getElementById('searchDropdown');

  if (!searchInput || !dropdown) return;

  let debounceTimer = null;

  searchInput.addEventListener('input', function () {
    const query = this.value.trim();
    clearTimeout(debounceTimer);

    if (query.length < 2) {
      dropdown.style.display = 'none';
      dropdown.innerHTML = '';
      return;
    }

    debounceTimer = setTimeout(() => {
      fetch(`/api/search/suggestions?q=${encodeURIComponent(query)}`)
        .then(res => res.json())
        .then(items => {
          if (!items || items.length === 0) {
            dropdown.innerHTML = `<div class="p-3 text-muted text-center small">No matching items found for "${escapeHtml(query)}"</div>`;
            dropdown.style.display = 'block';
            return;
          }

          let html = '';
          items.forEach(p => {
            const img = p.imageUrl || '/images/placeholder-product.svg';
            html += `
              <a href="/product/${p.id}" class="suggestion-item">
                <img src="${img}" class="suggestion-img" onerror="this.src='/images/placeholder-product.svg'" alt="${escapeHtml(p.name)}">
                <div class="flex-grow-1 overflow-hidden">
                  <div class="fw-semibold text-truncate small">${escapeHtml(p.name)}</div>
                  <div class="text-muted extra-small" style="font-size:0.75rem;">${escapeHtml(p.brand)} &bull; ${escapeHtml(p.packageSize)}</div>
                </div>
                <div class="text-end ps-2">
                  <span class="fw-bold text-dark small">&#8377;${p.price}</span>
                </div>
              </a>
            `;
          });
          html += `<div class="p-2 text-center bg-light border-top"><a href="/catalog?keyword=${encodeURIComponent(query)}" class="small fw-semibold text-primary">View all search results &rarr;</a></div>`;

          dropdown.innerHTML = html;
          dropdown.style.display = 'block';
        })
        .catch(err => {
          console.error("Search fetch error:", err);
          dropdown.style.display = 'none';
        });
    }, 280);
  });

  // Close dropdown on click outside
  document.addEventListener('click', function (e) {
    if (!searchInput.contains(e.target) && !dropdown.contains(e.target)) {
      dropdown.style.display = 'none';
    }
  });
}

/* -------------------------------------------------------------
 * 2. AI Shopping Assistant Interactive Drawer
 * ------------------------------------------------------------- */
function initAiAssistant() {
  const form = document.getElementById('aiAssistantForm');
  const input = document.getElementById('aiQueryInput');
  const chatLogs = document.getElementById('aiChatLogs');
  const chips = document.querySelectorAll('.ai-preset-chip');

  if (!form || !input || !chatLogs) return;

  chips.forEach(chip => {
    chip.addEventListener('click', function () {
      const q = this.getAttribute('data-query');
      const b = this.getAttribute('data-budget');
      if (q) {
        input.value = q;
        submitAiQuery(q, null);
      } else if (b) {
        input.value = `Find groceries within budget ₹${b}`;
        submitAiQuery(input.value, parseFloat(b));
      }
    });
  });

  form.addEventListener('submit', function (e) {
    e.preventDefault();
    const query = input.value.trim();
    if (!query) return;

    // Check if user entered a budget number directly like "under 500" or "budget 1000"
    let budget = null;
    const match = query.match(/(?:budget|under|below|for|within)\s*(?:₹|rs\.?|inr)?\s*(\d+)/i);
    if (match && match[1]) {
      budget = parseFloat(match[1]);
    }

    submitAiQuery(query, budget);
  });

  function submitAiQuery(query, budget) {
    // Append user message bubble
    appendMessage('user', query);
    input.value = '';

    // Append loading indicator
    const loadingId = 'loading-' + Date.now();
    appendLoading(loadingId);

    fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        query: query,
        budget: budget,
        dietPreference: 'ANY',
        householdType: 'FAMILY'
      })
    })
      .then(res => res.json())
      .then(data => {
        removeLoading(loadingId);
        renderAiResponse(data);
      })
      .catch(err => {
        removeLoading(loadingId);
        appendMessage('bot', "I apologize, but I had trouble processing your query. Please try asking again!");
      });
  }

  function appendMessage(sender, text) {
    const isUser = sender === 'user';
    const msgDiv = document.createElement('div');
    msgDiv.className = `d-flex mb-3 ${isUser ? 'justify-content-end' : 'justify-content-start'}`;

    msgDiv.innerHTML = `
      <div class="p-3 rounded-3 shadow-sm ${isUser ? 'bg-primary text-white' : 'bg-white border text-dark'}" style="max-width: 85%;">
        <div class="extra-small text-uppercase fw-bold mb-1 opacity-75" style="font-size: 0.7rem;">
          ${isUser ? 'You' : '<i class="bi bi-stars text-info me-1"></i> SVJ AI Assistant'}
        </div>
        <div style="font-size: 0.92rem;">${escapeHtml(text)}</div>
      </div>
    `;
    chatLogs.appendChild(msgDiv);
    chatLogs.scrollTop = chatLogs.scrollHeight;
  }

  function appendLoading(id) {
    const loadDiv = document.createElement('div');
    loadDiv.id = id;
    loadDiv.className = 'd-flex mb-3 justify-content-start';
    loadDiv.innerHTML = `
      <div class="p-3 rounded-3 bg-white border text-muted small d-flex align-items-center gap-2">
        <div class="spinner-border spinner-border-sm text-primary" role="status"></div>
        <span>Analyzing supermarket inventory and computing recommendations...</span>
      </div>
    `;
    chatLogs.appendChild(loadDiv);
    chatLogs.scrollTop = chatLogs.scrollHeight;
  }

  function removeLoading(id) {
    const el = document.getElementById(id);
    if (el) el.remove();
  }

  function renderAiResponse(data) {
    const botDiv = document.createElement('div');
    botDiv.className = 'd-flex mb-3 justify-content-start';

    let productsHtml = '';
    if (data.suggestedProducts && data.suggestedProducts.length > 0) {
      productsHtml = '<div class="row g-2 mt-2">';
      data.suggestedProducts.forEach(p => {
        productsHtml += `
          <div class="col-12 col-md-6">
            <div class="p-2 border rounded-2 bg-light d-flex align-items-center gap-2">
              <img src="${p.imageUrl || '/images/placeholder-product.svg'}" onerror="this.src='/images/placeholder-product.svg'" style="width:44px; height:44px; object-fit:contain; border-radius:4px; background:#fff;" alt="${escapeHtml(p.name)}">
              <div class="flex-grow-1 overflow-hidden">
                <div class="fw-semibold text-truncate small">${escapeHtml(p.name)}</div>
                <div class="text-muted extra-small" style="font-size:0.75rem;">${escapeHtml(p.packageSize)} &bull; <strong class="text-dark">&#8377;${p.price}</strong></div>
              </div>
              <form action="/cart/add" method="POST" class="m-0">
                <input type="hidden" name="productId" value="${p.id}">
                <input type="hidden" name="quantity" value="1">
                <button type="submit" class="btn btn-sm btn-outline-primary py-1 px-2" title="Add to Basket">
                  <i class="bi bi-cart-plus"></i>
                </button>
              </form>
            </div>
          </div>
        `;
      });
      productsHtml += '</div>';

      if (data.totalEstimatedPrice > 0) {
        productsHtml += `
          <div class="d-flex align-items-center justify-content-between mt-3 pt-2 border-top">
            <span class="small fw-semibold text-secondary">Estimated Total: <strong class="text-dark fs-6">&#8377;${data.totalEstimatedPrice}</strong></span>
          </div>
        `;
      }
    }

    botDiv.innerHTML = `
      <div class="p-3 rounded-3 shadow-sm bg-white border text-dark" style="max-width: 95%; width: 100%;">
        <div class="extra-small text-uppercase fw-bold mb-1 text-primary" style="font-size: 0.72rem;">
          <i class="bi bi-stars text-info me-1"></i> SVJ AI Assistant
        </div>
        <div class="mb-2" style="font-size: 0.92rem;">${escapeHtml(data.replyText)}</div>
        ${data.rationale ? `<div class="p-2 rounded bg-info bg-opacity-10 text-primary small mb-2 border border-info border-opacity-25" style="font-size:0.8rem;"><i class="bi bi-info-circle me-1"></i> ${escapeHtml(data.rationale)}</div>` : ''}
        ${productsHtml}
      </div>
    `;
    chatLogs.appendChild(botDiv);
    chatLogs.scrollTop = chatLogs.scrollHeight;
  }
}

/* -------------------------------------------------------------
 * 3. Password Show/Hide Toggle
 * ------------------------------------------------------------- */
function initPasswordToggles() {
  document.querySelectorAll('.password-toggle-btn').forEach(btn => {
    btn.addEventListener('click', function () {
      const targetId = this.getAttribute('data-target');
      const input = document.getElementById(targetId);
      const icon = this.querySelector('i');

      if (input) {
        if (input.type === 'password') {
          input.type = 'text';
          if (icon) {
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
          }
        } else {
          input.type = 'password';
          if (icon) {
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
          }
        }
      }
    });
  });
}

/* -------------------------------------------------------------
 * 4. Newsletter Subscription AJAX
 * ------------------------------------------------------------- */
function initNewsletterForm() {
  const form = document.getElementById('newsletterForm');
  const alertBox = document.getElementById('newsletterAlert');

  if (!form || !alertBox) return;

  form.addEventListener('submit', function (e) {
    e.preventDefault();
    const emailInput = form.querySelector('input[name="email"]');
    const email = emailInput ? emailInput.value.trim() : '';

    if (!email) return;

    fetch('/api/newsletter/subscribe', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `email=${encodeURIComponent(email)}`
    })
      .then(res => res.json())
      .then(data => {
        alertBox.className = `alert alert-${data.success ? 'success' : 'danger'} py-2 mt-2 small`;
        alertBox.innerText = data.message;
        alertBox.classList.remove('d-none');
        if (data.success && emailInput) {
          emailInput.value = '';
        }
      })
      .catch(err => {
        alertBox.className = 'alert alert-danger py-2 mt-2 small';
        alertBox.innerText = 'Unable to subscribe at this moment. Please try again.';
        alertBox.classList.remove('d-none');
      });
  });
}

/* -------------------------------------------------------------
 * 5. Quick-Fill Demo Credentials (For viva & testing!)
 * ------------------------------------------------------------- */
function initDemoCredentials() {
  document.querySelectorAll('.btn-quick-fill').forEach(btn => {
    btn.addEventListener('click', function () {
      const email = this.getAttribute('data-email');
      const password = this.getAttribute('data-pass');

      const emailField = document.getElementById('email') || document.querySelector('input[name="email"]');
      const passField = document.getElementById('password') || document.querySelector('input[name="password"]');

      if (emailField && email) emailField.value = email;
      if (passField && password) passField.value = password;
    });
  });
}

function escapeHtml(text) {
  if (!text) return '';
  return text.replace(/[&<>"']/g, function (m) {
    return {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;'
    }[m];
  });
}
