import { useState, useEffect } from 'react';
import { useAuth } from "../context/AuthContext";
import { useAccount } from '../hooks/useAccount';
import { getCustomerAccounts } from "../api/restBankApi";
import BalanceCard from "./BalanceCard";
import TransactionTable from "./TransactionTable";
import IncomeExpenseDonut from "./IncomeExpenseDonut";

export default function Dashboard() {

    const { customerUuid } = useAuth();

    const [accounts, setAccounts] = useState([]);
    const [selectedIban, setSelectedIban] = useState(null);

    useEffect(() => {
        if (!customerUuid) return;
        let ignore = false;
        getCustomerAccounts(customerUuid).then(accounts => {
            if (ignore) return;
            setAccounts(accounts);
            if(!selectedIban && accounts[0]) setSelectedIban(accounts[0].iban);
        })
        return () => { ignore = true; };
    }, [customerUuid]);

    const { balance, transactions, loading, error, refresh } = useAccount(selectedIban);

    return (
        <div className="p-5 flex flex-col gap-4">
            {accounts.length > 1 && (
                <div className="flex flex-col gap-2 w-full max-w-[300px]">
                    <label className="font-bold text-sm">Ενεργός Λογαριασμός</label>
                    <select value={selectedIban ?? ''} onChange={e => setSelectedIban(e.target.value)}
                        className="p-2 border border-gray-300 rounded w-fit">
                        {accounts.map(a => <option key={a.iban} value={a.iban}>{a.iban}</option>)}
                    </select>
                </div>
            )}

            {loading && <p className="text-gray-400">φόρτωση...</p>}
            {error && <p className="text-red-500">{error}</p>}
            
            {!loading && !error && balance != null && (
                <>
                    <IncomeExpenseDonut transactions={transactions} />
                    <section className="card bg-white p-5 rounded-2xl hover:shadow-2xl">
                        <h2 className="card-heading text-lg font-bold text-[#1f3c88] mb-3">Συναλλαγές</h2>
                        <TransactionTable transactions={transactions} />
                    </section>
                    <BalanceCard balance={`${balance.toFixed(2)} €`} />
                </>
            )}
        </div>
    );
}
