import { useState, useEffect, useCallback } from "react";
import { getAccount, getTransactions } from "../api/restBankApi";

/**
 * The transactions endpoint is paged. The donut on the dashboard summarises a
 * period rather than one screen, so callers that chart the data ask for a larger
 * page than the statement does.
 */
export function useAccount(iban, { page = 0, size = 20 } = {}) {

    const [balance, setBalance] = useState(null);
    const [transactions, setTransactions] = useState([]);
    const [pageInfo, setPageInfo] = useState({ number: 0, totalPages: 0, totalElements: 0 });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const load = useCallback(async () => {
        if (!iban) return;
        setLoading(true);
        setError('');
        try {
            const [account, txPage] = await Promise.all([
                getAccount(iban),
                getTransactions(iban, { page, size })
            ]);
            setBalance(account.balance);
            setTransactions(txPage.content ?? []);
            setPageInfo({
                number: txPage.number ?? 0,
                totalPages: txPage.totalPages ?? 0,
                totalElements: txPage.totalElements ?? 0,
            });
         } catch (err) {
            setError(err.message);
         } finally {
            setLoading(false);
         }
    }, [iban, page, size]);

    useEffect(() => { load(); }, [load]);

    return { balance, transactions, pageInfo, loading, error, refresh: load };
}
