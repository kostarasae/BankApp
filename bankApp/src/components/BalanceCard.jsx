import Card from './Card';

export default function BalanceCard({ balance }) {
    return (
        <Card className="balance-hover-group">
            <p className="text-[13px] text-muted pl-5">Διαθέσιμο υπόλοιπο</p>
            <p className="balance-pulse text-[32px] leading-tight font-bold text-primary tabular-nums pl-5 mt-1">
                {balance}
            </p>
        </Card>
    );
}
