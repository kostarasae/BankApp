import Card from './Card';

export default function BalanceCard({ balance }) {
    return (
        <Card className="balance-hover-group">
            <h2 className="card-heading text-lg font-bold text-[#1f3c88] mb-2">Υπόλοιπο</h2>
            <p className="balance-pulse text-2xl font-bold text-[#2e7d32] pl-5">{balance}</p>
        </Card>
    );
}