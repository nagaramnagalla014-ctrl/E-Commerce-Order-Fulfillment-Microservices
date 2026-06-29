const GW = '/api';
const PRODUCTS = [
  {sku:'LAPTOP-001', name:'Gaming Laptop Pro 15', category:'Electronics', price:1299.99, stock:45},
  {sku:'PHONE-001', name:'Smartphone Ultra X', category:'Electronics', price:899.99, stock:120},
  {sku:'HEADPHONE-001', name:'Wireless Headphones', category:'Electronics', price:249.99, stock:80},
  {sku:'TABLET-001', name:'Professional Tablet 12"', category:'Electronics', price:649.99, stock:35},
  {sku:'WATCH-001', name:'Smart Watch Series 8', category:'Electronics', price:399.99, stock:60},
  {sku:'SHIRT-001', name:'Premium Cotton T-Shirt', category:'Clothing', price:29.99, stock:300},
  {sku:'JEANS-001', name:'Slim Fit Denim Jeans', category:'Clothing', price:59.99, stock:200},
  {sku:'SNEAKER-001', name:'Running Sneakers Pro', category:'Footwear', price:89.99, stock:150},
  {sku:'BOOK-001', name:'Java Microservices in Practice', category:'Books', price:49.99, stock:500},
  {sku:'BACKPACK-001', name:'Laptop Backpack 30L', category:'Accessories', price:79.99, stock:100},
];

async function req(method, path, body) {
  const opts = { method, headers: { 'Content-Type': 'application/json' } };
  if (body) opts.body = JSON.stringify(body);
  const res = await fetch(GW + path, opts);
  const json = await res.json();
  if (!res.ok) throw new Error(json.error || 'Request failed');
  return json;
}

function showAlert(msg, type = 'success') {
  const el = document.getElementById('alertBanner');
  el.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'} mr-2"></i>${msg}`;
  el.className = `alert-${type}-banner`;
  el.classList.remove('d-none');
  setTimeout(() => el.classList.add('d-none'), 4000);
}

function showPage(page) {
  document.querySelectorAll('[id^="page-"]').forEach(el => el.classList.add('d-none'));
  document.getElementById('page-' + page).classList.remove('d-none');
  if (page === 'catalog') renderCatalog();
  if (page === 'orders') loadOrders();
  if (page === 'shipments') loadShipments();
  if (page === 'notifications') loadNotifications();
}

function badge(status) {
  return `<span class="order-status-badge status-${status}">${status}</span>`;
}
function dt(v) { return v ? new Date(v).toLocaleString() : '-'; }
function money(v) { return v != null ? '$' + Number(v).toFixed(2) : '-'; }

// Catalog
function renderCatalog() {
  const el = document.getElementById('catalogGrid');
  el.innerHTML = PRODUCTS.map(p => `
    <div class="col-md-3 mb-4">
      <div class="card product-card shadow-sm h-100">
        <div class="card-body">
          <div class="product-category mb-1">${p.category}</div>
          <h6 class="card-title">${p.name}</h6>
          <div class="d-flex justify-content-between align-items-center mt-3">
            <span class="font-weight-bold text-success">${money(p.price)}</span>
            <span class="badge badge-${p.stock > 50 ? 'success' : p.stock > 10 ? 'warning' : 'danger'} stock-badge">${p.stock} in stock</span>
          </div>
          <div class="text-muted small mt-1">SKU: ${p.sku}</div>
        </div>
        <div class="card-footer bg-white border-top-0">
          <button class="btn btn-outline-primary btn-sm btn-block" onclick="addToNewOrder('${p.sku}','${p.name.replace(/'/g,"\\'")}',${p.price})">
            <i class="fas fa-cart-plus mr-1"></i>Add to Order
          </button>
        </div>
      </div>
    </div>`).join('');
}

function addToNewOrder(sku, name, price) {
  showPage('newOrder');
  setTimeout(() => {
    const rows = document.querySelectorAll('.order-item-row');
    const last = rows[rows.length - 1];
    const skuSel = last.querySelector('.item-sku');
    skuSel.value = sku;
    last.querySelector('.item-name').value = name;
    last.querySelector('.item-price').value = price;
    calcTotal();
  }, 100);
}

