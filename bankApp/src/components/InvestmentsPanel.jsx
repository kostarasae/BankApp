import { useState, useMemo } from 'react';
import Card from './Card';
import { formatAmount, formatEuro } from '../utils/format';

const PERIODS = ['1W', '1M', '3M', '1Y'];
const POINTS_BY_PERIOD = { '1W': 5, '1M': 22, '3M': 66, '1Y': 60 };

const HOLDINGS = [
    { name: 'Μετοχές εξωτερικού', code: 'MSCI World', value: 4820.5, weight: 42, change: 3.1 },
    { name: 'Ελληνικές μετοχές', code: 'ATHEX 25', value: 2410.0, weight: 21, change: -1.4 },
    { name: 'Ομόλογα', code: 'EU Govt 5Y', value: 2755.0, weight: 24, change: 0.6 },
    { name: 'Ρευστά διαθέσιμα', code: 'EUR', value: 1490.0, weight: 13, change: 0 },
];

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
    const lineColor = isUp ? '#2e7d32' : '#c62828';

    const width = 600, height = 200;
    const min = Math.min(...prices), max = Math.max(...prices);
    const toXY = (p, i) => {
        const x = (i / (prices.length - 1)) * width;
        const y = height - ((p - min) / (max - min || 1)) * height;
        return [x, y];
    };
    const points = prices.map(toXY).map(([x, y]) => `${x},${y}`).join(' ');
    const area = `0,${height} ${points} ${width},${height}`;
    const gridLines = [0, 0.25, 0.5, 0.75, 1];

    const total = HOLDINGS.reduce((sum, h) => sum + h.value, 0);

    return (
        <>
            <Card>
                <h2 className="card-heading mb-4">Επενδύσεις</h2>

                <div className="flex items-baseline gap-4 mb-4 flex-wrap">
                    <span className="text-xl font-bold text-primary">S&amp;P 500</span>
                    <span className="text-3xl font-bold text-ink tabular-nums">{formatAmount(last)} pts</span>
                    <span className="text-lg font-bold tabular-nums" style={{ color: lineColor }}>
                        {isUp ? '+' : ''}{formatAmount(change)} ({isUp ? '+' : ''}{formatAmount(pct)}%)
                    </span>
                </div>

                <div className="flex gap-2 mb-5">
                    {PERIODS.map(p => (
                        <button key={p} onClick={() => setPeriod(p)}
                            className={`py-2 px-5 rounded-full border font-bold transition-all cursor-pointer ${
                                p === period
                                    ? 'bg-primary text-white border-primary'
                                    : 'bg-white text-ink border-gray-300 hover:border-primary'
                            }`}>
                            {p}
                        </button>
                    ))}
                </div>

                <div className="flex gap-4">
                    <div className="flex flex-col justify-between text-sm text-muted tabular-nums py-1 shrink-0">
                        <span>{formatAmount(max)}</span>
                        <span>{formatAmount((max + min) / 2)}</span>
                        <span>{formatAmount(min)}</span>
                    </div>

                    <svg viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="none"
                        className="w-full h-64 border-l border-b border-hairline">
                        {gridLines.map(g => (
                            <line key={g} x1="0" x2={width} y1={g * height} y2={g * height}
                                stroke="var(--color-hairline)" strokeWidth="1" vectorEffect="non-scaling-stroke" />
                        ))}
                        <polygon fill={lineColor} fillOpacity="0.08" points={area} />
                        <polyline fill="none" stroke={lineColor} strokeWidth="2"
                            vectorEffect="non-scaling-stroke" points={points} />
                    </svg>
                </div>
            </Card>

            <Card className="mb-0">
                <div className="flex flex-wrap items-baseline justify-between gap-2 mb-4">
                    <h2 className="card-heading">Το χαρτοφυλάκιό μου</h2>
                    <p className="text-sm text-muted">
                        Συνολική αξία
                        <span className="text-2xl font-bold text-primary tabular-nums ml-2">{formatEuro(total)}</span>
                    </p>
                </div>

                <table className="w-full table-fixed">
                    <thead>
                        <tr className="text-sm text-muted">
                            <th className="text-left font-semibold pb-2">Κατηγορία</th>
                            <th className="text-left font-semibold pb-2 w-[180px]">Δείκτης</th>
                            <th className="text-right font-semibold pb-2 w-[170px]">Αξία</th>
                            <th className="text-right font-semibold pb-2 w-[150px]">Βάρος</th>
                            <th className="text-right font-semibold pb-2 w-[130px]">Μεταβολή</th>
                        </tr>
                    </thead>
                    <tbody>
                        {HOLDINGS.map(h => (
                            <tr key={h.code} className="border-b border-hairline last:border-0">
                                <td className="py-3 pr-3 font-bold">{h.name}</td>
                                <td className="py-3 pr-3 text-muted">{h.code}</td>
                                <td className="py-3 text-right tabular-nums whitespace-nowrap">{formatEuro(h.value)}</td>
                                <td className="py-3 pl-6">
                                    <span className="flex items-center gap-2 justify-end">
                                        <span className="grow h-2 rounded-full bg-marble overflow-hidden max-w-[70px]">
                                            <span className="block h-full bg-primary-soft"
                                                style={{ width: `${h.weight}%` }} />
                                        </span>
                                        <span className="tabular-nums text-sm text-muted w-[42px] text-right">{h.weight}%</span>
                                    </span>
                                </td>
                                <td className={`py-3 text-right font-bold tabular-nums whitespace-nowrap
                                    ${h.change > 0 ? 'text-green-700' : h.change < 0 ? 'text-red-600' : 'text-muted'}`}>
                                    {h.change > 0 ? '+' : ''}{formatAmount(h.change)}%
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </Card>
        </>
    );
}
