import { useState } from 'react';
import { deposit, withdraw } from '../api/restBankApi';
import Button from './Button';
import StatusMessage from './StatusMessage';
import { getErrorMessage } from '../utils/apiError';
import { formatEuro } from '../utils/format';

export default function AtmForm({ iban, onSuccess }) {
    const [atm, setAtm] = useState('');
    const [amount, setAmount] = useState('');
    const [status, setStatus] = useState({ text: '', ok: false });
    const [loading, setLoading] = useState(false);

    async function handleTransaction(type) {
        if (!atm) { setStatus({ text: 'Επιλέξτε ATM', ok: false }); return; }
        if (!amount || Number(amount) <= 0) { setStatus({ text: 'Εισάγετε έγκυρο ποσό', ok: false }); return; }
        setStatus({ text: '', ok: false });
        try {
            setLoading(true);
            const action = type === 'deposit' ? deposit : withdraw;
            const account = await action(iban, 'ATM ' + atm, Number(amount));
            setStatus({
                text: `${type === 'deposit' ? 'Η κατάθεση' : 'Η ανάληψη'} ολοκληρώθηκε — νέο υπόλοιπο: ${formatEuro(account.balance)}`,
                ok: true,
            });
            setAmount('');
            onSuccess?.();
        } catch (err) {
            setStatus({ text: getErrorMessage(err), ok: false });
        } finally {
            setLoading(false);
        }
    }

    return (
        <fieldset className="flex flex-col gap-1 rounded-lg p-4 border border-gray-300">
            <legend className="font-bold text-sm text-[#1f3c88] px-1.5">ATM</legend>
            <label htmlFor="atm-location" className="font-bold text-sm mt-2.5 mb-[3px]">Επιλογή ATM</label>
            <select id="atm-location" value={atm} onChange={e => setAtm(e.target.value)}
                className="p-3 text-base border border-gray-300 rounded h-12 box-border">
                <option value="">-- Επιλέξτε ATM --</option>
                <option value="Σύνταγμα">Σύνταγμα</option>
                <option value="Ομόνοια">Ομόνοια</option>
                <option value="Μοναστηράκι">Μοναστηράκι</option>
            </select>
            <label htmlFor="atm-amount" className="font-bold text-sm mt-2.5 mb-[3px]">Ποσό (€)</label>
            <input id="atm-amount" type="number" value={amount} onChange={e => setAmount(e.target.value)} placeholder="0.00"
                className="p-3 text-base border border-gray-300 rounded h-12 box-border" />
            <div className="flex gap-5 mt-2.5">
                <Button className="flex-1" disabled={loading} onClick={() => handleTransaction('deposit')}>Κατάθεση</Button>
                <Button className="flex-1" disabled={loading} onClick={() => handleTransaction('withdraw')}>Ανάληψη</Button>
            </div>
            <StatusMessage status={status} className="mt-2" />
        </fieldset>
    );
}
