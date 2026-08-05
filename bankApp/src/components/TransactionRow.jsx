import { formatEuro } from '../utils/format';

const TYPE_LABEL = { DEPOSIT: 'Κατάθεση', WITHDRAWAL: 'Ανάληψη', TRANSFER: 'Μεταφορά' };

export default function TransactionRow({ transaction }) {
    const isIncome = transaction.type === 'DEPOSIT';
    return (
        <tr className="even:bg-[#eaf0f0]">
            <td className="border border-gray-300 p-2 text-left text-sm tabular-nums whitespace-nowrap">
                {new Date(transaction.timestamp).toLocaleDateString('el-GR')}
            </td>
            <td className="border border-gray-300 p-2 text-left text-sm">
                {TYPE_LABEL[transaction.type]}
            </td>
            {/* tabular-nums: κάθε ψηφίο πιάνει το ίδιο πλάτος, ώστε οι υποδιαστολές
                να ευθυγραμμίζονται κάθετα από γραμμή σε γραμμή */}
            <td className={`border border-gray-300 p-2 text-right text-sm font-bold tabular-nums whitespace-nowrap
                ${isIncome ? 'text-green-700' : 'text-red-600'}`}>
                {isIncome ? '+' : '-'}{formatEuro(transaction.amount)}
            </td>
            <td className="border border-gray-300 p-2 text-left text-sm">
                {transaction.description}
            </td>
        </tr>
    );
}