// Order form
function fillProductName(select) {
  const opt = select.options[select.selectedIndex];
  const row = select.closest('.order-item-row');
  row.querySelector('.item-name').value = opt.getAttribute('data-name') || '';
  row.querySelector('.item-price').value = opt.getAttribute('data-price') || '';
  calcTotal();
}

function addItem() {
  const container = document.getElementById('orderItemsContainer');
  const div = document.createElement('div');
  div.className = 'order-item-row form-row mb-2';
  div.innerHTML = container.querySelector('.order-item-row').innerHTML;
  div.querySelectorAll('input').forEach(i => i.value = i.type === 'number' ? '1' : '');
  div.querySelector('select').value = '';
  container.appendChild(div);
}

function removeItem(btn) {
  const rows = document.querySelectorAll('.order-item-row');
  if (rows.length > 1) btn.closest('.order-item-row').remove();
  calcTotal();
}

function calcTotal() {
  let total = 0;
  document.querySelectorAll('.order-item-row').forEach(row => {
    const qty = parseFloat(row.querySelector('.item-qty')?.value || 0);
    const price = parseFloat(row.querySelector('.item-price')?.value || 0);
    total += qty * price;
  });
  document.getElementById('orderTotal').textContent = total.toFixed(2);
}

async function submitOrder(e) {
  e.preventDefault();
  const items = [];
  document.querySelectorAll('.order-item-row').forEach(row => {
    const sku = row.querySelector('.item-sku').value;
    if (!sku) return;
    items.push({
      sku,
      productName: row.querySelector('.item-name').value,
      quantity: parseInt(row.querySelector('.item-qty').value),
      unitPrice: parseFloat(row.querySelector('.item-price').value)
    });
  });
  if (!items.length) { showAlert('Add at least one item', 'error'); return; }
  try {
    const order = await req('POST', '/orders', {
      customerId: document.getElementById('custId').value,
      customerEmail: document.getElementById('custEmail').value,
      customerName: document.getElementById('custName').value,
      shippingAddress: document.getElementById('shipAddr').value,
      paymentMethod: document.getElementById('payMethod').value,
      items
    });
    document.getElementById('orderResult').innerHTML = renderOrderCard(order);
    showAlert('Order placed! ID: ' + order.orderId);
  } catch (err) { showAlert(err.message, 'error'); }
}

function renderOrderCard(o) {
  return `<div class="card shadow-sm border-success">
    <div class="card-header bg-success text-white"><b>Order Placed: ${o.orderId}</b></div>
    <div class="card-body small">
      <p><b>Status:</b> ${badge(o.status)}</p>
      <p><b>Customer:</b> ${o.customerName}</p>
      <p><b>Total:</b> ${money(o.totalAmount)}</p>
      <p class="text-muted">The order-created event has been published to Kafka. The inventory service will now check stock availability.</p>
      <button class="btn btn-outline-primary btn-sm" onclick="loadOrderDetail('${o.orderId}')">Refresh Status</button>
    </div>
  </div>`;
}

async function loadOrderDetail(orderId) {
  try {
    const o = await req('GET', `/orders/${orderId}`);
    document.getElementById('orderResult').innerHTML = renderOrderCard(o);
  } catch (err) { showAlert(err.message, 'error'); }
}

// Orders table
async function loadOrders() {
  const customerId = document.getElementById('orderCustomerFilter')?.value;
  try {
    const orders = await req('GET', customerId ? `/orders?customerId=${customerId}` : '/orders');
    const el = document.getElementById('ordersTable');
    if (!orders.length) { el.innerHTML = '<div class="alert alert-info">No orders found.</div>'; return; }
    el.innerHTML = `<div class="table-responsive"><table class="table table-striped table-sm table-hover">
      <thead class="thead-dark"><tr>
        <th>Order ID</th><th>Customer</th><th>Items</th><th>Total</th><th>Status</th><th>Date</th>
      </tr></thead>
      <tbody>${orders.slice(0,100).map(o => `<tr>
        <td><small>${o.orderId}</small></td>
        <td>${o.customerName}<br/><small class="text-muted">${o.customerEmail}</small></td>
        <td>${(o.items||[]).length} items</td>
        <td>${money(o.totalAmount)}</td>
        <td>${badge(o.status)}</td>
        <td>${dt(o.createdAt)}</td>
      </tr>`).join('')}</tbody>
    </table></div>`;
  } catch (err) { showAlert(err.message, 'error'); }
}

