import { useState } from "react";
import { getAccountByPhone, transfer } from "../api/restBankApi";
import { getErrorMessage } from '../utils/apiError';
import { formatEuro } from '../utils/format';
import StatusMessage from './StatusMessage';
import Button from './Button';
import Card from './Card';

const PHONE_PATTERN = /^\d{10}$/;

const ERROR_OVERRIDES = {
    CustomerNotFound: 'Δεν βρέθηκε λογαριασμός με αυτό το τηλέφωνο.',
    AccountNotFound: 'Δεν βρέθηκε λογαριασμός με αυτό το τηλέφωνο.',
};

export default function IrisForm({ iban, onSuccess }) {
    const [phone, setPhone] = useState('');
    const [amount, setAmount] = useState('');
    const [description, setDescription] = useState('');
    const [status, setStatus] = useState({ text: '', ok: false });
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setStatus({ text: '', ok: false });

        const to = phone.trim();
        if (!PHONE_PATTERN.test(to)) {
            setStatus({ text: 'Το τηλέφωνο πρέπει να έχει 10 ψηφία, χωρίς κωδικό χώρας (π.χ. 6912345678)', ok: false });
            return;
        }
        if (!amount || Number(amount) <= 0) {
            setStatus({ text: 'Εισάγετε ποσό μεγαλύτερο από μηδέν', ok: false });
            return;
        }

        try {
            const owner = await getAccountByPhone(to);
            if (owner.iban === iban) {
                setStatus({ text: 'Αυτό το τηλέφωνο αντιστοιχεί στον δικό σας λογαριασμό', ok: false });
                return;
            }
            const ok = window.confirm(
                `Παραλήπτης: ${owner.firstname} ${owner.lastname}\nΠοσό: ${formatEuro(amount)}\n\nΝα προχωρήσει η μεταφορά;`
            );
            if (!ok) return;
            setLoading(true);
            await transfer(iban, owner.iban, description, Number(amount));
            setStatus({ text: `Επιτυχής μεταφορά ${formatEuro(amount)} — παραλήπτης: ${owner.firstname} ${owner.lastname}`, ok: true });
            setPhone(''); setAmount(''); setDescription('');
            onSuccess?.();
        } catch (err) {
            setStatus({ text: getErrorMessage(err, ERROR_OVERRIDES), ok: false });
        } finally {
            setLoading(false);
        }
    }

    return (
        <Card>
            <h2 className="card-heading mb-3">IRIS</h2>
            <form onSubmit={handleSubmit}>
                <fieldset className="flex flex-col gap-1 rounded-lg p-4 border border-gray-300">
                    <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Αποστολή χρημάτων μέσω IRIS</legend>

                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Αριθμός τηλεφώνου</label>
                    <input type="tel" inputMode="numeric" maxLength={10}
                        value={phone} onChange={e => setPhone(e.target.value)} placeholder="6912345678"
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />

                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Ποσό (€)</label>
                    <input type="number" min="0.01" step="0.01"
                        value={amount} onChange={e => setAmount(e.target.value)} placeholder="0.00"
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />

                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Περιγραφή</label>
                    <input value={description} onChange={e => setDescription(e.target.value)}
                        placeholder="π.χ. ενοίκιο, εστιατόριο"
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />

                    <Button type="submit" disabled={loading} className="mt-2.5 w-full">
                        {loading ? 'Αποστολή...' : 'Αποστολή'}
                    </Button>
                    <StatusMessage status={status} className="mt-2" />
                </fieldset>
            </form>
        </Card>
    );
}
