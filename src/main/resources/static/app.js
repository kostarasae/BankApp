// Redirect to login if not authenticated
if (!sessionStorage.getItem('token')) {
    window.location.href = 'login.html';
}

const role = sessionStorage.getItem('role');
if (role === 'CUSTOMER') {
    document.querySelector('[data-target="panel-create"]').style.display = 'none';
}

function decodeToken(token) {
    const payload = token.split('.')[1];
    return JSON.parse(atob(payload));
}

document.addEventListener('DOMContentLoaded', async function() {
    const iban = sessionStorage.getItem('iban');
    if (iban) {
        await loadDashboard(iban);
    }
});

// Sidebar toggle
const menuBtn = document.querySelector('.menu-toggle');
const overlay = document.querySelector('.overlay');

menuBtn.addEventListener('click', () => {
    document.body.classList.toggle('menu-open');
});

overlay.addEventListener('click', () => {
    document.body.classList.remove('menu-open');
});


async function loadProfile() {
    const profileContainer = document.querySelector('.profile-info');
    profileContainer.textContent = 'Φόρτωση...';
    const uuid = sessionStorage.getItem('uuid');
    if (!uuid) {
        console.error('No UUID found in sessionStorage');
        profileContainer.textContent = 'Σφάλμα φόρτωσης προφίλ';
        return;
    }
    try {
        const customer = await getCustomer(uuid);
        profileContainer.innerHTML = `
            <p><strong>Όνομα:</strong> ${customer.firstname}</p>
            <p><strong>Επώνυμο:</strong> ${customer.lastname}</p>
            <p><strong>Περιοχή:</strong> ${customer.region}</p>
            <p><strong>ΑΦΜ:</strong> ${customer.vat}</p>
            <p><strong>eMail:</strong> ${customer.email}</p>
            <p><strong>Αριθμός Ταυτότητας:</strong> ${customer.personalInfo.idNumber}</p>
            <p><strong>Τόπος Γέννησης:</strong> ${customer.personalInfo.placeOfBirth}</p>
            <p><strong>Δήμος Καταγωγής:</strong> ${customer.personalInfo.municipalityOfRegistration}</p>
        `;
    } catch (error) {
        console.error('Error loading profile:', error);
        profileContainer.textContent = 'Σφάλμα φόρτωσης προφίλ: ' + error.message;
    }
}


// Panels switch
const menuItems = document.querySelectorAll('.sidebar li');
const panels = document.querySelectorAll('.panel');

function navigateToPanel(targetId) {
    panels.forEach(panel => {
        panel.classList.toggle('active', panel.id === targetId);
    });
    document.body.classList.remove('menu-open');
    if (targetId === 'panel-investments') {
        setTimeout(() => renderSP500(activePeriod), 50);
    }
    if (targetId === 'panel-profile') {
        loadProfile();
    }
}

menuItems.forEach(item => {
    item.addEventListener('click', () => {
        navigateToPanel(item.getAttribute('data-target'));
    });
});

document.querySelectorAll('.card').forEach(card => {
    card.addEventListener('click', () => {
        if (!document.body.classList.contains('menu-open')) return;
        const panel = card.closest('.panel');
        if (!panel) return;
        navigateToPanel(panel.id);
    });
});


// ===== GLOBAL STATE =====
let hoverIndex = -1;
let renderDonut = null;

drawDonut("incomeExpensePie", 1, 1, "Συναλλαγές", true);