// Shipments
async function loadShipments() {
  try {
    const shipments = await req('GET', '/shipments');
    renderShipmentsTable(shipments);
  } catch (err) { showAlert(err.message, 'error'); }
}

async function trackShipment() {
  const tracking = document.getElementById('trackInput').value;
  if (!tracking) { loadShipments(); return; }
  try {
    const s = await req('GET', `/shipments/track/${tracking}`);
    renderShipmentsTable([s]);
  } catch (err) { showAlert(err.message, 'error'); }
}

function renderShipmentsTable(shipments) {
  const el = document.getElementById('shipmentsTable');
  if (!shipments.length) { el.innerHTML = '<div class="alert alert-info">No shipments found.</div>'; return; }
  el.innerHTML = `<div class="table-responsive"><table class="table table-striped table-sm">
    <thead class="thead-dark"><tr>
      <th>Shipment ID</th><th>Order ID</th><th>Tracking</th><th>Carrier</th><th>Status</th>
      <th>Est. Delivery</th><th>Actions</th>
    </tr></thead>
    <tbody>${shipments.map(s => `<tr>
      <td><small>${s.shipmentId}</small></td>
      <td><small>${s.orderId}</small></td>
      <td><b>${s.trackingNumber}</b></td>
      <td>${s.carrier}</td>
      <td>${badge(s.status)}</td>
      <td>${s.estimatedDelivery || '-'}</td>
      <td>
        ${s.status === 'CREATED' ? `<button class="btn btn-warning btn-xs btn-sm" onclick="dispatchShipment('${s.shipmentId}')">Dispatch</button>` : ''}
        ${s.status === 'DISPATCHED' ? `<button class="btn btn-success btn-xs btn-sm" onclick="deliverShipment('${s.shipmentId}')">Mark Delivered</button>` : ''}
      </td>
    </tr>`).join('')}</tbody>
  </table></div>`;
}

async function dispatchShipment(id) {
  try {
    await req('PUT', `/shipments/${id}/dispatch`);
    showAlert('Shipment dispatched!');
    loadShipments();
  } catch (err) { showAlert(err.message, 'error'); }
}

async function deliverShipment(id) {
  try {
    await req('PUT', `/shipments/${id}/deliver`);
    showAlert('Shipment delivered!');
    loadShipments();
  } catch (err) { showAlert(err.message, 'error'); }
}

// Notifications
async function loadNotifications() {
  try {
    const notifications = await req('GET', '/notifications');
    const el = document.getElementById('notificationsTable');
    if (!notifications.length) { el.innerHTML = '<div class="alert alert-info">No notifications yet.</div>'; return; }
    el.innerHTML = `<div class="table-responsive"><table class="table table-striped table-sm">
      <thead class="thead-dark"><tr>
        <th>ID</th><th>Order ID</th><th>Type</th><th>Subject</th><th>Recipient</th><th>Status</th><th>Sent</th>
      </tr></thead>
      <tbody>${notifications.slice(0,100).map(n => `<tr>
        <td>${n.id}</td>
        <td><small>${n.orderId||'-'}</small></td>
        <td><span class="badge badge-info">${n.type}</span></td>
        <td>${n.subject}</td>
        <td>${n.recipientEmail}</td>
        <td><span class="badge badge-${n.status === 'SENT' ? 'success' : 'danger'}">${n.status}</span></td>
        <td>${dt(n.createdAt)}</td>
      </tr>`).join('')}</tbody>
    </table></div>`;
  } catch (err) { showAlert(err.message, 'error'); }
}

// Init
showPage('home');
