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

// Data-driven: αντικαθιστά τα 9 ξεχωριστά <li data-target="..."> του vanilla με ένα .map().
// Το `Component` (κεφαλαίο C) κρατά την ΙΔΙΑ τη function — renderάρεται αργότερα, όταν
// επιλεγεί το tab. `staffOnly` κρύβει το tab από τον CUSTOMER (F.20).
// Το `icon` είναι πλέον κλειδί του Icon component, όχι emoji.
const TABS = [
    { id: 'dashboard',   label: 'Προεπισκόπηση', icon: 'home',     Component: Dashboard },
    { id: 'create',      label: 'Λογαριασμοί',   icon: 'userPlus', Component: CreateAccountForm, staffOnly: true },
    { id: 'payments',    label: 'Πληρωμές',      icon: 'transfer', Component: PaymentsPanel },
    { id: 'iris',        label: 'IRIS',          icon: 'mobile',   Component: IrisForm },
    { id: 'investments', label: 'Επενδύσεις',    icon: 'chart',    Component: InvestmentsPanel },
    { id: 'loans',       label: 'Δάνεια',        icon: 'bank',     Component: LoansPanel },
    { id: 'cards',       label: 'Κάρτες',        icon: 'card',     Component: CardsPanel },
    { id: 'profile',     label: 'Προφίλ',        icon: 'user',     Component: ProfilePanel },
    { id: 'settings',    label: 'Ρυθμίσεις',     icon: 'gear',     Component: SettingsPanel },
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
        // `menu-open` → οι κάρτες γέρνουν σε 3D (index.css), όπως στο vanilla.
        // `perspective` εδώ ώστε το rotateX/rotateY να έχει βάθος αντί για flat skew.
        <div className={`min-h-screen bg-white ${menuOpen ? 'menu-open' : ''}`}
            style={{ perspective: '1500px' }}>
            <Header />

            <button onClick={() => setMenuOpen(o => !o)}
                aria-label="Μενού"
                className="fixed top-4 left-4 z-[400] w-14 h-11 px-3 flex flex-col justify-center gap-[5px]
                    bg-primary-dark text-white rounded cursor-pointer
                    transition-transform duration-300 hover:scale-105">
                <span className="block h-[3px] w-full bg-current rounded-full" />
                <span className="block h-[3px] w-full bg-current rounded-full" />
                <span className="block h-[3px] w-full bg-current rounded-full" />
            </button>

            {/* z-300 / 0.8s ease — ίδια τιμή με το vanilla `.sidebar` */}
            <aside className={`fixed top-0 left-0 w-full max-w-[20vw] min-w-[240px] h-screen
                bg-primary-dark text-[#eaf0f0] pt-20 px-8
                box-border overflow-y-auto transition-transform duration-[800ms] ease z-[300]
                ${menuOpen ? 'translate-x-0' : '-translate-x-full'}`}>
                <nav>
                    <ul className="flex flex-col gap-1">
                        {visibleTabs.map(tab => (
                            <li key={tab.id}>
                                <button onClick={() => selectTab(tab.id)}
                                    className={`w-full flex items-center gap-3 text-left p-3 rounded cursor-pointer transition
                                        ${activeTab === tab.id ? 'bg-white/20 font-bold' : 'hover:bg-white/10'}`}>
                                    <Icon name={tab.icon} />{tab.label}
                                </button>
                            </li>
                        ))}
                        <li className="mt-4 border-t border-white/20 pt-2">
                            <button onClick={logout}
                                className="w-full flex items-center gap-3 text-left p-3 rounded cursor-pointer hover:bg-white/10">
                                <Icon name="logout" />Αποσύνδεση
                            </button>
                        </li>
                    </ul>
                </nav>
            </aside>

            {/* Πάντα στο DOM με opacity transition (όχι conditional render), και σε
                z-100 — ΠΙΣΩ από τις κάρτες (z-200), όπως στο vanilla: το φόντο
                σκοτεινιάζει αλλά οι γερμένες κάρτες μένουν φωτεινές μπροστά. */}
            <div onClick={() => setMenuOpen(false)}
                className={`fixed inset-0 bg-black/30 z-[100] transition-opacity duration-300
                    ${menuOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'}`} />

            <main className="p-5 max-w-[900px] mx-auto">
                {/* Δύο επίπεδα επιφάνειας: μπεζ ομάδα, λευκές κάρτες μέσα της.
                    Το τελευταίο `mb` της κάρτας μηδενίζεται, αλλιώς η ομάδα
                    κλείνει με κενό στο κάτω μέρος. */}
                <div className="surface-group [&>*:last-child>.card:last-child]:mb-0">
                {/* Μέσα σε Card, ώστε να γέρνει μαζί με τα υπόλοιπα όταν ανοίγει το μενού */}
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
