import { useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useCustomerAccounts } from "../hooks/useCustomerAccounts";
import { useCustomers } from "../hooks/useCustomers";
import { useStaffUsers } from "../hooks/useStaffUsers";
import StaffProfile from "./StaffProfile";
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
    { id: 'history',     label: 'Ιστορικό',    icon: 'history',  Component: HistoryPanel },
    { id: 'payments',    label: 'Πληρωμές',    icon: 'transfer', Component: PaymentsPanel },
    { id: 'create',      label: 'Λογαριασμοί', icon: 'userPlus', Component: CreateAccountForm, staffOnly: true },
    { id: 'iris',        label: 'IRIS',        icon: 'mobile',   Component: IrisForm },
    { id: 'investments', label: 'Επενδύσεις',  icon: 'chart',    Component: InvestmentsPanel },
    { id: 'loans',       label: 'Δάνεια',      icon: 'bank',     Component: LoansPanel },
    { id: 'cards',       label: 'Κάρτες',      icon: 'card',     Component: CardsPanel },
    { id: 'profile',     label: 'Προφίλ',      icon: 'user',     Component: ProfilePanel },
    { id: 'settings',    label: 'Ρυθμίσεις',   icon: 'gear',     Component: SettingsPanel },
];

const ROLE_LABELS = { CUSTOMER: 'Πελάτης', ADMIN: 'Διαχειριστής', EMPLOYEE: 'Υπάλληλος' };

const NEEDS_IBAN = ['dashboard', 'payments', 'history', 'iris'];
const PICKER_TABS = ['dashboard', 'payments', 'history', 'iris', 'investments', 'cards', 'profile'];
const NEEDS_CUSTOMER = ['dashboard', 'payments', 'history', 'iris', 'profile'];

