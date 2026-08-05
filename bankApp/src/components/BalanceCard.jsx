import Card from './Card';

/**
 * Το υπόλοιπο δεν χρωματίζεται πράσινο.
 *
 * Το πράσινο/κόκκινο σε μια τράπεζα σημαίνει «μπήκαν / βγήκαν χρήματα» και
 * ανήκει στις γραμμές των συναλλαγών. Ένα πράσινο υπόλοιπο διαβάζεται σαν
 * κέρδος, που δεν σημαίνει τίποτα — το υπόλοιπο είναι απλώς ένα νούμερο, όσο
 * μεγάλο ή μικρό κι αν είναι. Μπαίνει λοιπόν στο μπλε της μάρκας, με τη μικρή
 * γκρι ετικέτα από πάνω να εξηγεί τι είναι.
 */
export default function BalanceCard({ balance }) {
    return (
        <Card className="balance-hover-group">
            <p className="text-[13px] text-muted pl-5">Διαθέσιμο υπόλοιπο</p>
            <p className="balance-pulse text-[32px] leading-tight font-bold text-primary tabular-nums pl-5 mt-1">
                {balance}
            </p>
        </Card>
    );
}
