import { useAccount } from '../hooks/useAccount';
import AccountCard from './AccountCard';
import IncomeExpenseDonut from "./IncomeExpenseDonut";
import Card from "./Card";
import { formatEuro } from "../utils/format";

export default function Dashboard({ iban, accounts = [], selectedIban, onSelect, onOpenHistory, isAdmin = false, onAccountsChanged }) {
    // The donut summarises a period, not one screen of the statement
    const { transactions, loading, error } = useAccount(iban, { size: 200 });

    if (!iban) {
        return (
            <Card>
                <h2 className="card-heading mb-3">Επισκόπηση</h2>
                <p className="text-muted">Δεν υπάρχει λογαριασμός για προβολή.</p>
            </Card>
        );
    }

    const total = accounts.reduce((sum, a) => sum + Number(a.balance ?? 0), 0);

    return (
        <div className="flex flex-col gap-6">
            <section>
                <div className="flex flex-wrap items-baseline justify-between gap-2 mb-3 px-1">
                    <h2 className="card-heading">Λογαριασμοί</h2>
                    <p className="text-sm text-muted">
                        Συνολικό διαθέσιμο υπόλοιπο{' '}
                        <span className="amount text-2xl font-bold text-ink tabular-nums ml-1">
                            {formatEuro(total)}
                        </span>
                    </p>
                </div>

                <div className="grid gap-4 sm:grid-cols-2">
                    {accounts.map(account => (
                        <AccountCard key={account.iban}
                            account={account}
                            selected={account.iban === selectedIban}
                            onSelect={onSelect}
                            onOpenHistory={onOpenHistory}
                            isAdmin={isAdmin}
                            onClosed={onAccountsChanged} />
                    ))}
                </div>
            </section>

            {loading && <Card className="mb-0"><p className="text-muted">Φόρτωση...</p></Card>}
            {error && <Card className="mb-0"><p className="font-bold text-red-500">{error}</p></Card>}
            {!loading && !error && <IncomeExpenseDonut transactions={transactions} />}
        </div>
    );
}
