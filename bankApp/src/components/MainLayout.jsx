import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useCustomerAccounts } from "../hooks/useCustomerAccounts";
import Header from "./Header";
import Card from "./Card";
import Icon from "./Icon";
import Dashboard from "./Dashboard";
import CreateAccountForm from "./CreateAccountForm";
import PaymentsPanel from "./PaymentsPanel";
import IrisForm from "./IrisForm";
import ProfilePanel from "./ProfilePanel";
import SettingsPanel from "./SettingsPanel";
import LoansPanel from "./LoansPanel";
import CardsPanel from "./CardsPanel";
import InvestmentsPanel from "./InvestmentsPanel";

const TABS = [
    { id: 'dashboard',   label: 'Επισκόπηση',  icon: 'home',     Component: Dashboard },
    { id: 'create',      label: 'Λογαριασμοί', icon: 'userPlus', Component: CreateAccountForm, staffOnly: true },
    { id: 'payments',    label: 'Πληρωμές',    icon: 'transfer', Component: PaymentsPanel },
    { id: 'iris',        label: 'IRIS',        icon: 'mobile',   Component: IrisForm },
    { id: 'investments', label: 'Επενδύσεις',  icon: 'chart',    Component: InvestmentsPanel },
    { id: 'loans',       label: 'Δάνεια',      icon: 'bank',     Component: LoansPanel },
    { id: 'cards',       label: 'Κάρτες',      icon: 'card',     Component: CardsPanel },
    { id: 'profile',     label: 'Προφίλ',      icon: 'user',     Component: ProfilePanel },
    { id: 'settings',    label: 'Ρυθμίσεις',   icon: 'gear',     Component: SettingsPanel },
];

const NEEDS_IBAN = ['dashboard', 'payments', 'iris'];

export default function MainLayout() {
    const { role, customerUuid, logout } = useAuth();
    const { accounts, selectedIban, setSelectedIban } = useCustomerAccounts(customerUuid);

    const [activeTab, setActiveTab] = useState('dashboard');
    const [menuOpen, setMenuOpen] = useState(false);

    const visibleTabs = TABS.filter(t => !t.staffOnly || role !== 'CUSTOMER');
    const active = visibleTabs.find(t => t.id === activeTab) ?? visibleTabs[0];

    function selectTab(id) {
        setActiveTab(id);
        setMenuOpen(false);
    }

    return (
        <div className={`min-h-screen bg-white ${menuOpen ? 'menu-open' : ''}`}
            style={{ perspective: '1500px' }}>
            <Header />

            <button onClick={() => setMenuOpen(o => !o)}
                aria-label="Μενού"
                className="fixed top-4 left-4 z-[400] w-14 h-14 px-3 flex flex-col justify-center gap-[7px]
                    bg-primary-dark text-white rounded-lg cursor-pointer
                    transition-transform duration-300 hover:scale-105">
                <span className="block h-[3px] w-full bg-current rounded-full" />
                <span className="block h-[3px] w-full bg-current rounded-full" />
                <span className="block h-[3px] w-full bg-current rounded-full" />
            </button>

            <aside className={`fixed top-0 left-0 w-full max-w-[20vw] min-w-[240px] h-screen
                bg-primary-dark text-[#eaf0f0] pt-24 px-6
                box-border overflow-y-auto transition-transform duration-[800ms] ease z-[300]
                ${menuOpen ? 'translate-x-0' : '-translate-x-full'}`}>
                <nav>
                    <ul className="flex flex-col gap-1">
                        {visibleTabs.map(tab => (
                            <li key={tab.id}>
                                <button onClick={() => selectTab(tab.id)}
                                    className={`w-full flex items-center gap-4 text-left text-[17px] p-3.5 rounded-lg cursor-pointer transition
                                        ${activeTab === tab.id ? 'bg-white/20 font-bold' : 'hover:bg-white/10'}`}>
                                    <Icon name={tab.icon} className="w-6 h-6 shrink-0" />{tab.label}
                                </button>
                            </li>
                        ))}
                        <li className="mt-4 border-t border-white/20 pt-2">
                            <button onClick={logout}
                                className="w-full flex items-center gap-4 text-left text-[17px] p-3.5 rounded-lg cursor-pointer hover:bg-white/10">
                                <Icon name="logout" className="w-6 h-6 shrink-0" />Αποσύνδεση
                            </button>
                        </li>
                    </ul>
                </nav>
            </aside>

            <div onClick={() => setMenuOpen(false)}
                className={`fixed inset-0 bg-black/30 z-[100] transition-opacity duration-300
                    ${menuOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'}`} />

            <main className="p-5 max-w-[900px] mx-auto">
                <div className="surface-group [&>*:last-child>.card:last-child]:mb-0">
                {accounts.length > 1 && NEEDS_IBAN.includes(active.id) && (
                    <Card>
                        <label className="block font-bold text-sm mb-[3px]">Ενεργός Λογαριασμός</label>
                        <select value={selectedIban} onChange={e => setSelectedIban(e.target.value)}
                            className="p-3 text-base border border-gray-300 rounded h-12 box-border bg-white w-full max-w-[420px]">
                            {accounts.map(a => <option key={a.iban} value={a.iban}>{a.iban}</option>)}
                        </select>
                    </Card>
                )}

                {NEEDS_IBAN.includes(active.id)
                    ? <active.Component iban={selectedIban} />
                    : <active.Component />}
                </div>
            </main>
        </div>
    );
}
