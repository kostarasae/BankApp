import { useAuth } from '../context/AuthContext';
import { useCustomerProfile } from '../hooks/useCustomerProfile';
import Icon from './Icon';

const ROLE_LABELS = { CUSTOMER: 'Πελάτης', ADMIN: 'Διαχειριστής', EMPLOYEE: 'Υπάλληλος' };

function initials(name) {
    return name.split(' ').filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase();
}

export default function Header({ className = '', hideAmounts, onToggleAmounts }) {
    const { role, customerUuid } = useAuth();
    const { profile } = useCustomerProfile(customerUuid);

    const fullName = profile ? `${profile.firstname} ${profile.lastname}` : '';
    const today = new Intl.DateTimeFormat('el-GR', {
        weekday: 'long', day: '2-digit', month: 'long', year: 'numeric',
    }).format(new Date());

    return (
        <header className={`flex items-center justify-between gap-6 bg-primary text-[#eaf0f0] px-6 py-4 ${className}`}>
            <div className="flex items-center gap-4 min-w-0">
                {fullName ? (
                    <>
                        <span className="w-12 h-12 shrink-0 grid place-items-center rounded-full
                            bg-white/15 font-bold text-lg">
                            {initials(fullName)}
                        </span>
                        <span className="min-w-0">
                            <span className="block font-bold truncate">{fullName}</span>
                            <span className="block text-sm opacity-75">{ROLE_LABELS[role] ?? role}</span>
                        </span>
                    </>
                ) : (
                    <span className="font-bold">{ROLE_LABELS[role] ?? role}</span>
                )}
            </div>

            <p className="hidden lg:block text-sm opacity-75 first-letter:uppercase">{today}</p>

            <div className="flex items-center gap-4 shrink-0">
                <button type="button" onClick={onToggleAmounts}
                    title={hideAmounts ? 'Εμφάνιση ποσών' : 'Απόκρυψη ποσών'}
                    aria-label={hideAmounts ? 'Εμφάνιση ποσών' : 'Απόκρυψη ποσών'}
                    className="w-11 h-11 grid place-items-center rounded-full cursor-pointer
                        transition hover:bg-white/15">
                    <Icon name={hideAmounts ? 'eyeOff' : 'eye'} className="w-6 h-6" />
                </button>

                <span className="w-px h-10 bg-white/25" />

                <figure className="flex items-center gap-3 m-0">
                    <img className="w-12" src="/bank_logo.png" alt="" />
                    <h1 className="text-lg font-bold whitespace-nowrap">KostaBank</h1>
                </figure>
            </div>
        </header>
    );
}
