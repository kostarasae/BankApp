import { useState } from 'react';
import { deleteUser, resetUserPassword } from '../api/restBankApi';
import { getErrorMessage } from '../utils/apiError';
import Card from './Card';
import Button from './Button';
import StatusMessage from './StatusMessage';
import DangerZone from './DangerZone';

const ROLE_LABELS = { ADMIN: 'Διαχειριστής', EMPLOYEE: 'Υπάλληλος' };
const PASSWORD_PATTERN = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{8,}$/;

function Row({ label, value }) {
    return (
        <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 py-2.5
            border-b border-gray-200 last:border-b-0">
            <span className="font-bold">{label}</span>
            <span>{value || '—'}</span>
        </div>
    );
}

/**
 * A staff member has no customer record — no VAT, no region, no accounts — so their
 * profile shows what actually exists for them plus the actions an admin needs.
 */
export default function StaffProfile({ user, isAdmin = false, onDeleted, onChanged }) {
    const [newPassword, setNewPassword] = useState('');
    const [saving, setSaving] = useState(false);
    const [status, setStatus] = useState({ text: '', ok: false });

    async function handleReset() {
        if (!PASSWORD_PATTERN.test(newPassword)) {
            setStatus({ text: 'Ο κωδικός πρέπει να έχει τουλάχιστον 8 χαρακτήρες με 1 πεζό, 1 κεφαλαίο, 1 ψηφίο και 1 ειδικό χαρακτήρα', ok: false });
            return;
        }
        setSaving(true);
        setStatus({ text: '', ok: false });
        try {
            await resetUserPassword(user.uuid, newPassword);
            setNewPassword('');
            setStatus({ text: 'Ο κωδικός άλλαξε', ok: true });
            onChanged?.();
        } catch (err) {
            setStatus({ text: getErrorMessage(err), ok: false });
        } finally {
            setSaving(false);
        }
    }

    return (
        <Card>
            <h2 className="card-heading mb-3">Προφίλ προσωπικού</h2>

            <fieldset className="flex flex-col rounded-lg p-4 border border-gray-300 mb-4">
                <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Στοιχεία χρήστη</legend>
                <Row label="Όνομα χρήστη" value={user.username} />
                <Row label="Ρόλος" value={ROLE_LABELS[user.role] ?? user.role} />
            </fieldset>

            <p className="text-sm text-muted">
                Ο λογαριασμός προσωπικού δεν αντιστοιχεί σε πελάτη, οπότε δεν έχει ΑΦΜ,
                προσωπικά στοιχεία ή τραπεζικούς λογαριασμούς.
            </p>

            {isAdmin && (
                <>
                    <fieldset className="flex flex-col rounded-lg p-4 border border-gray-300 mt-4">
                        <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Αλλαγή κωδικού</legend>
                        <label className="font-bold text-sm mt-2.5 mb-[3px]">Νέος κωδικός</label>
                        <input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)}
                            className="p-3 text-base border border-gray-300 rounded h-12 box-border" />
                        <Button onClick={handleReset} disabled={saving} className="mt-2.5 self-start">
                            {saving ? 'Αλλαγή...' : 'Αλλαγή κωδικού'}
                        </Button>
                        <StatusMessage status={status} className="mt-2" />
                    </fieldset>

                    <DangerZone
                        title="Διαγραφή χρήστη"
                        description={`Ο λογαριασμός ${user.username} θα πάψει να έχει πρόσβαση στην εφαρμογή.`}
                        confirmLabel="Για επιβεβαίωση γράψε το όνομα χρήστη:"
                        confirmValue={user.username}
                        actionLabel="Διαγραφή χρήστη"
                        errorOverrides={{
                            UserInvalidArgument: 'Η διαγραφή δεν επιτρέπεται: δεν μπορείτε να διαγράψετε τον εαυτό σας, τον τελευταίο διαχειριστή, ή χρήστη που αντιστοιχεί σε πελάτη.',
                        }}
                        onConfirm={async () => {
                            await deleteUser(user.uuid);
                            onDeleted?.();
                        }}
                    />
                </>
            )}
        </Card>
    );
}
