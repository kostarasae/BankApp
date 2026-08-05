import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { changePassword } from "../api/restBankApi";
import { getErrorMessage } from "../utils/apiError";
import StatusMessage from "./StatusMessage";
import Button from "./Button";
import Card from "./Card";

const INPUT_CLS = "p-3 text-base border border-gray-300 rounded h-12 box-border";
const LABEL_CLS = "font-bold text-sm mt-2.5 mb-[3px]";

// Ίδιος κανόνας με το backend (UserInsertDTO): 8+ χαρακτήρες, 1 πεζό, 1 κεφαλαίο,
// 1 ψηφίο, 1 ειδικός. Ο έλεγχος εδώ είναι για UX — ο server τον ξανακάνει.
const PASSWORD_PATTERN = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{8,}$/;

export default function SettingsPanel() {
    const { userUuid } = useAuth();
    const [currentPassword, setCurrentPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [status, setStatus] = useState({ text: '', ok: false });
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();

        // Client-side: ό,τι μπορούμε να απαντήσουμε χωρίς τον server
        if (!currentPassword) {
            setStatus({ text: 'Συμπληρώστε τον τρέχοντα κωδικό', ok: false });
            return;
        }
        if (!PASSWORD_PATTERN.test(newPassword)) {
            setStatus({ text: 'Ο νέος κωδικός πρέπει να έχει 8+ χαρακτήρες, με πεζό, κεφαλαίο, αριθμό και ειδικό χαρακτήρα (!@#$%^&+=)', ok: false });
            return;
        }
        if (newPassword !== confirmPassword) {
            setStatus({ text: 'Οι νέοι κωδικοί δεν ταιριάζουν', ok: false });
            return;
        }
        if (newPassword === currentPassword) {
            setStatus({ text: 'Ο νέος κωδικός είναι ίδιος με τον τρέχοντα', ok: false });
            return;
        }

        try {
            setLoading(true);
            await changePassword(userUuid, currentPassword, newPassword);
            setStatus({ text: 'Ο κωδικός άλλαξε επιτυχώς', ok: true });
            setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');
            setTimeout(() => setStatus({ text: '', ok: false }), 3000);
        } catch (err) {
            // Χωρίς reset: ο χρήστης μπορεί να θέλει να διορθώσει μόνο ένα πεδίο
            setStatus({ text: getErrorMessage(err), ok: false });
        } finally {
            setLoading(false);
        }
    }

    return (
        <Card>
            <h2 className="card-heading text-lg font-bold text-[#1f3c88] mb-3">Ρυθμίσεις</h2>
            <form onSubmit={handleSubmit}>
                <fieldset className="flex flex-col gap-1 rounded-lg p-4 border border-gray-300">
                    <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Αλλαγή Κωδικού</legend>

                    <label className={LABEL_CLS}>Τρέχων κωδικός</label>
                    <input type="password" autoComplete="current-password" className={INPUT_CLS}
                        value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} />

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
