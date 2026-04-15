document.getElementById('loginForm').addEventListener('submit', async function(e) {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorElem = document.getElementById('loginError');

    
    e.preventDefault(); // σταμάτα το default (page reload)
    errorElem.textContent = '';

    if (!username || !password) {
        errorElem.textContent = 'Παρακαλώ συμπληρώστε όλα τα πεδία.';
        return;
    }

    try {
        const data = await login(username, password);
        const accounts = data.customerUuid
            ? await getCustomerAccounts(data.customerUuid)
            : await getAccounts();
        if (accounts && accounts.length > 0) {
            sessionStorage.setItem('ibans', JSON.stringify(accounts.map(a => a.iban)));
            sessionStorage.setItem('iban', accounts[0].iban);
        } else if (data.customerUuid) {
            errorElem.textContent = 'Σφάλμα: δεν βρέθηκε λογαριασμός.';
            return;
        }
        window.location.href = 'index.html';
    } catch (error) {
        if (error.response && error.response.status === 401) {
            errorElem.textContent = 'Λανθασμένο όνομα χρήστη ή κωδικός.';
        } else {
            errorElem.textContent = 'Σφάλμα κατά τη σύνδεση. Παρακαλώ δοκιμάστε ξανά: ' + error.message;
        }
    }
});