// ===== DONUT =====
function drawDonut(canvasId, income, expenses, centerText = "Συναλλαγές", neutral = false) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    const neutralColor = "#c8d0e0";
    const total = neutral ? 2 : (income + expenses);
    const data = neutral
        ? [{ value: 1, color: neutralColor }, { value: 1, color: neutralColor }]
        : [
            { value: expenses, color: "#6e8ff0", label: "Έξοδα" },
            { value: income, color: "#1f3c88", label: "Έσοδα" }
          ];

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const baseRadius = 115;
    const hoverRadius = 125;
    const innerRadius = baseRadius * 0.6;

    const targets = [
        -Math.PI / 2,
        Math.PI / 2
    ];

    const slices = data.map((slice, i) => {
        const angle = (slice.value / total) * Math.PI * 2;
        const center = targets[i];
        return {
            start: center - angle / 2,
            end: center + angle / 2,
            color: slice.color,
            value: slice.value
        };
    });

    function isAngleBetween(a, start, end) {
        if (start <= end) return a >= start && a <= end;
        return a >= start || a <= end;
    }

    function render() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        slices.forEach((slice, i) => {
            const mid = (slice.start + slice.end) / 2;
            const radius = i === hoverIndex ? hoverRadius : baseRadius;

            ctx.beginPath();
            ctx.moveTo(centerX, centerY);
            ctx.arc(centerX, centerY, radius, slice.start, slice.end);
            ctx.closePath();
            ctx.fillStyle = slice.color;
            ctx.fill();

            const tx = centerX + Math.cos(mid) * radius * 0.75;
            const ty = centerY + Math.sin(mid) * radius * 0.75;

            if (!neutral) {
                ctx.fillStyle = "#fff";
                ctx.font = "bold 14px Arial";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                ctx.fillText(slice.value.toFixed(2) + "€", tx, ty);
            }
        });

        // hole
        ctx.beginPath();
        ctx.arc(centerX, centerY, innerRadius, 0, Math.PI * 2);
        ctx.fillStyle = "#fff";
        ctx.fill();

        ctx.fillStyle = "#222";
        ctx.font = "bold 18px Arial";
        ctx.fillText(centerText, centerX, centerY);

        updateLegend(hoverIndex);
    }

    renderDonut = render;
    render();

    if (neutral) return;

    // ===== CANVAS EVENTS =====
    canvas.addEventListener("mousemove", e => {
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left - centerX;
        const y = e.clientY - rect.top - centerY;

        const dist = Math.hypot(x, y);
        if (dist < innerRadius || dist > hoverRadius) {
            if (hoverIndex !== -1) {
                hoverIndex = -1;
                render();
            }
            return;
        }

        const angle = Math.atan2(y, x);
        let found = -1;

        slices.forEach((s, i) => {
            if (isAngleBetween(angle, s.start, s.end)) found = i;
        });

        if (found !== hoverIndex) {
            hoverIndex = found;
            render();
        }
    });

    canvas.addEventListener("mouseleave", () => {
        hoverIndex = -1;
        render();
    });
}

// ===== LEGEND =====
function updateLegend(active) {
    document.querySelectorAll(".legend-item").forEach(el => {
        const i = Number(el.dataset.index);
        el.classList.toggle("active", i === active);
    });
}

document.querySelectorAll(".legend-item").forEach(el => {
    el.addEventListener("mouseenter", () => {
        hoverIndex = Number(el.dataset.index);
        renderDonut();
    });

    el.addEventListener("mouseleave", () => {
        hoverIndex = -1;
        renderDonut();
    });
});


// ===== DASHBOARD =====
const accountSelect = document.getElementById('accountSelect');
accountSelect.addEventListener('change', async function() {
    const iban = accountSelect.value;
    sessionStorage.setItem('iban', iban);
    await loadDashboard(iban);
});


function updateTransactionsTable(transactions) {
    const tbody = document.querySelector("#transactionRows");
    tbody.innerHTML = "";
    if (!transactions || transactions.length === 0) {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td colspan="5" style="text-align:center;color:#888;">Δεν βρέθηκαν συναλλαγές</td>`;
        tbody.appendChild(tr);
        return;
    }
    transactions.forEach(transaction => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
        <td>${transaction.date}</td>
        <td>${transaction.time}</td>
        <td>${transaction.info}</td>
        <td>${transaction.type}</td>
        <td>${transaction.amount > 0 ? '+' : '-'}${transaction.amount.toFixed(2)}</td>`;
        tbody.appendChild(tr);
    });
}