export default function MainLayout() {
    const { role, customerUuid, logout } = useAuth();
    const isStaff = role !== 'CUSTOMER';
    const isAdmin = role === 'ADMIN';

    const [staffCustomerUuid, setStaffCustomerUuid] = useState('');
    const [selectedStaffUuid, setSelectedStaffUuid] = useState('');
    const { customers, reload: reloadCustomers } = useCustomers(isStaff);
    const { staff, reload: reloadStaff } = useStaffUsers(isAdmin);

    const activeCustomerUuid = isStaff ? staffCustomerUuid : customerUuid;
    const { accounts, selectedIban, setSelectedIban, reload: reloadAccounts } = useCustomerAccounts(activeCustomerUuid);

    const [activeTab, setActiveTab] = useState(isStaff ? 'create' : 'dashboard');
    const [menuOpen, setMenuOpen] = useState(false);
    const [hideAmounts, setHideAmounts] = useState(false);

    const allowedTabs = TABS.filter(t => !t.staffOnly || isStaff);
    const visibleTabs = isStaff
        ? [...allowedTabs.filter(t => t.staffOnly), ...allowedTabs.filter(t => !t.staffOnly)]
        : allowedTabs;
    const active = visibleTabs.find(t => t.id === activeTab) ?? visibleTabs[0];

    function selectTab(id) {
        setActiveTab(id);
        setMenuOpen(false);
        // A staff member has no accounts, so the selection cannot follow us elsewhere
        if (id !== 'profile') setSelectedStaffUuid('');
    }

    function openHistory(iban) {
        setSelectedIban(iban);
        setActiveTab('history');
    }

    // The picker holds either a customer or a staff user; a prefix keeps them apart
    function selectPerson(value) {
        const [kind, uuid] = value.split(':');
        setStaffCustomerUuid(kind === 'c' ? uuid : '');
        setSelectedStaffUuid(kind === 'u' ? uuid : '');
    }

    // Staff users only make sense on the profile — they have no accounts to show
    const showStaffInPicker = isAdmin && active.id === 'profile';
    const selectedStaff = selectedStaffUuid ? staff.find(u => u.uuid === selectedStaffUuid) : null;

    const panelProps = NEEDS_IBAN.includes(active.id) ? { iban: selectedIban } : {};
    if (active.id === 'dashboard') {
        Object.assign(panelProps, {
            accounts, selectedIban, onSelect: setSelectedIban, onOpenHistory: openHistory,
            isAdmin, onAccountsChanged: reloadAccounts,
        });
    }
    if (active.id === 'profile') {
        Object.assign(panelProps, {
            customerUuid: activeCustomerUuid, isStaff, isAdmin,
            onDeleted: () => { setStaffCustomerUuid(''); reloadCustomers?.(); },
        });
    }
    if (active.id === 'settings') {
        Object.assign(panelProps, { isAdmin, customers });
    }
    if (active.id === 'create') {
        panelProps.isAdmin = isAdmin;
    }

    return (
        <>
            <aside className={`fixed left-0 top-1/2 -translate-y-1/2 max-h-screen
                bg-primary-dark text-[#eaf0f0] px-[18px] py-5 rounded-r-2xl
                box-border overflow-y-auto overflow-x-hidden no-scrollbar
                transition-[width] duration-[800ms] ease z-[300]
                ${menuOpen ? 'w-[320px]' : 'w-[100px]'}`}>
                <div className="mb-3 pb-3 border-b border-white/20">
                    <button onClick={() => setMenuOpen(o => !o)}
                        aria-label="Μενού"
                        className="w-[64px] h-[64px] px-4 flex flex-col justify-center gap-[7px]
                            rounded-xl cursor-pointer transition hover:bg-white/10">
                        <span className="block h-[3px] w-full bg-current rounded-full" />
                        <span className="block h-[3px] w-full bg-current rounded-full" />
                        <span className="block h-[3px] w-full bg-current rounded-full" />
                    </button>
                </div>

                <nav>
                    <ul className="flex flex-col gap-1">
                        {visibleTabs.map(tab => (
                            <li key={tab.id}>
                                <button onClick={() => selectTab(tab.id)}
                                    title={tab.label}
                                    className={`w-full flex items-center gap-4 text-left text-[21px] h-[64px] px-[12px] rounded-xl cursor-pointer transition
                                        ${activeTab === tab.id ? 'bg-white/20 font-bold' : 'hover:bg-white/10'}`}>
                                    <Icon name={tab.icon} className="w-[40px] h-[40px] shrink-0" />
                                    <span className={`whitespace-nowrap transition-opacity duration-300
                                        ${menuOpen ? 'opacity-100' : 'opacity-0'}`}>{tab.label}</span>
                                </button>
                            </li>
                        ))}
                        <li className="mt-4 border-t border-white/20 pt-2">
                            <button onClick={logout} title="Αποσύνδεση"
                                className="w-full flex items-center gap-4 text-left text-[21px] h-[64px] px-[12px] rounded-xl cursor-pointer hover:bg-white/10">
                                <Icon name="logout" className="w-[40px] h-[40px] shrink-0" />
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

        <div className={`min-h-screen bg-white ${menuOpen ? 'menu-open' : ''}`}
            style={{ perspective: '1500px' }}>
            <div className={`pl-[100px] min-h-screen flex flex-col ${hideAmounts ? 'amounts-hidden' : ''}`}>
            <Header className="-ml-[100px] w-[calc(100%+100px)] pl-[124px] pr-6"
                hideAmounts={hideAmounts}
                onToggleAmounts={() => setHideAmounts(h => !h)} />
            <main className="grow w-full p-5 max-w-[1400px] mx-auto">
                <div className="surface-group [&>*:last-child>.card:last-child]:mb-0">
                {isStaff && PICKER_TABS.includes(active.id) && (
                    <Card>
                        <label className="block font-bold text-sm mb-[3px]">
                            {showStaffInPicker ? 'Πρόσωπο' : 'Πελάτης'}
                        </label>
                        <select value={selectedStaffUuid ? `u:${selectedStaffUuid}` : (staffCustomerUuid ? `c:${staffCustomerUuid}` : '')}
                            onChange={e => selectPerson(e.target.value)}
                            className="p-3 text-base border border-gray-300 rounded h-12 box-border bg-white w-full max-w-[420px]">
                            <option value="">— Επιλέξτε {showStaffInPicker ? 'πρόσωπο' : 'πελάτη'} —</option>
                            <optgroup label="Πελάτες">
                                {customers.map(c => (
                                    <option key={c.uuid} value={`c:${c.uuid}`}>
                                        {c.firstname} {c.lastname} ({c.username})
                                    </option>
                                ))}
                            </optgroup>
                            {showStaffInPicker && staff.length > 0 && (
                                <optgroup label="Προσωπικό">
                                    {staff.map(u => (
                                        <option key={u.uuid} value={`u:${u.uuid}`}>
                                            {u.username} ({ROLE_LABELS[u.role] ?? u.role})
                                        </option>
                                    ))}
                                </optgroup>
                            )}
                        </select>
                    </Card>
                )}

                {accounts.length > 1 && NEEDS_IBAN.includes(active.id) && active.id !== 'dashboard' && (
                    <Card>
                        <label className="block font-bold text-sm mb-[3px]">Ενεργός Λογαριασμός</label>
                        <select value={selectedIban} onChange={e => setSelectedIban(e.target.value)}
                            className="p-3 text-base border border-gray-300 rounded h-12 box-border bg-white w-full max-w-[420px]">
                            {accounts.map(a => <option key={a.iban} value={a.iban}>{a.iban}</option>)}
                        </select>
                    </Card>
                )}

                {selectedStaff && active.id === 'profile'
                    ? <StaffProfile user={selectedStaff} isAdmin={isAdmin}
                        onDeleted={() => { setSelectedStaffUuid(''); reloadStaff(); }} />
                    : isStaff && NEEDS_CUSTOMER.includes(active.id) && !activeCustomerUuid
                        ? <Card className="mb-0"><p className="text-muted">Επιλέξτε πελάτη για να δείτε τα στοιχεία του.</p></Card>
                        : <active.Component {...panelProps} />}
                </div>
            </main>
            <Footer className="-ml-[100px] w-[calc(100%+100px)]" />
            </div>
        </div>

        <ChatWidget />
        </>
    );
}
