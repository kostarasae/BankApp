import { createContext, useContext, useState } from "react";
import { useNavigate } from 'react-router-dom';
import { login as apiLogin, getCustomerAccounts } from '../api/restBankApi';

const AuthContext = createContext(null);

export default function AuthProvider({ children }) {

    const [token,        setToken]        = useState(sessionStorage.getItem('token'));
    const [userUuid,     setUserUuid]     = useState(sessionStorage.getItem('userUuid'));
    const [customerUuid, setCustomerUuid] = useState(sessionStorage.getItem('customerUuid'));
    const [role,         setRole]         = useState(sessionStorage.getItem('role'));
    const [iban,         setIban]         = useState(sessionStorage.getItem('iban'));

    const navigate = useNavigate();

    async function login(username, password) {

        const data = await apiLogin(username, password);
        setToken(data.token);
        setUserUuid(data.userUuid);
        setCustomerUuid(data.customerUuid);
        setRole(data.role);

        const accounts = await getCustomerAccounts(data.customerUuid);
        const firstIban = accounts[0]?.iban ?? null;
        setIban(firstIban);
        if (firstIban) sessionStorage.setItem('iban', firstIban);

        navigate('/');
    };

    async function logout() {
        sessionStorage.clear();
        setToken(null); 
        setUserUuid(null); 
        setCustomerUuid(null); 
        setRole(null); 
        setIban(null);
        navigate('/login');
    }

    return (
        <AuthContext.Provider value={{ token, userUuid, customerUuid, role, iban, login, logout }}>
            {children}
        </AuthContext.Provider>
    )

}

export const useAuth = () => useContext(AuthContext);