async function loadDashboard(iban) {
    const balance = document.querySelector(".balance-amount");
    try {
        balance.textContent = 'Φόρτωση...';
        accountSelect.innerHTML = '';

        const accounts = JSON.parse(sessionStorage.getItem('ibans') || '[]');
        accounts.forEach(acc => {
            const option = document.createElement('option');
            option.value = acc;
            option.textContent = acc;
            accountSelect.appendChild(option);
        });
        
        accountSelect.value = iban;

        try {
            const account = await getAccount(iban);
            balance.textContent = account.balance.toFixed(2) + " €";
        } catch (e) {
            console.error('Balance load failed:', e.message);
            balance.textContent = 'Σφάλμα στη φόρτωση!';
        }

        let transactions = [];
        try {
            transactions = await getTransactions(iban);
        } catch (e) {
            console.warn('Transactions not available:', e.message);
        }

        let incomeSum = 0;
        let outcomeSum = 0;
        transactions.forEach(transaction => {
            if (transaction.type === "Κατάθεση") {
                incomeSum += transaction.amount;
            } else if (transaction.type === "Ανάληψη") {
                outcomeSum += transaction.amount;
            }
        });

        updateTransactionsTable(transactions);
        drawDonut("incomeExpensePie", incomeSum, outcomeSum, "Συναλλαγές");

    } catch (error) {
        console.error('Dashboard load failed:', error);
        balance.textContent = 'Σφάλμα στη φόρτωση!';
    }
}

document.querySelector('[data-target="panel-dashboard"]').addEventListener('click', async function() {
    const iban = sessionStorage.getItem('iban');
    if (iban) {
        await loadDashboard(iban);
    } else {
        console.error('No IBAN found in sessionStorage');
    }
});


// ===== PAYMENTS =====

const paymentBtn = document.getElementById('paymentBtn');
const transferBtn = document.getElementById('transferBtn');
const depositBtn = document.getElementById('depositBtn');
const withdrawBtn = document.getElementById('withdrawBtn');

paymentBtn.addEventListener('click', () => handlePayment());
transferBtn.addEventListener('click', () => handleTransfer());
depositBtn.addEventListener('click', () => handleAtmTransaction('deposit'));
withdrawBtn.addEventListener('click', () => handleAtmTransaction('withdraw'));

async function handlePayment() {
    const statusContainer = document.querySelector('.payment-status');
    try {
        paymentBtn.disabled = true;
        statusContainer.textContent = '';
        statusContainer.textContent = 'Λειτουργία σε εξέλιξη...';
        const myIban = sessionStorage.getItem('iban');
        const description = document.getElementById('paymentId').value;
        const amountInput = document.getElementById('paymentAmount');
        await withdraw(myIban, description, Number(amountInput.value));
        amountInput.value = '';
    } catch (error) {
        console.error('Payment failed: ' + error.message);
        statusContainer.textContent = 'Η πληρωμή απέτυχε: ' + error.message;
    } finally {
        paymentBtn.disabled = false;
    }
}

async function handleTransfer() {
    const statusContainer = document.querySelector('.transfer-status');
    try {
        transferBtn.disabled = true;
        statusContainer.textContent = '';
        statusContainer.textContent = 'Λειτουργία σε εξέλιξη...';
        const myIban = sessionStorage.getItem('iban');
        const description = document.getElementById('recipientIban').value;
        const amountInput = document.getElementById('transferAmount');
        await withdraw(myIban, description, Number(amountInput.value));
        amountInput.value = '';
    } catch (error) {
        console.error('Transfer failed: ' + error.message);
        statusContainer.textContent = 'Η μεταφορά απέτυχε: ' + error.message;
    } finally {
        transferBtn.disabled = false;
    }
}

