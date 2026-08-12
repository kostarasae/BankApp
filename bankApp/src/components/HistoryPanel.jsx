import { useState, useEffect } from 'react';
import { useAccount } from '../hooks/useAccount';
import TransactionTable from './TransactionTable';
import Card from './Card';

const PAGE_SIZE = 20;

export default function HistoryPanel({ iban }) {
    const [page, setPage] = useState(0);
    const { transactions, pageInfo, loading, error } = useAccount(iban, { page, size: PAGE_SIZE });

    // A different account starts its own statement from the beginning
    useEffect(() => { setPage(0); }, [iban]);

    if (!iban) {
        return (
            <Card>
                <h2 className="card-heading mb-3">Ιστορικό</h2>
                <p className="text-muted">Δεν υπάρχει λογαριασμός για προβολή.</p>
            </Card>
        );
    }
    if (loading) return <Card><p className="text-muted">Φόρτωση...</p></Card>;
    if (error) return <Card><p className="font-bold text-red-500">{error}</p></Card>;

    const { number, totalPages, totalElements } = pageInfo;

    return (
        <Card>
            <h2 className="card-heading mb-1">Ιστορικό κινήσεων</h2>
            <p className="text-sm text-muted tabular-nums mb-4 break-all">{iban}</p>
            <TransactionTable transactions={transactions} />

            {totalPages > 1 && (
                <div className="flex items-center justify-between gap-4 mt-4 pt-4 border-t border-hairline">
                    <span className="text-sm text-muted tabular-nums">
                        Σελίδα {number + 1} από {totalPages} — {totalElements} κινήσεις
                    </span>
                    <span className="flex gap-2">
                        <button type="button" disabled={number === 0} onClick={() => setPage(p => p - 1)}
                            className="px-4 py-2 rounded-full border border-primary text-primary font-bold text-sm
                                cursor-pointer transition hover:bg-primary hover:text-white
                                disabled:opacity-40 disabled:cursor-default disabled:hover:bg-white disabled:hover:text-primary">
                            Προηγούμενη
                        </button>
                        <button type="button" disabled={number + 1 >= totalPages} onClick={() => setPage(p => p + 1)}
                            className="px-4 py-2 rounded-full border border-primary text-primary font-bold text-sm
                                cursor-pointer transition hover:bg-primary hover:text-white
                                disabled:opacity-40 disabled:cursor-default disabled:hover:bg-white disabled:hover:text-primary">
                            Επόμενη
                        </button>
                    </span>
                </div>
            )}
        </Card>
    );
}
