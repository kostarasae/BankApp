import { useState, useEffect, useCallback } from "react";
import { getCustomer } from "../api/restBankApi";
import { getErrorMessage } from "../utils/apiError";

export function useCustomerProfile(customerUuid) {

    const [profile, setProfile] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    const load = useCallback(async () => {
        // Χωρίς customerUuid (admin/employee) δεν υπάρχει προφίλ να φορτώσει —
        // κατέβασε το loading, αλλιώς το panel μένει σε «Φόρτωση...» για πάντα.
        if (!customerUuid) { setLoading(false); return; }
        setLoading(true); setError('');
        try {
            const data = await getCustomer(customerUuid);
            setProfile(data);
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setLoading(false);
        }
    }, [customerUuid]);
    
    useEffect(() => { load(); }, [load]);

    return { profile, loading, error };
}
