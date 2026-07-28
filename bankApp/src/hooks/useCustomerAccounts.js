import { useEffect, useState } from "react";
import { getCustomerAccounts } from "../api/restBankApi";

export function useCustomerAccounts(customerUuid) {
    const [accounts, setAccounts] = useState([]);
    const [selectedIban, setSelectedIban] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        if (!customerUuid) {
            setLoading(false);
            return;
        }
        async function load() {
            try {
                setLoading(true);
                const fetchedAccounts = await getCustomerAccounts(customerUuid);
                setAccounts(fetchedAccounts);
                setSelectedIban(fetchedAccounts[0]?.iban ?? '');
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        }
        load();
    }, [customerUuid]);

    return { accounts, selectedIban, setSelectedIban, loading, error }
}