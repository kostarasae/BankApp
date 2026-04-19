// ===== DONUT CHART =====
let hoverIndex = -1;
let renderDonut = null;

drawDonut("incomeExpensePie", 1, 1, "Συναλλαγές", true);

function drawDonut(canvasId, income, expenses, centerText = "Συναλλαγές", neutral = false) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    const neutralColor = "#c8d0e0";
    const total = neutral ? 2 : (income + expenses);
    const data = neutral
        ? [{ value: 1, color: neutralColor }, { value: 1, color: neutralColor }]
        : [
            { value: expenses, color: "#6e8ff0", label: "Έξοδα" },
            { value: income, color: "#1f3c88", label: "Έσοδα" }
          ];

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const baseRadius = 115;
    const hoverRadius = 125;
    const innerRadius = baseRadius * 0.6;

    const targets = [
        -Math.PI / 2,
        Math.PI / 2
    ];

    const slices = data.map((slice, i) => {
        const angle = (slice.value / total) * Math.PI * 2;
        const center = targets[i];
        return {
            start: center - angle / 2,
            end: center + angle / 2,
            color: slice.color,
            value: slice.value
        };
    });

    function isAngleBetween(a, start, end) {
        if (start <= end) return a >= start && a <= end;
        return a >= start || a <= end;
    }

    function render() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        slices.forEach((slice, i) => {
            const mid = (slice.start + slice.end) / 2;
            const radius = i === hoverIndex ? hoverRadius : baseRadius;

            ctx.beginPath();
            ctx.moveTo(centerX, centerY);
            ctx.arc(centerX, centerY, radius, slice.start, slice.end);
            ctx.closePath();
            ctx.fillStyle = slice.color;
            ctx.fill();

            const tx = centerX + Math.cos(mid) * radius * 0.75;
            const ty = centerY + Math.sin(mid) * radius * 0.75;

            if (!neutral) {
                ctx.fillStyle = "#fff";
                ctx.font = "bold 14px Arial";
                ctx.textAlign = "center";
                ctx.textBaseline = "middle";
                ctx.fillText(slice.value.toFixed(2) + "€", tx, ty);
            }
        });

        // hole
        ctx.beginPath();
        ctx.arc(centerX, centerY, innerRadius, 0, Math.PI * 2);
        ctx.fillStyle = "#fff";
        ctx.fill();

        ctx.fillStyle = "#222";
        ctx.font = "bold 18px Arial";
        ctx.fillText(centerText, centerX, centerY);

        updateLegend(hoverIndex);
    }

    renderDonut = render;
    render();

    if (neutral) return;

    canvas.addEventListener("mousemove", e => {
        const rect = canvas.getBoundingClientRect();
        const x = e.clientX - rect.left - centerX;
        const y = e.clientY - rect.top - centerY;

        const dist = Math.hypot(x, y);
        if (dist < innerRadius || dist > hoverRadius) {
            if (hoverIndex !== -1) {
                hoverIndex = -1;
                render();
            }
            return;
        }

        const angle = Math.atan2(y, x);
        let found = -1;
        slices.forEach((s, i) => {
            if (isAngleBetween(angle, s.start, s.end)) found = i;
        });

        if (found !== hoverIndex) {
            hoverIndex = found;
            render();
        }
    });

    canvas.addEventListener("mouseleave", () => {
        hoverIndex = -1;
        render();
    });
}

function updateLegend(active) {
    document.querySelectorAll(".legend-item").forEach(el => {
        const i = Number(el.dataset.index);
        el.classList.toggle("active", i === active);
    });
}

document.querySelectorAll(".legend-item").forEach(el => {
    el.addEventListener("mouseenter", () => {
        hoverIndex = Number(el.dataset.index);
        renderDonut();
    });

    el.addEventListener("mouseleave", () => {
        hoverIndex = -1;
        renderDonut();
    });
});


// ===== S&P 500 CHART =====
let sp500Chart = null;
let activePeriod = '1Y';
const TRADING_DAYS = { '1W': 5, '1M': 22, '3M': 66, '1Y': 252 };

function generateSP500(tradingDays) {
    const endPrice = 5247.32;
    const startPrice = endPrice / Math.pow(1.10, tradingDays / 252);
    const prices = [];
    const labels = [];
    const today = new Date();
    const tradingDates = [];

    for (let i = Math.ceil(tradingDays * 1.5) + 5; i >= 0; i--) {
        const d = new Date(today);
        d.setDate(today.getDate() - i);
        if (d.getDay() !== 0 && d.getDay() !== 6) tradingDates.push(d);
    }
    const days = tradingDates.slice(-tradingDays);

    for (let i = 0; i < tradingDays; i++) {
        const t = i / Math.max(tradingDays - 1, 1);
        const trend = startPrice + (endPrice - startPrice) * t;
        const noise = Math.sin(i * 0.31) * 90 + Math.sin(i * 0.073) * 220 +
                      Math.cos(i * 0.17) * 130 + Math.sin(i * 1.8) * 45;
        prices.push(+(trend + noise).toFixed(2));

        const d = days[i];
        if (!d) { labels.push(''); continue; }
        if (tradingDays <= 5) {
            labels.push(d.toLocaleDateString('el-GR', { weekday: 'short', day: 'numeric' }));
        } else if (tradingDays <= 22) {
            labels.push(d.toLocaleDateString('el-GR', { day: 'numeric', month: 'short' }));
        } else {
            const step = Math.ceil(tradingDays / 8);
            labels.push(i % step === 0 ? d.toLocaleDateString('el-GR', { day: 'numeric', month: 'short' }) : '');
        }
    }
    return { prices, labels };
}

function renderSP500(period) {
    activePeriod = period;
    const { prices, labels } = generateSP500(TRADING_DAYS[period]);
    const first = prices[0];
    const last = prices[prices.length - 1];
    const change = last - first;
    const pct = (change / first) * 100;
    const isUp = change >= 0;
    const color = isUp ? '#2e7d32' : '#c62828';

    document.getElementById('stockPrice').textContent =
        last.toLocaleString('el-GR', { minimumFractionDigits: 2 }) + ' pts';
    const changeEl = document.getElementById('stockChange');
    changeEl.textContent = (isUp ? '+' : '') + change.toFixed(2) +
        ' (' + (isUp ? '+' : '') + pct.toFixed(2) + '%)';
    changeEl.style.color = color;

    if (sp500Chart) sp500Chart.destroy();
    sp500Chart = new Chart(document.getElementById('sp500Chart'), {
        type: 'line',
        data: {
            labels,
            datasets: [{
                data: prices,
                borderColor: color,
                backgroundColor: isUp ? 'rgba(46,125,50,0.08)' : 'rgba(198,40,40,0.08)',
                borderWidth: 2,
                pointRadius: 0,
                fill: true,
                tension: 0.35
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => ctx.parsed.y.toLocaleString('el-GR', { minimumFractionDigits: 2 }) + ' pts'
                    }
                }
            },
            scales: {
                x: { grid: { display: false }, ticks: { maxRotation: 0 } },
                y: {
                    ticks: { callback: v => v.toLocaleString('el-GR', { minimumFractionDigits: 0 }) + ' pts' },
                    grid: { color: 'rgba(0,0,0,0.06)' }
                }
            }
        }
    });
}

document.querySelectorAll('.btn-period').forEach(btn => {
    btn.addEventListener('click', () => {
        document.querySelectorAll('.btn-period').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        renderSP500(btn.dataset.period);
    });
});
