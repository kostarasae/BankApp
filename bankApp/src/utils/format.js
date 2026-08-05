const euroFormatter = new Intl.NumberFormat('el-GR', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
});

const decimalFormatter = new Intl.NumberFormat('el-GR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
});

export function formatEuro(value) {
    const n = Number(value);
    return Number.isFinite(n) ? euroFormatter.format(n) : '—';
}

export function formatAmount(value) {
    const n = Number(value);
    return Number.isFinite(n) ? decimalFormatter.format(n) : '—';
}
