import { useAccount } from '../hooks/useAccount';
import BalanceCard from "./BalanceCard";
import TransactionTable from "./TransactionTable";
import IncomeExpenseDonut from "./IncomeExpenseDonut";
import Card from "./Card";
import { formatEuro } from "../utils/format";

export default function Dashboard({ iban }) {
    const { balance, transactions, loading, error } = useAccount(iban);

    if (!iban) {
        return (
            <Card>
                <h2 className="card-heading text-lg font-bold text-[#1f3c88] mb-3">Προεπισκόπηση</h2>
                <p className="text-gray-500">Δεν υπάρχει λογαριασμός για προβολή.</p>
            </Card>
        );
    }
    if (loading) return <Card><p className="text-gray-400">Φόρτωση...</p></Card>;
    if (error) return <Card><p className="font-bold text-red-500">{error}</p></Card>;

    return (
        <div className="flex flex-col gap-4">
            {balance != null && <BalanceCard balance={formatEuro(balance)} />}
            <IncomeExpenseDonut transactions={transactions} />
            <Card>
                <h2 className="card-heading text-lg font-bold text-[#1f3c88] mb-3">Συναλλαγές</h2>
                <TransactionTable transactions={transactions} />
            </Card>
        </div>
    );
}
