import { useState } from 'react';
import { deposit, withdraw } from '../api/restBankApi';
import Button from './Button';
import { getErrorMessage } from '../utils/apiError';

export default function AtmForm({ iban, onSuccess }) {
    const [atm, setAtm] = useState('');
    const [amount, setAmount] = useState('');
    const [status, setStatus] = useState('');
    const [loading, setLoading] = useState(false);

    async function handleTransaction(type) {
        if (!atm) { setStatus('Σφάλμα: Δεν επιλέχθηκε ATM'); return; }
        if (!amount || Number(amount) <= 0) { setStatus('Σφάλμα: Μη έγκυρο ποσό'); return; }
        setStatus('');
        try {
            setLoading(true);
            const action = type === 'deposit' ? deposit : withdraw;
            const account = await action(iban, 'ATM ' + atm, Number(amount));
            setStatus(`${type === 'deposit' ? 'Η κατάθεση' : 'Η ανάληψη'} ολοκληρώθηκε — νέο υπόλοιπο: ${account.balance}€`);
            setAmount('');
            onSuccess?.();
        } catch (err) {
            setStatus('Σφάλμα: ' + getErrorMessage(err));
        } finally {
            setLoading(false);
        }
    }

    return (
        <fieldset className="flex flex-col gap-1 rounded-lg p-4 border border-gray-300">
            <legend className="font-bold text-sm text-[#1f3c88] px-1.5">ATM</legend>
            <label className="font-bold text-sm mt-2.5 mb-[3px]">Επιλογή ATM</label>
            <select value={atm} onChange={e => setAtm(e.target.value)}
                className="p-3 text-base border border-gray-300 rounded h-12 box-border">
                <option value="">-- Επιλέξτε ATM --</option>
                <option value="Σύνταγμα">Σύνταγμα</option>
                <option value="Ομόνοια">Ομόνοια</option>
                <option value="Μοναστηράκι">Μοναστηράκι</option>
            </select>
            <label className="font-bold text-sm mt-2.5 mb-[3px]">Ποσό (€)</label>
            <input type="number" value={amount} onChange={e => setAmount(e.target.value)} placeholder="0.00"
                className="p-3 text-base border border-gray-300 rounded h-12 box-border" />
            <div className="flex gap-5 mt-2.5">
                <Button className="flex-1" disabled={loading} onClick={() => handleTransaction('deposit')}>Κατάθεση</Button>
                <Button className="flex-1" disabled={loading} onClick={() => handleTransaction('withdraw')}>Ανάληψη</Button>
            </div>
            {status && (
                <p className={`mt-2 font-bold ${status.startsWith('Σφάλμα') ? 'text-red-500' : 'text-green-700'}`}>
                    {status}
                </p>
            )}
        </fieldset>
    );
}
