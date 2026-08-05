import { useState } from 'react';
import Card from './Card';

export default function LoansPanel() {
    const [amount, setAmount] = useState(10000);
    const [rate, setRate] = useState(5.5);
    const [months, setMonths] = useState(60);
    const [result, setResult] = useState(null);

    function handleSubmit(e) {
        e.preventDefault();
        const P = Number(amount);
        const n = Number(months);
        const r = Number(rate) / 100 / 12;
        const M = r === 0 ? P / n : (P * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
        const total = M * n;
        const interest = total - P;

        const rows = [];
        let balance = P;
        for (let i = 1; i <= n; i++) {
            const iPayment = balance * r;
            const pPayment = M - iPayment;
            balance = Math.max(0, balance - pPayment);
            rows.push({ month: i, payment: M, principal: pPayment, interest: iPayment, balance });
        }
        setResult({ monthly: M, total, interest, rows });
    }

    return (
        <Card>
            <h2 className="card-heading text-lg font-bold text-[#1f3c88] mb-3">Δάνεια</h2>
            <form onSubmit={handleSubmit} className="flex flex-col gap-2.5">
                <fieldset className="flex flex-col gap-1 rounded-lg p-4 border border-gray-300">
                    <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Υπολογισμός Δανείου</legend>
                    <div className="flex gap-5 flex-wrap mb-2">
                        <div className="flex-1 min-w-[180px] flex flex-col">
                            <label className="font-bold text-sm mt-2.5 mb-[3px]">Ποσό Δανείου (€)</label>
                            <input className="p-3 text-base border border-gray-300 rounded h-12 box-border" type="number" min="1000" max="500000" step="1000"
                                value={amount} onChange={e => setAmount(e.target.value)} />
                        </div>
                        <div className="flex-1 min-w-[180px] flex flex-col">
                            <label className="font-bold text-sm mt-2.5 mb-[3px]">Ετήσιο Επιτόκιο (%)</label>
                            <input className="p-3 text-base border border-gray-300 rounded h-12 box-border" type="number" min="0.1" max="30" step="0.1"
                                value={rate} onChange={e => setRate(e.target.value)} />
                        </div>
                        <div className="flex-1 min-w-[180px] flex flex-col">
                            <label className="font-bold text-sm mt-2.5 mb-[3px]">Διάρκεια (μήνες)</label>
                            <input className="p-3 text-base border border-gray-300 rounded h-12 box-border" type="number" min="12" max="360" step="12"
                                value={months} onChange={e => setMonths(e.target.value)} />
                        </div>
                    </div>
                    <button className="p-2.5 text-lg font-bold border-0 rounded cursor-pointer bg-[#1f3c88] text-[#eaf0f0]" type="submit">Υπολογισμός</button>
                </fieldset>
            </form>

            {result && (
                <div className="mt-4">
                    <div className="flex gap-4 my-5 flex-wrap">
                        <div className="flex-1 min-w-[140px] bg-[#eaf0f0] rounded-[10px] p-4 flex flex-col gap-1.5 text-center">
                            <span className="text-[13px] text-[#666]">Μηνιαία Δόση</span>
                            <strong className="text-[22px] text-[#1f3c88]">{result.monthly.toFixed(2)} €</strong>
                        </div>
                        <div className="flex-1 min-w-[140px] bg-[#eaf0f0] rounded-[10px] p-4 flex flex-col gap-1.5 text-center">
                            <span className="text-[13px] text-[#666]">Συνολικό Ποσό</span>
                            <strong className="text-[22px] text-[#1f3c88]">{result.total.toFixed(2)} €</strong>
                        </div>
                        <div className="flex-1 min-w-[140px] bg-[#eaf0f0] rounded-[10px] p-4 flex flex-col gap-1.5 text-center">
                            <span className="text-[13px] text-[#666]">Συνολικοί Τόκοι</span>
                            <strong className="text-[22px] text-[#1f3c88]">{result.interest.toFixed(2)} €</strong>
                        </div>
                    </div>
                    <details>
                        <summary className="cursor-pointer font-bold py-2">Αναλυτικός Πίνακας Αποπληρωμής</summary>
                        <div className="max-h-80 overflow-y-auto mt-2.5 rounded-lg border border-gray-300">
                            <table className="w-full text-sm">
                                <thead>
                                    <tr>
                                        <th className="p-2 border border-gray-300">Μήνας</th>
                                        <th className="p-2 border border-gray-300">Δόση (€)</th>
                                        <th className="p-2 border border-gray-300">Κεφάλαιο (€)</th>
                                        <th className="p-2 border border-gray-300">Τόκος (€)</th>
                                        <th className="p-2 border border-gray-300">Υπόλοιπο (€)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {result.rows.map(row => (
                                        <tr key={row.month}>
                                            <td className="p-2 border border-gray-300 text-center">{row.month}</td>
                                            <td className="p-2 border border-gray-300 text-center">{row.payment.toFixed(2)}</td>
                                            <td className="p-2 border border-gray-300 text-center">{row.principal.toFixed(2)}</td>
                                            <td className="p-2 border border-gray-300 text-center">{row.interest.toFixed(2)}</td>
                                            <td className="p-2 border border-gray-300 text-center">{row.balance.toFixed(2)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </details>
                </div>
            )}
        </Card>
    );
}
