import { useCallback, useEffect, useState } from "react";
import { getCustomers } from "../api/restBankApi";
import { getErrorMessage } from "../utils/apiError";

export function useCustomers(enabled) {
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const load = useCallback(async () => {
        if (!enabled) return;
        setLoading(true);
        setError('');
        try {
            setCustomers(await getCustomers());
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setLoading(false);
        }
    }, [enabled]);

    useEffect(() => { load(); }, [load]);

    return { customers, loading, error, reload: load };
}
