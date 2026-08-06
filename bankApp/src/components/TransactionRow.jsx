import { formatEuro } from '../utils/format';

const TYPE_LABEL = { DEPOSIT: 'Κατάθεση', WITHDRAWAL: 'Ανάληψη', TRANSFER: 'Μεταφορά' };

const TIME_FORMAT = new Intl.DateTimeFormat('el-GR', {
    hour: '2-digit', minute: '2-digit', hourCycle: 'h23',
});

export default function TransactionRow({ transaction }) {
    const isIncome = transaction.type === 'DEPOSIT';
    const time = TIME_FORMAT.format(new Date(transaction.timestamp));

    return (
        <tr className="border-b border-hairline last:border-0">
            <td className="py-3 pr-3 text-left text-muted tabular-nums whitespace-nowrap">{time}</td>
            <td className="py-3 pr-3 text-left whitespace-nowrap">{TYPE_LABEL[transaction.type]}</td>
            <td className="py-3 pr-3 text-left text-muted">{transaction.description}</td>
            <td className={`amount py-3 text-right font-bold tabular-nums whitespace-nowrap
                ${isIncome ? 'text-green-700' : 'text-red-600'}`}>
                {isIncome ? '+' : '-'}{formatEuro(transaction.amount)}
            </td>
        </tr>
    );
}
