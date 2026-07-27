import { useState, useEffect, useCallback } from "react";
import { getAccount, getTransactions } from "../api/restBankApi";

export function useAccount(iban) {

    const [balance, setBalance] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const load = useCallback(async () => {
        if (!iban) return;
        setLoading(true);
        setError('');
        try {
            const [account, txs] = await Promise.all([
                getAccount(iban),
                getTransactions(iban)
            ]);
            setBalance(account.balance);
            setTransactions(txs);
         } catch (err) {
            setError(err.message);
         } finally {
            setLoading(false);
         }
    }, [iban]);

    useEffect(() => { load(); }, [load]);

    return { balance, transactions, loading, error, refresh: load };
}