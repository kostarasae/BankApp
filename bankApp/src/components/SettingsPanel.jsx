import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { changePassword, resetCustomerPassword } from "../api/restBankApi";
import { getErrorMessage } from "../utils/apiError";
import StatusMessage from "./StatusMessage";
import Button from "./Button";
import Card from "./Card";

const INPUT_CLS = "p-3 text-base border border-gray-300 rounded h-12 box-border";
const LABEL_CLS = "font-bold text-sm mt-2.5 mb-[3px]";

const PASSWORD_PATTERN = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{8,}$/;
const WEAK_MESSAGE = 'Ο νέος κωδικός πρέπει να έχει 8+ χαρακτήρες, με πεζό, κεφαλαίο, αριθμό και ειδικό χαρακτήρα (!@#$%^&+=)';

export default function SettingsPanel({ isAdmin = false, customers = [] }) {
    const { userUuid } = useAuth();
    const [target, setTarget] = useState('self');
    const [customerUuid, setCustomerUuid] = useState('');
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [status, setStatus] = useState({ text: '', ok: false });
    const [loading, setLoading] = useState(false);

    const resetForm = () => {
        setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');
    };

    const forCustomer = isAdmin && target === 'customer';

    async function handleSubmit(e) {
        e.preventDefault();

        if (forCustomer && !customerUuid) {
            setStatus({ text: 'Επιλέξτε πελάτη', ok: false });
            return;
        }
        if (!forCustomer && !currentPassword) {
            setStatus({ text: 'Συμπληρώστε τον τρέχοντα κωδικό', ok: false });
            return;
        }
        if (!PASSWORD_PATTERN.test(newPassword)) {
            setStatus({ text: WEAK_MESSAGE, ok: false });
            return;
        }
        if (newPassword !== confirmPassword) {
            setStatus({ text: 'Οι νέοι κωδικοί δεν ταιριάζουν', ok: false });
            return;
        }
        if (!forCustomer && newPassword === currentPassword) {
            setStatus({ text: 'Ο νέος κωδικός είναι ίδιος με τον τρέχοντα', ok: false });
            return;
        }

        try {
            setLoading(true);
            if (forCustomer) {
                await resetCustomerPassword(customerUuid, newPassword);
                setStatus({ text: 'Ο κωδικός του πελάτη άλλαξε επιτυχώς', ok: true });
            } else {
                await changePassword(userUuid, currentPassword, newPassword);
                setStatus({ text: 'Ο κωδικός άλλαξε επιτυχώς', ok: true });
            }
            resetForm();
            setTimeout(() => setStatus({ text: '', ok: false }), 3000);
        } catch (err) {
            setStatus({ text: getErrorMessage(err), ok: false });
        } finally {
            setLoading(false);
        }
    }

    return (
        <Card>
            <h2 className="card-heading mb-3">Ρυθμίσεις</h2>
            <form onSubmit={handleSubmit}>
                <fieldset className="flex flex-col gap-1 rounded-lg p-4 border border-gray-300">
                    <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Αλλαγή Κωδικού</legend>

                    {isAdmin && (
                        <div className="flex gap-4 mb-2">
                            <label className="flex items-center gap-2 cursor-pointer">
                                <input type="radio" name="target" value="self"
                                    checked={target === 'self'}
                                    onChange={() => { setTarget('self'); setStatus({ text: '', ok: false }); resetForm(); }} />
                                Ο κωδικός μου
                            </label>
                            <label className="flex items-center gap-2 cursor-pointer">
                                <input type="radio" name="target" value="customer"
                                    checked={target === 'customer'}
                                    onChange={() => { setTarget('customer'); setStatus({ text: '', ok: false }); resetForm(); }} />
                                Κωδικός πελάτη
                            </label>
                        </div>
                    )}

                    {forCustomer && (
                        <>
                            <label className={LABEL_CLS}>Πελάτης</label>
                            <select className={INPUT_CLS} value={customerUuid}
                                onChange={e => setCustomerUuid(e.target.value)}>
                                <option value="">— Επιλέξτε πελάτη —</option>
                                {customers.map(c => (
                                    <option key={c.uuid} value={c.uuid}>
                                        {c.firstname} {c.lastname} ({c.username})
                                    </option>
                                ))}
                            </select>
                        </>
                    )}

                    {!forCustomer && (
                        <>
                            <label className={LABEL_CLS}>Τρέχων κωδικός</label>
                            <input type="password" autoComplete="current-password" className={INPUT_CLS}
                                value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} />
                        </>
                    )}

                    <label className={LABEL_CLS}>Νέος κωδικός</label>
                    <input type="password" autoComplete="new-password" className={INPUT_CLS}
                        value={newPassword} onChange={e => setNewPassword(e.target.value)} />

                    <label className={LABEL_CLS}>Επιβεβαίωση νέου κωδικού</label>
                    <input type="password" autoComplete="new-password" className={INPUT_CLS}
                        value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} />

                    <Button type="submit" disabled={loading} className="mt-2.5 w-full">
                        {loading ? 'Αποθήκευση...' : 'Αποθήκευση Κωδικού'}
                    </Button>
                    <StatusMessage status={status} className="mt-2" />
                </fieldset>
            </form>
        </Card>
    );
}
