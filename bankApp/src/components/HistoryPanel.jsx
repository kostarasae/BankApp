import { useAccount } from '../hooks/useAccount';
import TransactionTable from './TransactionTable';
import Card from './Card';

export default function HistoryPanel({ iban }) {
    const { transactions, loading, error } = useAccount(iban);

    if (!iban) {
        return (
            <Card>
                <h2 className="card-heading text-lg font-bold text-primary mb-3">Ιστορικό</h2>
                <p className="text-muted">Δεν υπάρχει λογαριασμός για προβολή.</p>
            </Card>
        );
    }
    if (loading) return <Card><p className="text-muted">Φόρτωση...</p></Card>;
    if (error) return <Card><p className="font-bold text-red-500">{error}</p></Card>;

    return (
        <Card>
            <h2 className="card-heading text-lg font-bold text-primary mb-1">Ιστορικό κινήσεων</h2>
            <p className="text-[13px] text-muted tabular-nums mb-4 break-all">{iban}</p>
            <TransactionTable transactions={transactions} />
        </Card>
    );
}
