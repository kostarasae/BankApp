import { useCallback, useEffect, useState } from "react";
import { getStaffUsers } from "../api/restBankApi";
import { getErrorMessage } from "../utils/apiError";

export function useStaffUsers(enabled) {
    const [staff, setStaff] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const load = useCallback(async () => {
        if (!enabled) return;
        setLoading(true);
        setError('');
        try {
            setStaff(await getStaffUsers());
        } catch (err) {
            setError(getErrorMessage(err));
        } finally {
            setLoading(false);
        }
    }, [enabled]);

    useEffect(() => { load(); }, [load]);

    return { staff, loading, error, reload: load };
}
