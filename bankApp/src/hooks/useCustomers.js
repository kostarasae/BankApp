import { useEffect, useState } from "react";
import { getCustomers } from "../api/restBankApi";
import { getErrorMessage } from "../utils/apiError";

export function useCustomers(enabled) {
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (!enabled) return;
        let ignore = false;
        setLoading(true);
        setError('');
        getCustomers()
            .then(data => { if (!ignore) setCustomers(data); })
            .catch(err => { if (!ignore) setError(getErrorMessage(err)); })
            .finally(() => { if (!ignore) setLoading(false); });
        return () => { ignore = true; };
    }, [enabled]);

    return { customers, loading, error };
}
