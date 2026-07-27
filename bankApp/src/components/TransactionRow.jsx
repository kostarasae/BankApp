const TYPE_LABEL = { DEPOSIT: 'Κατάθεση', WITHDRAWAL: 'Ανάληψη', TRANSFER: 'Μεταφορά' };

export default function TransactionRow({ transaction }) {
    const isIncome = transaction.type === 'DEPOSIT';
    return (
        <tr>
            <td className="border border-gray-300 p-2 text-center text-sm">
                {new Date(transaction.timestamp).toLocaleDateString('el-GR')}
            </td>
            <td className="border border-gray-300 p-2 text-center text-sm">
                {TYPE_LABEL[transaction.type]}
            </td>
            <td className={`border border-gray-300 p-2 text-center text-sm font-bold ${isIncome ? 'text-green-700' : 'text-red-600'}`}>
                {isIncome ? '+' : '-'}{transaction.amount.toFixed(2)}
            </td>
            <td className="border border-gray-300 p-2 text-center text-sm">
                {transaction.description}
            </td>
        </tr>
    );
}