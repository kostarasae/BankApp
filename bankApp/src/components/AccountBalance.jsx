import { useEffect, useState } from "react";
import { getAccount } from "../api/restBankApi";

export default function AccountBalance ({ iban }) {

    const [balance, setBalance] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!iban) return;
        setLoading(true);
        getAccount(iban)
        .then(account => setBalance(account.balance))
        .catch(err => setError(err.message))
        .finally(() => setLoading(false));
    }, [iban]);

    if (loading) return <p className="text-gray-400">Φόρτωση...</p>;
    if (error) return <p className="text-red-500">{error}</p>;

    return (
        <p className="text-2xl font-bold text-[#2e7d32] pl-5">
            {balance.toFixed(2)} € 
        </p>
    );
}