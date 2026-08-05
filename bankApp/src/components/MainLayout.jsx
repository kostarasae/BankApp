import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useCustomerAccounts } from "../hooks/useCustomerAccounts";
import Header from "./Header";
import Footer from "./Footer";
import Card from "./Card";
import Icon from "./Icon";
import ChatWidget from "./ChatWidget";
import Dashboard from "./Dashboard";
import HistoryPanel from "./HistoryPanel";
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
    { id: 'history',     label: 'Ιστορικό',    icon: 'history',  Component: HistoryPanel },
    { id: 'iris',        label: 'IRIS',        icon: 'mobile',   Component: IrisForm },
    { id: 'investments', label: 'Επενδύσεις',  icon: 'chart',    Component: InvestmentsPanel },
    { id: 'loans',       label: 'Δάνεια',      icon: 'bank',     Component: LoansPanel },
    { id: 'cards',       label: 'Κάρτες',      icon: 'card',     Component: CardsPanel },
    { id: 'profile',     label: 'Προφίλ',      icon: 'user',     Component: ProfilePanel },
    { id: 'settings',    label: 'Ρυθμίσεις',   icon: 'gear',     Component: SettingsPanel },
];

const NEEDS_IBAN = ['dashboard', 'payments', 'history', 'iris'];

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

    function openHistory(iban) {
        setSelectedIban(iban);
        setActiveTab('history');
    }

    const panelProps = NEEDS_IBAN.includes(active.id) ? { iban: selectedIban } : {};
    if (active.id === 'dashboard') {
        Object.assign(panelProps, {
            accounts, selectedIban, onSelect: setSelectedIban, onOpenHistory: openHistory,
        });
    }

    return (
        <>
        <div className={`min-h-screen bg-white ${menuOpen ? 'menu-open' : ''}`}
            style={{ perspective: '1500px' }}>

            <button onClick={() => setMenuOpen(o => !o)}
                aria-label="Μενού"
                className="fixed top-4 left-4 z-[400] w-14 h-14 px-3 flex flex-col justify-center gap-[7px]
                    bg-primary-dark text-white rounded-lg cursor-pointer
                    transition-transform duration-300 hover:scale-105">
                <span className="block h-[3px] w-full bg-current rounded-full" />
                <span className="block h-[3px] w-full bg-current rounded-full" />
                <span className="block h-[3px] w-full bg-current rounded-full" />
            </button>

            <aside className={`fixed top-0 left-0 h-screen
                bg-primary-dark text-[#eaf0f0] pt-24 px-3
                box-border overflow-y-auto overflow-x-hidden
                transition-[width] duration-[800ms] ease z-[300]
                ${menuOpen ? 'w-[260px]' : 'w-[80px]'}`}>
                <nav>
                    <ul className="flex flex-col gap-1">
                        {visibleTabs.map(tab => (
                            <li key={tab.id}>
                                <button onClick={() => selectTab(tab.id)}
                                    title={tab.label}
                                    className={`w-full flex items-center gap-4 text-left text-[17px] p-3 rounded-lg cursor-pointer transition
                                        ${activeTab === tab.id ? 'bg-white/20 font-bold' : 'hover:bg-white/10'}`}>
                                    <Icon name={tab.icon} className="w-7 h-7 shrink-0" />
                                    <span className={`whitespace-nowrap transition-opacity duration-300
                                        ${menuOpen ? 'opacity-100' : 'opacity-0'}`}>{tab.label}</span>
                                </button>
                            </li>
                        ))}
                        <li className="mt-4 border-t border-white/20 pt-2">
                            <button onClick={logout} title="Αποσύνδεση"
                                className="w-full flex items-center gap-4 text-left text-[17px] p-3 rounded-lg cursor-pointer hover:bg-white/10">
                                <Icon name="logout" className="w-7 h-7 shrink-0" />
                                <span className={`whitespace-nowrap transition-opacity duration-300
                                    ${menuOpen ? 'opacity-100' : 'opacity-0'}`}>Αποσύνδεση</span>
                            </button>
                        </li>
                    </ul>
                </nav>
            </aside>

            <div onClick={() => setMenuOpen(false)}
                className={`fixed inset-0 bg-black/30 z-[100] transition-opacity duration-300
                    ${menuOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'}`} />

            <div className="pl-[80px]">
            <Header />
            <main className="p-5 max-w-[900px] mx-auto">
                <div className="surface-group [&>*:last-child>.card:last-child]:mb-0">
                {accounts.length > 1 && NEEDS_IBAN.includes(active.id) && active.id !== 'dashboard' && (
                    <Card>
                        <label className="block font-bold text-sm mb-[3px]">Ενεργός Λογαριασμός</label>
                        <select value={selectedIban} onChange={e => setSelectedIban(e.target.value)}
                            className="p-3 text-base border border-gray-300 rounded h-12 box-border bg-white w-full max-w-[420px]">
                            {accounts.map(a => <option key={a.iban} value={a.iban}>{a.iban}</option>)}
                        </select>
                    </Card>
                )}

                <active.Component {...panelProps} />
                </div>
            </main>
            <Footer />
            </div>
        </div>

        <ChatWidget />
        </>
    );
}
