import { useState, useMemo } from 'react';

const PERIODS = ['1W', '1M', '3M', '1Y'];
const POINTS_BY_PERIOD = { '1W': 5, '1M': 22, '3M': 66, '1Y': 60 };

function generateSeries(points) {
    const prices = [5247.32];
    for (let i = 1; i < points; i++) {
        const drift = (Math.random() - 0.48) * 20;
        prices.push(Math.max(100, prices[i - 1] + drift));
    }
    return prices;
}

export default function InvestmentsPanel() {
    const [period, setPeriod] = useState('1Y');
    const prices = useMemo(() => generateSeries(POINTS_BY_PERIOD[period]), [period]);

    const first = prices[0];
    const last = prices[prices.length - 1];
    const change = last - first;
    const pct = (change / first) * 100;
    const isUp = change >= 0;

    const width = 500, height = 160;
    const min = Math.min(...prices), max = Math.max(...prices);
    const points = prices.map((p, i) => {
        const x = (i / (prices.length - 1)) * width;
        const y = height - ((p - min) / (max - min || 1)) * height;
        return `${x},${y}`;
    }).join(' ');

    return (
        <section className="bg-white p-5 mb-[2%] ml-[4%] rounded-2xl">
            <h2 className="text-lg font-bold text-[#1f3c88] mb-2">Επενδύσεις</h2>

            <div className="flex items-baseline gap-3.5 mb-3 flex-wrap">
                <span className="text-[22px] font-bold text-[#1f3c88]">S&amp;P 500</span>
                <span className="text-3xl font-bold text-[#222]">{last.toLocaleString('el-GR', { minimumFractionDigits: 2 })} pts</span>
                <span className="text-base font-bold" style={{ color: isUp ? '#2e7d32' : '#c62828' }}>
                    {isUp ? '+' : ''}{change.toFixed(2)} ({isUp ? '+' : ''}{pct.toFixed(2)}%)
                </span>
            </div>

            <div className="flex gap-2 mb-4">
                {PERIODS.map(p => (
                    <button key={p} onClick={() => setPeriod(p)}
                        className={`py-1.5 px-3.5 rounded-full border text-[13px] font-bold transition-all ${
                            p === period ? 'bg-[#1f3c88] text-white border-[#1f3c88]' : 'bg-white text-[#222] border-gray-300'
                        }`}>
                        {p}
                    </button>
                ))}
            </div>

            <svg viewBox={`0 0 ${width} ${height}`} className="w-full h-40">
                <polyline fill="none" stroke={isUp ? '#2e7d32' : '#c62828'} strokeWidth="2" points={points} />
            </svg>
        </section>
    );
}
