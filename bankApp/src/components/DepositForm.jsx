import { useState } from "react";

export default function DepositForm ({ iban }) {
    const [amount, setAmount] = useState('');
    const [atm, setAtm] = useState('');
    const [error, setError] = useState('');
    
    function handleDeposit() {
        if (!amount || Number(amount) <= 0) {
            setError('Εισάγετε ένα έγκυρο ποσό κατάθεσης.');
            return;
        }
        setError('');
        console.log({ iban, atm, amount: Number(amount) });
    }

    return (
        <div className="flex flex-col gap-3">
            <select value={atm} onChange={e => setAtm(e.target.value)}>
                <option value="">-- Επιλέξτε ATM --</option>
                <option value="Σύνταγμα">Σύνταγμα</option>
                <option value="Ομόνοια">Ομόνοια</option>
                <option value="Μοναστηράκι">Μοναστηράκι</option>
            </select>
            <input type="number" 
                value={amount} 
                onChange={e => setAmount(e.target.value)} 
                placeholder="0.00"
                className="p-3 border border-gray-300 rounded h-[50px] w-full"/>
            {error && <p className="text-red-500">{error}</p>}
            <button onClick={handleDeposit} 
            className="p-2.5 bg-[#1f3c88] text-white font-bold rounded cursor-pointer hover:opacity-90">
                Κατάθεση</button>
        </div>
    );
}