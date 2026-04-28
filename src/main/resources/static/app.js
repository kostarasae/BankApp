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
    if (document.body.classList.contains('menu-open')) {
        document.body.classList.remove('menu-open');
    } else {
        document.querySelectorAll('.card').forEach(c => c.getBoundingClientRect());
        document.body.classList.add('menu-open');
    }
});

overlay.addEventListener('click', () => {
    document.body.classList.remove('menu-open');
});


// Load profile data
async function loadProfile() {
    const profileContainer = document.querySelector('.profile-info');
    profileContainer.textContent = 'Φόρτωση...';
    const uuid = sessionStorage.getItem('customerUuid');
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
            <p><strong>Email:</strong> ${customer.email}</p>
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
    if (targetId === 'panel-cards') {
        loadCard();
    }
}

async function loadCard() {
    const uuid = sessionStorage.getItem('customerUuid');
    if (!uuid) return;
    try {
        const customer = await getCustomer(uuid);
        document.getElementById('cardHolder').textContent =
            `${customer.firstname} ${customer.lastname}`.toUpperCase();
    } catch (error) {
        console.error('Error loading card holder:', error);
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



// ===== ACCOUNTS =====

document.querySelector('#panel-create form').addEventListener('submit', async function(e) {
    e.preventDefault();

    const customerData = {
        userInsertDTO: {
            username: document.getElementById('username').value,
            password: document.getElementById('password').value,
            roleId:   2   // default: regular user
        },

        firstname:  document.getElementById('ownerName').value,
        lastname:   document.getElementById('ownerSurname').value,
        vat:        document.getElementById('ownerVat').value,
        email:      document.getElementById('ownerEmail').value,
        phone:      document.getElementById('ownerPhone').value,
        regionId:    Number(document.getElementById('ownerRegion').value),

        personalInfoInsertDTO: {
            idNumber:                   document.getElementById('ownerIdNumber').value,
            placeOfBirth:               document.getElementById('ownerPlaceOfBirth').value,
            dateOfBirth:                document.getElementById('ownerDob').value,
            municipalityOfRegistration: document.getElementById('ownerMunicipalityOfRegistration').value,
            homeAddress:                document.getElementById('ownerAddress').value,
            gender:                     document.querySelector('input[name="gender"]:checked')?.value,
        }
    };

    if (document.getElementById('checkboxTerms').checked === false) {
        document.getElementById('accountCreationStatus').textContent = 'Παρακαλώ αποδεχτείτε τους όρους και προϋποθέσεις';
        return;
    }

    try {
        const customer = await createCustomer(customerData);
        
        await uploadIdFile(customer.uuid, document.getElementById('ownerIdFile').files[0]);

        const accountData = {
            accountType: document.getElementById('accountType').value,
            initialDeposit: Number(document.getElementById('initialDeposit').value),
            customerUuid: customer.uuid
        };

        await createAccount(accountData);

    } catch (error) {
        console.error('Account creation failed:', error);
        document.getElementById('accountCreationStatus').textContent = 'Η δημιουργία λογαριασμού απέτυχε: ' + error.message;
        return;
    }
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
    const typeLabel = { DEPOSIT: 'Κατάθεση', WITHDRAWAL: 'Ανάληψη', TRANSFER: 'Μεταφορά' };
    transactions.forEach(transaction => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
        <td>${new Date(transaction.timestamp).toLocaleString('el-GR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })}</td>
        <td>${typeLabel[transaction.type] ?? transaction.type}</td>
        <td>${transaction.type === 'DEPOSIT' ? '+' : '-'}${transaction.amount.toFixed(2)}</td>
        <td>${transaction.description}</td>`;
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
            if (transaction.type === "DEPOSIT") {
                incomeSum += transaction.amount;
            } else if (transaction.type === "WITHDRAWAL" || transaction.type === "TRANSFER") {
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
    const myIban = sessionStorage.getItem('iban');
    const select = document.getElementById('provider');
    const provider = select.options[select.selectedIndex].text;
    const toPaymentIdContainer = document.getElementById('paymentId');
    const amountInput = document.getElementById('paymentAmount');
    const amount = Number(amountInput.value);
    
    statusContainer.textContent = 'Λειτουργία σε εξέλιξη...';

    if (!toPaymentIdContainer.value) { statusContainer.textContent = 'Εισάγετε ID παραλήπτη'; return; }
    if (!amount || amount <= 0) { statusContainer.textContent = 'Εισάγετε έγκυρο ποσό'; return; }

    try {
        paymentBtn.disabled = true;
        const fee = await getAccountFee(myIban);
        if (!confirm(`Θα χρεωθείτε με προμήθεια ${fee}€. Θέλετε να συνεχίσετε;`)) {
            statusContainer.textContent = '';
            return;
        }
        await withdraw(myIban, `Λογαριασμός ${provider}: ${toPaymentIdContainer.value}`, amount);

        statusContainer.textContent = 'Η πληρωμή ολοκληρώθηκε';
        select.selectedIndex = 0;
        toPaymentIdContainer.value = '';
        amountInput.value = '';

        loadDashboard(myIban);

    } catch (error) {
        console.error('Payment failed: ' + error.message);
        statusContainer.textContent = 'Η πληρωμή απέτυχε: ' + error.message;

    } finally {
        paymentBtn.disabled = false;
    }
}


async function handleTransfer() {
    const statusContainer = document.querySelector('.transfer-status');
    const myIban = sessionStorage.getItem('iban');
    const toIbanContainer = document.getElementById('recipientIban');
    const descriptionContainer = document.getElementById('transferDescription');
    const amountInput = document.getElementById('transferAmount');
    
    statusContainer.textContent = 'Λειτουργία σε εξέλιξη...';

    if (!toIbanContainer.value) { statusContainer.textContent = 'Εισάγετε IBAN παραλήπτη'; return; }
    if (!amountInput || Number(amountInput.value) <= 0) { statusContainer.textContent = 'Εισάγετε έγκυρο ποσό'; return; }

    try {
        transferBtn.disabled = true;
        const fee = await getAccountFee(myIban);
        if (!confirm(`Θα χρεωθείτε με προμήθεια ${fee}€. Θέλετε να συνεχίσετε;`)) {
            statusContainer.textContent = '';
            return;
        }
        const owner = await getAccountOwner(toIbanContainer.value);
        const recipientName = `${owner.firstname} ${owner.lastname}`;
        const transferDescription = descriptionContainer.value
            ? `${descriptionContainer.value} σε ${recipientName}`
            : `σε ${recipientName}`;
        await transfer(myIban, toIbanContainer.value, transferDescription, Number(amountInput.value));

        statusContainer.textContent = 'Η μεταφορά ολοκληρώθηκε';
        amountInput.value = '';
        toIbanContainer.value = '';
        descriptionContainer.value = '';

        loadDashboard(myIban);

    } catch (error) {
        console.error('Transfer failed: ' + error.message);
        statusContainer.textContent = 'Η μεταφορά απέτυχε: ' + error.message;

    } finally {
        transferBtn.disabled = false;
    }
}


async function handleAtmTransaction(type) {
    const statusContainer = document.querySelector('.atm-status');
    const myIban = sessionStorage.getItem('iban');
    const select = document.getElementById('atm');
    const description = select.options[select.selectedIndex].text;
    const amountInput = document.getElementById('atmAmount');
    const amount = Number(amountInput.value);
    
    statusContainer.textContent = 'Λειτουργία σε εξέλιξη...';

    if (!description) { statusContainer.textContent = 'Επιλέξτε ATM'; return; }
    if (!amount || amount <= 0) { statusContainer.textContent = 'Εισάγετε έγκυρο ποσό'; return; }

    try {
        depositBtn.disabled = withdrawBtn.disabled = true;

        if (type === 'deposit') {
            await deposit(myIban, description, amount);
            statusContainer.textContent = 'Η κατάθεση ολοκληρώθηκε';
        } else {
            const fee = await getAccountFee(myIban);
            if (!confirm(`Θα χρεωθείτε με προμήθεια ${fee}€. Θέλετε να συνεχίσετε;`)) {
                statusContainer.textContent = '';
                return;
            }
            await withdraw(myIban, `ATM ${description}`, amount);
            statusContainer.textContent = 'Η ανάληψη ολοκληρώθηκε';
        }

        select.selectedIndex = 0;
        amountInput.value = '';

        loadDashboard(myIban);

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


// ===== IRIS =====

document.getElementById('irisForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    const myiban = sessionStorage.getItem('iban');
    const phone = document.getElementById('irisPhone').value;
    const amount = parseFloat(document.getElementById('irisAmount').value);
    const description = document.getElementById('irisDescription').value;
    if (!phone) {
        document.getElementById('iris-status').textContent = "Εισάγετε αριθμό τηλεφώνου παραλήπτη";
        console.error('Απαιτείται αριθμός τηλεφώνου IRIS');
        return;
    }
    if (!amount || amount <= 0) {
        document.getElementById('iris-status').textContent = "Εισάγετε έγκυρο ποσό μεταφοράς";
        console.error('Μη έγκυρο ποσό IRIS');
        return;
    }
    try {
        document.getElementById('irisBtn').disabled = true;
        const recipient = await getAccountByPhone(phone);
        if (!confirm(`Αποστολή ${amount}€ σε ${recipient.firstname} ${recipient.lastname};`)) {
            document.getElementById('iris-status').textContent = '';
            return;
        }
        const irisDescription = `σε ${recipient.firstname} ${recipient.lastname}${description ? ': ' + description : ''}`;
        await transfer(myiban, recipient.iban, irisDescription, amount);
        document.getElementById('iris-status').textContent = 'Η μεταφορά ολοκληρώθηκε επιτυχώς';
        setTimeout(() => loadDashboard(myiban), 2000);
    } catch (error) {
        document.getElementById('iris-status').textContent = "Η μεταφορά απέτυχε. Ελέγξτε τον αριθμό τηλεφώνου και το υπόλοιπό σας.";
        console.error('Αποτυχία IRIS:', error);
    } finally {
        document.getElementById('irisBtn').disabled = false;
    }
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



// ===== SETTINGS =====
const newPasswordForm = document.getElementById('newPasswordForm');
newPasswordForm.addEventListener('submit', async function(e) {
    e.preventDefault();
    const uuid = sessionStorage.getItem('userUuid');
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

document.querySelector('[data-target="panel-logout"]').addEventListener('click', function() {
    sessionStorage.clear();
    window.location.href = 'login.html';
});