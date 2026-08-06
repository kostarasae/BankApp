import Card from './Card';
import Icon from './Icon';
import { formatEuro } from '../utils/format';

const TYPES = {
    CHECKING: { label: 'Τρεχούμενος', icon: 'checking' },
    SAVINGS: { label: 'Ταμιευτήριο', icon: 'savings' },
};

export default function AccountCard({ account, selected, onSelect, onOpenHistory }) {
    const type = TYPES[account.accountType] ?? { label: 'Λογαριασμός', icon: 'checking' };

    return (
        <Card className={`mb-0 flex flex-col ${selected ? 'outline outline-2 outline-primary' : ''}`}>
            <button type="button" onClick={() => onSelect(account.iban)}
                className="text-left cursor-pointer grow">
                <span className="flex items-center gap-2 text-primary">
                    <Icon name={type.icon} className="w-6 h-6" />
                    <span className="font-bold">{type.label}</span>
                </span>

                <span className="block text-base text-ink tabular-nums mt-2">
                    Αρ. λογαριασμού {account.accountNumber ?? '—'}
                </span>
                <span className="block text-sm text-muted tabular-nums break-all">
                    {account.iban}
                </span>

                <span className="block text-sm text-muted mt-5">Διαθέσιμο υπόλοιπο</span>
                <span className="amount block text-2xl leading-tight font-bold text-primary tabular-nums">
                    {formatEuro(account.balance)}
                </span>
            </button>

            <div className="mt-4">
                <button type="button" onClick={() => onOpenHistory(account.iban)}
                    className="px-4 py-2 rounded-full border border-primary text-primary
                        font-bold text-sm cursor-pointer transition hover:bg-primary hover:text-white">
                    Κινήσεις
                </button>
            </div>
        </Card>
    );
}