async function handleAtmTransaction(type) {
    const statusContainer = document.querySelector('.atm-status');
    statusContainer.textContent = '';
    statusContainer.textContent = 'Λειτουργία σε εξέλιξη...';

    const description = document.getElementById('atm').value;
    const input = document.getElementById('atmAmount');
    const amount = Number(input.value);

    if (!description) { statusContainer.textContent = 'Επιλέξτε ATM'; return; }
    if (!amount || amount <= 0) { statusContainer.textContent = 'Εισάγετε έγκυρο ποσό'; return; }

    depositBtn.disabled = withdrawBtn.disabled = true;

    try {
        const iban = sessionStorage.getItem('iban');
        if (type === 'deposit') {
            await deposit(iban, description, amount);
        } else {
            await withdraw(iban, description, amount);
        }
        input.value = '';

    } catch (error) {
        if (type === 'deposit') {
            console.error('Deposit failed: ' + error.message);
            statusContainer.textContent = 'Η κατάθεση απέτυχε: ' + error.message;
        } else {
            console.error('Withdrawal failed: ' + error.message);
            statusContainer.textContent = 'Η ανάληψη απέτυχε: ' + error.message;
        }
    } finally {
        depositBtn.disabled = withdrawBtn.disabled = false;
    }
}


// ===== S&P 500 CHART =====
let sp500Chart = null;
let activePeriod = '1Y';
const TRADING_DAYS = { '1W': 5, '1M': 22, '3M': 66, '1Y': 252 };

function generateSP500(tradingDays) {
    const endPrice = 5247.32;
    const startPrice = endPrice / Math.pow(1.10, tradingDays / 252);
    const prices = [];
    const labels = [];
    const today = new Date();
    const tradingDates = [];

    for (let i = Math.ceil(tradingDays * 1.5) + 5; i >= 0; i--) {
        const d = new Date(today);
        d.setDate(today.getDate() - i);
        if (d.getDay() !== 0 && d.getDay() !== 6) tradingDates.push(d);
    }
    const days = tradingDates.slice(-tradingDays);

    for (let i = 0; i < tradingDays; i++) {
        const t = i / Math.max(tradingDays - 1, 1);
        const trend = startPrice + (endPrice - startPrice) * t;
        const noise = Math.sin(i * 0.31) * 90 + Math.sin(i * 0.073) * 220 +
                      Math.cos(i * 0.17) * 130 + Math.sin(i * 1.8) * 45;
        prices.push(+(trend + noise).toFixed(2));

        const d = days[i];
        if (!d) { labels.push(''); continue; }
        if (tradingDays <= 5) {
            labels.push(d.toLocaleDateString('el-GR', { weekday: 'short', day: 'numeric' }));
        } else if (tradingDays <= 22) {
            labels.push(d.toLocaleDateString('el-GR', { day: 'numeric', month: 'short' }));
        } else {
            const step = Math.ceil(tradingDays / 8);
            labels.push(i % step === 0 ? d.toLocaleDateString('el-GR', { day: 'numeric', month: 'short' }) : '');
        }
    }
    return { prices, labels };
}

function renderSP500(period) {
    activePeriod = period;
    const { prices, labels } = generateSP500(TRADING_DAYS[period]);
    const first = prices[0];
    const last = prices[prices.length - 1];
    const change = last - first;
    const pct = (change / first) * 100;
    const isUp = change >= 0;
    const color = isUp ? '#2e7d32' : '#c62828';

    document.getElementById('stockPrice').textContent =
        last.toLocaleString('el-GR', { minimumFractionDigits: 2 }) + ' pts';
    const changeEl = document.getElementById('stockChange');
    changeEl.textContent = (isUp ? '+' : '') + change.toFixed(2) +
        ' (' + (isUp ? '+' : '') + pct.toFixed(2) + '%)';
    changeEl.style.color = color;

    if (sp500Chart) sp500Chart.destroy();
    sp500Chart = new Chart(document.getElementById('sp500Chart'), {
        type: 'line',
        data: {
            labels,
            datasets: [{
                data: prices,
                borderColor: color,
                backgroundColor: isUp ? 'rgba(46,125,50,0.08)' : 'rgba(198,40,40,0.08)',
                borderWidth: 2,
                pointRadius: 0,
                fill: true,
                tension: 0.35
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => ctx.parsed.y.toLocaleString('el-GR', { minimumFractionDigits: 2 }) + ' pts'
                    }
                }
            },
            scales: {
                x: { grid: { display: false }, ticks: { maxRotation: 0 } },
                y: {
                    ticks: { callback: v => v.toLocaleString('el-GR', { minimumFractionDigits: 0 }) + ' pts' },
                    grid: { color: 'rgba(0,0,0,0.06)' }
                }
            }
        }
    });
}

