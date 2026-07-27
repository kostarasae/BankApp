import AccountBalance from './components/AccountBalance';
import BalanceCard from './components/BalanceCard';
import DepositForm from './components/DepositForm';
import TransactionTable from './components/TransactionTable'; 

function App() {
  const dummyTransactions = [
    { timestamp: '2026-07-20T10:00:00', type: 'DEPOSIT', amount: 200, description: 'ATM Σύνταγμα' },
    { timestamp: '2026-07-19T09:00:00', type: 'WITHDRAWAL', amount: 50, description: 'ATM Ομόνοια' },
  ];
  return (
    <div>
      <AccountBalance iban="GR1600000000000000000000001" />
      <BalanceCard balance="1.234,56 €" />
      <DepositForm iban="GR1600000000000000000000001"/>
      <TransactionTable transactions={dummyTransactions}/>
    </div>
  );
}

export default App
