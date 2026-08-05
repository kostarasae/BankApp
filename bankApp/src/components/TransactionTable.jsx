import TransactionRow from "./TransactionRow";

export default function TransactionTable ({ transactions }) {
    if (!transactions || transactions.length === 0) {
        return <p className="text-center text-gray-400 py-4">Δεν βρέθηκαν συναλλαγές</p>;
    }

    // Το ποσό στοιχίζεται δεξιά (και η κεφαλίδα του μαζί) — έτσι οι μονάδες, οι
    // δεκάδες και οι εκατοντάδες πέφτουν σε κοινή κατακόρυφο και τα ποσά
    // συγκρίνονται με τη ματιά, όπως σε κάθε extrait τράπεζας.
    const columns = [
        { title: 'Ημερομηνία', align: 'text-left' },
        { title: 'Τύπος',      align: 'text-left' },
        { title: 'Ποσό',       align: 'text-right' },
        { title: 'Περιγραφή',  align: 'text-left' },
    ];
    return (
        <table className="w-full">
            <thead>
                <tr>
                    {columns.map(c => (
                        <th key={c.title}
                            className={`border border-gray-300 p-2 text-sm font-bold bg-[#eaf0f0] ${c.align}`}>
                            {c.title}
                        </th>
                    ))}
                </tr>
            </thead>
            <tbody>
                {transactions.map(t => <TransactionRow key={`${t.timestamp}-${t.type}-${t.amount}`} transaction={t} />)}
            </tbody>
        </table>
    )
}