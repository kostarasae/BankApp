import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useCustomerAccounts } from "../hooks/useCustomerAccounts";
import Header from "./Header";
import Dashboard from "./Dashboard";
import CreateAccountForm from "./CreateAccountForm";
import PaymentsPanel from "./PaymentsPanel";
import IrisForm from "./IrisForm";
import ProfilePanel from "./ProfilePanel";
import SettingsPanel from "./SettingsPanel";
import LoansPanel from "./LoansPanel";
import CardsPanel from "./CardsPanel";
import InvestmentsPanel from "./InvestmentsPanel";

// Data-driven: αντικαθιστά τα 9 ξεχωριστά <li data-target="..."> του vanilla με ένα .map().
// Το `Component` (κεφαλαίο C) κρατά την ΙΔΙΑ τη function — renderάρεται αργότερα, όταν
// επιλεγεί το tab. `staffOnly` κρύβει το tab από τον CUSTOMER (F.20).
const TABS = [
    { id: 'dashboard',   label: 'Προεπισκόπηση', icon: '🏠', Component: Dashboard },
    { id: 'create',      label: 'Λογαριασμοί',   icon: '➕', Component: CreateAccountForm, staffOnly: true },
    { id: 'payments',    label: 'Πληρωμές',      icon: '💸', Component: PaymentsPanel },
    { id: 'iris',        label: 'IRIS',          icon: '📲', Component: IrisForm },
    { id: 'investments', label: 'Επενδύσεις',    icon: '📈', Component: InvestmentsPanel },
    { id: 'loans',       label: 'Δάνεια',        icon: '🏦', Component: LoansPanel },
    { id: 'cards',       label: 'Κάρτες',        icon: '💳', Component: CardsPanel },
    { id: 'profile',     label: 'Προφίλ',        icon: '👤', Component: ProfilePanel },
    { id: 'settings',    label: 'Ρυθμίσεις',     icon: '⚙️', Component: SettingsPanel },
];

// Ποια panels δουλεύουν πάνω σε συγκεκριμένο λογαριασμό
const NEEDS_IBAN = ['dashboard', 'payments', 'iris'];

export default function MainLayout() {
    const { role, customerUuid, logout } = useAuth();
    const { accounts, selectedIban, setSelectedIban } = useCustomerAccounts(customerUuid);

    const [activeTab, setActiveTab] = useState('dashboard');
    const [menuOpen, setMenuOpen] = useState(false);

    // F.20 — το hiding είναι μόνο UX. Η πραγματική προστασία είναι το @PreAuthorize
    // στο backend: ο CUSTOMER που καλέσει το POST /accounts παίρνει 403 έτσι κι αλλιώς.
    const visibleTabs = TABS.filter(t => !t.staffOnly || role !== 'CUSTOMER');
    const active = visibleTabs.find(t => t.id === activeTab) ?? visibleTabs[0];

    function selectTab(id) {
        setActiveTab(id);
        setMenuOpen(false);
    }

    return (
        <div className="min-h-screen bg-[#eaf0f0]">
            <Header />

            <button onClick={() => setMenuOpen(o => !o)}
                aria-label="Μενού"
                className="p-2.5 fixed top-4 left-4 z-[1001] text-2xl bg-primary-dark text-white rounded">
                ☰
            </button>

            <aside className={`fixed top-0 left-0 w-64 h-screen bg-primary-dark text-[#eaf0f0] pt-20 px-4
                box-border overflow-y-auto transition-transform duration-300 ease-in-out z-[1000]
                ${menuOpen ? 'translate-x-0' : '-translate-x-full'}`}>
                <nav>
                    <ul className="flex flex-col gap-1">
                        {visibleTabs.map(tab => (
                            <li key={tab.id}>
                                <button onClick={() => selectTab(tab.id)}
                                    className={`w-full text-left p-3 rounded cursor-pointer transition
                                        ${activeTab === tab.id ? 'bg-white/20 font-bold' : 'hover:bg-white/10'}`}>
                                    <span className="mr-2">{tab.icon}</span>{tab.label}
                                </button>
                            </li>
                        ))}
                        <li className="mt-4 border-t border-white/20 pt-2">
                            <button onClick={logout}
                                className="w-full text-left p-3 rounded cursor-pointer hover:bg-white/10">
                                <span className="mr-2">🚪</span>Αποσύνδεση
                            </button>
                        </li>
                    </ul>
                </nav>
            </aside>

            {menuOpen && (
                <div onClick={() => setMenuOpen(false)}
                    className="fixed inset-0 bg-black/40 z-[999]" />
            )}

            <main className="p-5 max-w-[900px] mx-auto">
                {accounts.length > 1 && NEEDS_IBAN.includes(active.id) && (
                    <div className="flex flex-col gap-1 mb-4">
                        <label className="font-bold text-sm">Ενεργός Λογαριασμός</label>
                        <select value={selectedIban} onChange={e => setSelectedIban(e.target.value)}
                            className="p-3 text-base border border-gray-300 rounded h-12 box-border bg-white max-w-[420px]">
                            {accounts.map(a => <option key={a.iban} value={a.iban}>{a.iban}</option>)}
                        </select>
                    </div>
                )}

                {NEEDS_IBAN.includes(active.id)
                    ? <active.Component iban={selectedIban} />
                    : <active.Component />}
            </main>
        </div>
    );
}
