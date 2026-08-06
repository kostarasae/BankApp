import { useState } from "react";
import { getAccountFee, getAccountOwner, withdraw, transfer } from "../api/restBankApi";
import AtmForm from "./AtmForm";
import Button from "./Button";
import Card from "./Card";
import StatusMessage from "./StatusMessage";
import { getErrorMessage } from "../utils/apiError";
import { formatEuro } from "../utils/format";

export default function PaymentsPanel({ iban, onSuccess }) {
    const [payment, setPayment] = useState({ provider: '', paymentId: '', amount: '' });
    const [transferForm, setTransferForm] = useState({ recipientIban: '', description: '', amount: '' });
    const [paymentStatus, setPaymentStatus] = useState({ text: '', ok: false });
    const [transferStatus, setTransferStatus] = useState({ text: '', ok: false });
    const [loading, setLoading] = useState(false);

    async function handlePayment() {
        try{
            const fee = await getAccountFee(iban);
            if (!window.confirm(`Θα χρεωθείτε με προμήθεια ${formatEuro(fee)}. Συνέχεια;`)) return;
            setLoading(true);
            setPaymentStatus({ text: '', ok: false });
            await withdraw(
                iban, 
                `Λογαριασμός ${payment.provider}: ${payment.paymentId}`,
                Number(payment.amount)
            );
            setPaymentStatus({ text: 'Η πληρωμή ολοκληρώθηκε', ok: true });
            onSuccess?.();
            setPayment({
                provider:'',
                paymentId:'',
                amount:''
            });
        } catch (err) {
            setPaymentStatus({ text: getErrorMessage(err), ok: false });
        } finally {
            setLoading(false);
        }
    }

    async function handleTransfer() {
        const to = transferForm.recipientIban.trim().toUpperCase();
        if (!/^GR\d{25}$/.test(to)) {
            setTransferStatus({ text: `Το IBAN πρέπει να έχει 27 χαρακτήρες (GR + 25 ψηφία) — έδωσες ${to.length}`, ok: false });
            return;
        }
        if (to === iban) {
            setTransferStatus({ text: 'Δεν γίνεται μεταφορά στον ίδιο λογαριασμό', ok: false });
            return;
        }
        try{
            const fee = await getAccountFee(iban);
            const owner = await getAccountOwner(to);
            const ok = window.confirm(
                `Παραλήπτης: ${owner.firstname} ${owner.lastname}\nΠοσό: ${formatEuro(transferForm.amount)}\nΠρομήθεια: ${formatEuro(fee)}\n\nΝα προχωρήσει η μεταφορά;`
            )
            if (!ok) return;
            setLoading(true);
            setTransferStatus({ text: '', ok: false });
            await transfer(
                iban,
                to,
                transferForm.description,
                Number(transferForm.amount)
            );
            setTransferStatus({ text: 'Η μεταφορά ολοκληρώθηκε', ok: true });
            onSuccess?.();
            setTransferForm({
                recipientIban:'',
                description:'',
                amount:''
            });
        } catch (err) {
            setTransferStatus({ text: getErrorMessage(err), ok: false });
        } finally {
            setLoading(false);
        }
    }

    return (
        <Card>
            <h2 className="card-heading mb-3">Πληρωμές</h2>

            <div className="flex flex-col gap-4">
                <fieldset className="flex flex-col gap-1 rounded-lg p-4 border border-gray-300">
                    <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Πληρωμή Λογαριασμού</legend>
                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Πάροχος</label>
                    <select value={payment.provider}
                        onChange={e => setPayment(prev => ({ ...prev, provider: e.target.value }))}
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border">
                        <option value="">-- Επιλέξτε Πάροχο --</option>
                        <option value="ΔΕΗ">ΔΕΗ</option>
                        <option value="ΕΥΔΑΠ">ΕΥΔΑΠ</option>
                        <option value="ΟΤΕ">ΟΤΕ</option>
                        <option value="ΑΑΔΕ">ΑΑΔΕ</option>
                    </select>
                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Κωδικός Λογαριασμού</label>
                    <input value={payment.paymentId} placeholder="RF1234567890123456789012345"
                        onChange={e => setPayment(prev => ({ ...prev, paymentId: e.target.value }))}
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />
                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Ποσό (€)</label>
                    <input type="number" min="0.01" step="0.01" value={payment.amount} placeholder="0.00"
                        onChange={e => setPayment(prev => ({ ...prev, amount: e.target.value }))}
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />
                    <Button disabled={loading} onClick={handlePayment} className="mt-2.5 w-full">Πληρωμή</Button>
                    <StatusMessage status={paymentStatus} className="mt-2" />
                </fieldset>

                <fieldset className="flex flex-col gap-1 rounded-lg p-4 border border-gray-300">
                    <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Τραπεζική Μεταφορά</legend>
                    <label className="font-bold text-sm mt-2.5 mb-[3px]">IBAN Παραλήπτη</label>
                    <input value={transferForm.recipientIban} placeholder="GR1234567890123456789012345"
                        onChange={e => setTransferForm(prev => ({ ...prev, recipientIban: e.target.value }))}
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />
                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Περιγραφή</label>
                    <input value={transferForm.description} placeholder="Περιγραφή της μεταφοράς"
                        onChange={e => setTransferForm(prev => ({ ...prev, description: e.target.value }))}
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />
                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Ποσό (€)</label>
                    <input type="number" min="0.01" step="0.01" value={transferForm.amount} placeholder="0.00"
                        onChange={e => setTransferForm(prev => ({ ...prev, amount: e.target.value }))}
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />
                    <Button disabled={loading} onClick={handleTransfer} className="mt-2.5 w-full">Μεταφορά</Button>
                    <StatusMessage status={transferStatus} className="mt-2" />
                </fieldset>

                <AtmForm iban={iban} onSuccess={onSuccess} />
            </div>
        </Card>
    )
}
