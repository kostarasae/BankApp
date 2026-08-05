/**
 * Ελληνική μορφοποίηση αριθμών.
 *
 * Το `toFixed(2)` δίνει πάντα αγγλική γραφή — "1241.50" — χωρίς διαχωριστικό
 * χιλιάδων. Στα ελληνικά (όπως και στην υπόλοιπη ηπειρωτική Ευρώπη) ισχύει το
 * αντίστροφο: τελεία για τις χιλιάδες, κόμμα για τα δεκαδικά → "1.241,50 €".
 *
 * Τα Intl formatters φτιάχνονται μία φορά σε module scope: η κατασκευή τους
 * είναι το ακριβό μέρος, ενώ το ίδιο το format() είναι stateless.
 */
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

/** 1241.5 → "1.241,50 €" — για υπόλοιπα, μηνύματα, σύνολα */
export function formatEuro(value) {
    const n = Number(value);
    return Number.isFinite(n) ? euroFormatter.format(n) : '—';
}

/** 1241.5 → "1.241,50" — για στήλες πίνακα που έχουν ήδη το € στην κεφαλίδα */
export function formatAmount(value) {
    const n = Number(value);
    return Number.isFinite(n) ? decimalFormatter.format(n) : '—';
}
