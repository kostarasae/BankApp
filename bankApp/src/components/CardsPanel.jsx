import { useState, useEffect } from 'react';
import Card from './Card';
import { useAuth } from '../context/AuthContext';
import { getCustomer } from '../api/restBankApi';

export default function CardsPanel() {
    const { customerUuid } = useAuth();
    const [holder, setHolder] = useState('');

    useEffect(() => {
        if (!customerUuid) return;
        let ignore = false;
        getCustomer(customerUuid).then(customer => {
            if (ignore) return;
            setHolder(`${customer.firstname} ${customer.lastname}`.toUpperCase());
        });
        return () => { ignore = true; };
    }, [customerUuid]);

    return (
        <Card>
            <h2 className="card-heading mb-3">Κάρτες</h2>

            <div className="w-[440px] max-w-full h-[250px] mx-auto my-6 rounded-[18px] text-white p-6 box-border flex flex-col justify-between relative overflow-hidden font-mono"
                style={{ background: 'linear-gradient(135deg, #132452 0%, #1f3c88 60%, #6e8ff0 100%)', boxShadow: '0 16px 48px rgba(31, 60, 136, 0.45)' }}>
                <div className="flex justify-between items-center">
                    <span className="text-lg font-bold tracking-wide">KostaBank</span>
                    <span className="text-2xl font-bold italic opacity-95">VISA</span>
                </div>
                <div className="w-11 h-8 rounded-md" style={{ background: 'linear-gradient(135deg, #c8922a, #f0c060, #b8860b)' }} />
                <div className="text-xl text-center tracking-[4px] whitespace-nowrap">4728 •••• •••• 3891</div>
                <div className="flex justify-between items-end">
                    <div>
                        <div className="text-[11px] uppercase opacity-70 mb-[3px]">Κάτοχος Κάρτας</div>
                        <div className="text-sm tracking-wide">{holder || '—'}</div>
                    </div>
                    <div>
                        <div className="text-[11px] uppercase opacity-70 mb-[3px]">Λήξη</div>
                        <div>12/28</div>
                    </div>
                </div>
            </div>

            <div className="mt-4 flex flex-col gap-0">
                <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 py-3 px-1 border-b border-gray-300">
                    <span className="font-bold">Τύπος</span><span>VISA Debit</span>
                </div>
                <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 py-3 px-1 border-b border-gray-300">
                    <span className="font-bold">Κατάσταση</span><span className="text-[#2e7d32] font-bold">● Ενεργή</span>
                </div>
                <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 py-3 px-1 border-b border-gray-300">
                    <span className="font-bold">Όριο ημερήσιων συναλλαγών</span><span className="tabular-nums">1.000,00 €</span>
                </div>
                <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 py-3 px-1 border-b border-gray-300">
                    <span className="font-bold">Contactless</span><span>Ενεργό</span>
                </div>
            </div>
        </Card>
    );
}
