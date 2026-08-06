import { useRef, useEffect, useState, useMemo } from 'react';
import { formatEuro } from '../utils/format';
import Card from './Card';

const CANVAS_SIZE = 360;
const BASE_RADIUS = 160;
const HOVER_RADIUS = 172;
const INNER_RADIUS = BASE_RADIUS * 0.6;

export default function IncomeExpenseDonut({ transactions }) {
    const canvasRef = useRef(null);
    const [hoverIndex, setHoverIndex] = useState(-1);

    const { income, expenses } = useMemo(() => {
        let incomeSum = 0, expenseSum = 0;
        (transactions ?? []).forEach(t => {
            if (t.type === 'DEPOSIT') incomeSum += t.amount;
            else if (t.type === 'WITHDRAWAL' || t.type === 'TRANSFER') expenseSum += t.amount;
        });
        return { income: incomeSum, expenses: expenseSum };
    }, [transactions]);

    const slices = useMemo(() => {
        const total = income + expenses || 1;
        const targets = [-Math.PI / 2, Math.PI / 2];
        const data = [
            { value: expenses, color: '#6e8ff0' },
            { value: income, color: '#1f3c88' },
        ];
        return data.map((slice, i) => {
            const angle = (slice.value / total) * Math.PI * 2;
            const center = targets[i];
            return { start: center - angle / 2, end: center + angle / 2, color: slice.color, value: slice.value };
        });
    }, [income, expenses]);

    useEffect(() => {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext('2d');
        const centerX = canvas.width / 2;
        const centerY = canvas.height / 2;

        ctx.clearRect(0, 0, canvas.width, canvas.height);

        slices.forEach((slice, i) => {
            const mid = (slice.start + slice.end) / 2;
            const radius = i === hoverIndex ? HOVER_RADIUS : BASE_RADIUS;

            ctx.beginPath();
            ctx.moveTo(centerX, centerY);
            ctx.arc(centerX, centerY, radius, slice.start, slice.end);
            ctx.closePath();
            ctx.fillStyle = slice.color;
            ctx.fill();

            const tx = centerX + Math.cos(mid) * radius * 0.75;
            const ty = centerY + Math.sin(mid) * radius * 0.75;
            ctx.fillStyle = '#fff';
            ctx.font = 'bold 22px system-ui, sans-serif';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText(formatEuro(slice.value), tx, ty);
        });

        ctx.beginPath();
        ctx.arc(centerX, centerY, INNER_RADIUS, 0, Math.PI * 2);
        ctx.fillStyle = '#fff';
        ctx.fill();

        ctx.fillStyle = '#222';
        ctx.font = 'bold 26px system-ui, sans-serif';
        ctx.fillText('Συναλλαγές', centerX, centerY);
    }, [slices, hoverIndex]);

    function handleMouseMove(e) {
        const canvas = canvasRef.current;
        const rect = canvas.getBoundingClientRect();
        const centerX = canvas.width / 2;
        const centerY = canvas.height / 2;
        const x = e.clientX - rect.left - centerX;
        const y = e.clientY - rect.top - centerY;

        const dist = Math.hypot(x, y);
        if (dist < INNER_RADIUS || dist > HOVER_RADIUS) {
            if (hoverIndex !== -1) setHoverIndex(-1);
            return;
        }

        const angle = Math.atan2(y, x);
        const found = slices.findIndex(s =>
            s.start <= s.end ? (angle >= s.start && angle <= s.end) : (angle >= s.start || angle <= s.end)
        );
        if (found !== hoverIndex) setHoverIndex(found);
    }

    const legendItems = [
        { label: 'Έξοδα', color: '#6e8ff0', index: 0 },
        { label: 'Έσοδα', color: '#1f3c88', index: 1 },
    ];

    return (
        <Card className="mb-0 flex flex-col items-center">
            <h2 className="card-heading mb-2">Κατανομή Εσόδων / Εξόδων</h2>
            <div className="flex gap-[30px] items-center">
                <canvas
                    ref={canvasRef}
                    width={CANVAS_SIZE}
                    height={CANVAS_SIZE}
                    onMouseMove={handleMouseMove}
                    onMouseLeave={() => setHoverIndex(-1)}
                />
                <div className="flex flex-col gap-2">
                    {legendItems.map(item => (
                        <div
                            key={item.index}
                            onMouseEnter={() => setHoverIndex(item.index)}
                            onMouseLeave={() => setHoverIndex(-1)}
                            className={`flex items-center gap-2 text-sm transition-transform ${
                                hoverIndex === item.index ? 'scale-[1.15] font-bold text-base' : ''
                            }`}
                        >
                            <span className="w-5 h-5 rounded-full" style={{ background: item.color }} />
                            {item.label}
                        </div>
                    ))}
                </div>
            </div>
        </Card>
    );
}
