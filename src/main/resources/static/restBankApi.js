const BASE_URL = 'http://localhost:8080/api/v1';


// Authentication
axios.interceptors.request.use(config => {
    const token = sessionStorage.getItem('token');
    console.log('INTERCEPTOR url=' + config.url + ' dots=' + (token ? token.split('.').length - 1 : 'NO_TOKEN'));
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response?.status === 401 && sessionStorage.getItem('token')) {
            sessionStorage.removeItem('token');
            window.location.href = 'login.html';
        }
        return Promise.reject(error);
    }
);

async function login(username, password) {
    try {
        const response = await axios.post(`${BASE_URL}/auth/authenticate`, {username: username, password: password});
        sessionStorage.setItem('token', response.data.token);
        sessionStorage.setItem('userUuid', response.data.userUuid);
        sessionStorage.setItem('customerUuid', response.data.customerUuid);
        sessionStorage.setItem('role', response.data.role);
        return response.data;
    } catch (error) {
        console.error('Error login user:', error);
        throw error;
    }
}


// CUSTOMERS
async function createCustomer(customerData) {
    try {
        const response = await axios.post(`${BASE_URL}/customers`, customerData);
        return response.data;
    } catch (error) {
        console.error('Error creating customer:', error);
        throw error;
    }
}

async function getCustomer(uuid) {
    try {
        const response = await axios.get(`${BASE_URL}/customers/${uuid}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching customer:', error);
        throw error;
    }
}

async function getCustomerAccounts(uuid) {
    try {
        const response = await axios.get(`${BASE_URL}/customers/${uuid}/accounts`);
        return response.data;
    } catch (error) {
        console.error('Error fetching customer accounts:', error);
        throw error;
    }
}


// ACCOUNTS
async function getAccounts() {
    try {
        const response = await axios.get(`${BASE_URL}/accounts`);
        return response.data;
    } catch (error) {
        console.error('Error fetching accounts:', error);
        throw error;
    }
}

async function getAccount(iban) {
    try {
        const response = await axios.get(`${BASE_URL}/accounts/${iban}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching account:', error);
        throw error;
    }
}

async function createAccount(accountData) {
    try {
        const response = await axios.post(`${BASE_URL}/accounts`, accountData);
        return response.data;
    } catch (error) {
        console.error('Error creating account:', error);
        throw error;
    }
}

async function closeAccount(iban) {
    try {
        const response = await axios.delete(`${BASE_URL}/accounts/${iban}`);
        return response.data;
    } catch (error) {
        console.error('Error deleting account:', error);
        throw error;
    }
}

async function uploadIdFile(uuid, file) {
    try {
        const formData = new FormData();
        formData.append('idFile', file);
        const response = await axios.post(`${BASE_URL}/customers/${uuid}/id-file`, formData);
        return response.data;
    } catch (error) {
        console.error('Error uploading ID file:', error);
        throw error;
    }
}

async function getTransactions(iban) {
    try {
        const response = await axios.get(`${BASE_URL}/accounts/${iban}/transactions`);
        return response.data;
    } catch (error) {
        console.error('Error fetching transactions:', error);
        throw error;
    }
}

async function deposit(iban, description, amount) {
    try {
        const response = await axios.post(`${BASE_URL}/accounts/deposit`, {iban: iban, description: 'ATM ' + description, amount: amount});
        return response.data;
    } catch (error) {
        console.error('Error posting deposit:', error);
        throw error;
    }
}

async function withdraw(myIban, description, amount) {
    try {
        const response = await axios.post(`${BASE_URL}/accounts/withdraw`, {iban: myIban, description: description, amount: amount});
        return response.data;
    } catch (error) {
        console.error('Error posting withdraw:', error);
        throw error;
    }
}

async function getAccountFee(iban) {
    try {
        const response = await axios.get(`${BASE_URL}/accounts/${iban}/fee`);
        return response.data.fee; // BigDecimal number
    } catch (error) {
        console.error('Error fetching account fee:', error);
        throw error;
    }
}

async function getAccountOwner(iban) {
    try {
        const response = await axios.get(`${BASE_URL}/accounts/${iban}/owner`);
        return response.data; // { iban, firstname, lastname }
    } catch (error) {
        console.error('Error fetching account owner:', error);
        throw error;
    }
}

async function transfer(myIban, toIban, description, amount) {
    try {
        const response = await axios.post(`${BASE_URL}/accounts/transfer`, {myIban: myIban, toIban: toIban, description: description, amount: amount});
        return response.data;
    } catch (error) {
        console.error('Error posting transfer:', error);
        throw error;
    }
}

async function getAccountByPhone(phone) {
    try {
        const response = await axios.get(`${BASE_URL}/accounts/phone/${phone}`);
        return response.data; // { iban, firstname, lastname }
    } catch (error) {
        console.error('Error fetching account by phone:', error);
        throw error;
    }
}


// USERS
async function changePassword(uuid, currentPassword, newPassword) {
    try {
        const response = await axios.put(`${BASE_URL}/users/${uuid}/password`, {currentPassword: currentPassword, newPassword: newPassword});
        return response.data;
    } catch (error) {
        console.error('Error changing password:', error);
        throw error;
    }
}
