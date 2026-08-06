import Card from './Card';

export default function BalanceCard({ balance }) {
    return (
        <Card className="balance-hover-group">
            <p className="text-sm text-muted pl-5">Διαθέσιμο υπόλοιπο</p>
            <p className="amount balance-pulse text-3xl leading-tight font-bold text-primary tabular-nums pl-5 mt-1">
                {balance}
            </p>
        </Card>
    );
}