document.querySelectorAll('.btn-period').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.btn-period').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        renderSP500(btn.dataset.period);
    });
});


// ===== LOAN CALCULATOR =====
document.getElementById('loanForm').addEventListener('submit', e => {
    e.preventDefault();
    const P = parseFloat(document.getElementById('loanAmount').value);
    const annualRate = parseFloat(document.getElementById('loanRate').value);
    const n = parseInt(document.getElementById('loanMonths').value);
    const r = annualRate / 100 / 12;
    const M = r === 0 ? P / n : P * r * Math.pow(1 + r, n) / (Math.pow(1 + r, n) - 1);
    const total = M * n;
    const interest = total - P;

    document.getElementById('monthlyPayment').textContent = M.toFixed(2) + ' €';
    document.getElementById('totalAmount').textContent = total.toFixed(2) + ' €';
    document.getElementById('totalInterest').textContent = interest.toFixed(2) + ' €';

    const tbody = document.getElementById('amortizationRows');
    tbody.innerHTML = '';
    let balance = P;
    for (let i = 1; i <= n; i++) {
        const iPayment = balance * r;
        const pPayment = M - iPayment;
        balance = Math.max(0, balance - pPayment);
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${i}</td><td>${M.toFixed(2)}</td><td>${pPayment.toFixed(2)}</td><td>${iPayment.toFixed(2)}</td><td>${balance.toFixed(2)}</td>`;
        tbody.appendChild(tr);
    }
    document.getElementById('loanResult').style.display = 'block';
});


// ===== ACCOUNTS =====
document.querySelector('#panel-create form').addEventListener('submit', async function(e) {
    e.preventDefault();

    const customerData = {
        firstname: document.getElementById('ownerName').value,
        lastname:  document.getElementById('ownerSurname').value,
        vat:       document.getElementById('ownerVat').value,
        regionId:  1,   // TODO: replace with real region selector
        userInsertDTO: {
            username: document.getElementById('username').value,
            password: document.getElementById('password').value,
            roleId:   2   // default: regular user
        },
        personalInfoInsertDTO: {
            idNumber:                    document.getElementById('ownerIdNumber').value,
            placeOfBirth:               document.getElementById('ownerPlaceOfBirth').value,
            municipalityOfRegistration: document.getElementById('ownerMunicipalityOfRegistration').value
        }
    };

    await createCustomer(customerData);
});


// ===== SETTINGS =====
const newPasswordForm = document.getElementById('newPasswordForm');
newPasswordForm.addEventListener('submit', async function(e) {
    e.preventDefault();
    const uuid = sessionStorage.getItem('uuid');
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const statusContainer = document.getElementById('settingsStatus');
    const changePasswordBtn = document.getElementById('changePasswordBtn');
    changePasswordBtn.disabled = true;
    statusContainer.textContent = '';
    if (newPassword !== confirmPassword) {
        statusContainer.textContent = 'Οι νέοι κωδικοί δεν ταιριάζουν!';
        console.error('Passwords do not match');
        return;
    }
    try {
        await changePassword(uuid, currentPassword, newPassword);
    } catch (error) {
        console.error('Password change failed: ' + error.message);
        statusContainer.textContent = 'Η αλλαγή κωδικού απέτυχε: ' + error.message;
        return;
    } finally {
        changePasswordBtn.disabled = false;
    }
    statusContainer.textContent = 'Ο κωδικός άλλαξε επιτυχώς!';
    setTimeout(() => { statusContainer.textContent = ''; }, 3000);
    newPasswordForm.reset();
});