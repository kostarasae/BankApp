import { useState } from 'react';
import { deposit, withdraw } from '../api/restBankApi';
import Button from './Button';

export default function AtmForm({ iban, onSuccess }) {
    const [atm, setAtm] = useState('');
    const [amount, setAmount] = useState('');
    const [error, setError] = useState('');

    async function handleTransaction(type) {
        if (!atm) { setError('Επιλέξτε ATM'); return; }
        if (!amount || Number(amount) <= 0) { setError('Εισάγετε έγκυρο ποσό'); return; }
        setError('');
        const action = type === 'deposit' ? deposit : withdraw;
        await action(iban, 'ATM ' + atm, Number(amount));
        onSuccess?.();
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
            {error && <p className="text-red-500">{error}</p>}
            <div className="flex gap-5 mt-2.5">
                <Button onClick={() => handleTransaction('deposit')}>Κατάθεση</Button>
                <Button onClick={() => handleTransaction('withdraw')}>Ανάληψη</Button>
            </div>
        </fieldset>
    );
}
