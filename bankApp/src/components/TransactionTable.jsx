import TransactionRow from "./TransactionRow";

export default function TransactionTable ({ transactions }) {
    if (!transactions || transactions.length === 0) {
        return <p className="text-center text-gray-400 py-4">Δεν βρέθηκαν συναλλαγές</p>;
    }

    const columnTitles = ['Ημερομηνία', 'Τύπος', 'Ποσό (€)', 'Περιγραφή'];
    return (
        <table>
            <thead>
                <tr>
                    {columnTitles.map(h => <th key={h} className="border border-gray-300 p-2 text-sm font-bold bg-[#eaf0f0]">{h}</th>)}
                </tr>
            </thead>
            <tbody>
                {transactions.map(t => <TransactionRow key={`${t.timestamp}-${t.type}-${t.amount}`} transaction={t} />)}
            </tbody>
        </table>
    )
}