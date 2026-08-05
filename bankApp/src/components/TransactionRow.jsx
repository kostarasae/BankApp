import { formatEuro } from '../utils/format';

const TYPE_LABEL = { DEPOSIT: 'Κατάθεση', WITHDRAWAL: 'Ανάληψη', TRANSFER: 'Μεταφορά' };

// hourCycle h23 → "22:12" και όχι "10:12 μ.μ.". Το 12ωρο δεν χρησιμοποιείται σε
// ελληνικά παραστατικά, και σε λίστα ταξινομημένη κατά χρόνο κρύβει τη σειρά:
// το "11:41 μ.μ." τυπώνεται πριν από το "11:35 π.μ." και μοιάζει με λάθος.
const TIME_FORMAT = new Intl.DateTimeFormat('el-GR', {
    hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
});

export default function TransactionRow({ transaction }) {
    const isIncome = transaction.type === 'DEPOSIT';

    // Η ημερομηνία μπαίνει στην επικεφαλίδα της ημέρας (TransactionTable), οπότε
    // εδώ μένει μόνο η ώρα.
    const time = TIME_FORMAT.format(new Date(transaction.timestamp));

    return (
        <tr className="border-b border-hairline last:border-0">
            <td className="py-3 pr-3 text-left text-muted tabular-nums whitespace-nowrap">{time}</td>
            <td className="py-3 pr-3 text-left whitespace-nowrap">{TYPE_LABEL[transaction.type]}</td>
            <td className="py-3 pr-3 text-left text-muted">{transaction.description}</td>
            {/* tabular-nums: κάθε ψηφίο πιάνει το ίδιο πλάτος, ώστε οι υποδιαστολές
                να ευθυγραμμίζονται κάθετα από γραμμή σε γραμμή */}
            <td className={`py-3 text-right font-bold tabular-nums whitespace-nowrap
                ${isIncome ? 'text-green-700' : 'text-ink'}`}>
                {isIncome ? '+' : '-'}{formatEuro(transaction.amount)}
            </td>
        </tr>
    );
}
