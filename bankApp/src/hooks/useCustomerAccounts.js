import { useCallback, useEffect, useState } from "react";
import { getCustomerAccounts } from "../api/restBankApi";

export function useCustomerAccounts(customerUuid) {
    const [accounts, setAccounts] = useState([]);
    const [selectedIban, setSelectedIban] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const load = useCallback(async () => {
        if (!customerUuid) {
            setAccounts([]);
            setSelectedIban('');
            setLoading(false);
            return;
        }
        try {
            setLoading(true);
            const fetchedAccounts = await getCustomerAccounts(customerUuid);
            setAccounts(fetchedAccounts);
            // Keep the current selection when it survived the reload
            setSelectedIban(prev =>
                fetchedAccounts.some(a => a.iban === prev) ? prev : (fetchedAccounts[0]?.iban ?? ''));
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }, [customerUuid]);

    useEffect(() => { load(); }, [load]);

    return { accounts, selectedIban, setSelectedIban, loading, error, reload: load }
}
