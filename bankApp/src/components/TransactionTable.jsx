import TransactionRow from "./TransactionRow";

const DAY_FORMAT = new Intl.DateTimeFormat('el-GR', {
    weekday: 'long', day: '2-digit', month: '2-digit', year: 'numeric',
});

function groupByDay(transactions) {
    const groups = [];
    for (const t of transactions) {
        const date = new Date(t.timestamp);
        const key = date.toDateString();
        const current = groups[groups.length - 1];

        if (current?.key === key) current.items.push(t);
        else groups.push({ key, label: DAY_FORMAT.format(date), items: [t] });
    }
    return groups;
}

export default function TransactionTable({ transactions }) {
    if (!transactions || transactions.length === 0) {
        return <p className="text-center text-muted py-4">Δεν βρέθηκαν συναλλαγές</p>;
    }

    const groups = groupByDay(transactions);

    return (
        <table className="w-full text-sm">
            <thead>
                <tr className="text-[13px] text-muted">
                    <th className="text-left font-semibold pb-2">Ώρα</th>
                    <th className="text-left font-semibold pb-2">Τύπος</th>
                    <th className="text-left font-semibold pb-2">Περιγραφή</th>
                    <th className="text-right font-semibold pb-2">Ποσό</th>
                </tr>
            </thead>

            {groups.map(group => (
                <tbody key={group.key}>
                    <tr>
                        <th colSpan={4}
                            className="text-left font-bold text-[13px] pt-5 pb-2 border-b border-hairline">
                            {group.label}
                        </th>
                    </tr>
                    {group.items.map(t => (
                        <TransactionRow key={`${t.timestamp}-${t.type}-${t.amount}`} transaction={t} />
                    ))}
                </tbody>
            ))}
        </table>
    );
}
