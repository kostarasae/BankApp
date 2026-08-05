/**
 * Μετατρέπει ένα σφάλμα του axios σε μήνυμα κατανοητό στον χρήστη.
 *
 * Το backend στέλνει `{ code, description }` (ή `{ code, message, errors }` για
 * validation) με ΑΓΓΛΙΚΟ κείμενο — π.χ. "Account with iban=... not found" — και το
 * axios `err.message` δίνει μόνο το άχρηστο "Request failed with status code 404".
 * Εδώ χαρτογραφούμε το `code` σε ελληνικό μήνυμα.
 */

const CODE_MESSAGES = {
    // Δεν βρέθηκε
    AccountNotFound: 'Δεν βρέθηκε λογαριασμός με αυτό το IBAN.',
    CustomerNotFound: 'Δεν βρέθηκε ο πελάτης.',
    UserNotFound: 'Δεν βρέθηκε ο χρήστης.',

    // Υπάρχει ήδη
    CustomerAlreadyExists: 'Υπάρχει ήδη πελάτης με αυτό το ΑΦΜ.',
    EmailAlreadyExists: 'Υπάρχει ήδη πελάτης με αυτό το email.',
    IdNumberAlreadyExists: 'Υπάρχει ήδη πελάτης με αυτόν τον αριθμό ταυτότητας.',
    UsernameAlreadyExists: 'Το όνομα χρήστη χρησιμοποιείται ήδη.',
    UserAlreadyExists: 'Ο χρήστης υπάρχει ήδη.',
    AccountAlreadyExists: 'Πρόβλημα κατά τη δημιουργία αριθμού λογαριασμού. Δοκιμάστε ξανά.',

    // Χρήματα
    AccountInsufficientBalance: 'Το υπόλοιπο δεν επαρκεί για αυτή τη συναλλαγή.',
    AccountNegativeAmount: 'Το ποσό δεν μπορεί να είναι αρνητικό.',

    // Μη έγκυρα στοιχεία
    RegionInvalidArgument: 'Μη έγκυρη περιφέρεια.',
    RoleInvalidArgument: 'Μη έγκυρος ρόλος χρήστη.',
    UserInvalidArgument: 'Μη έγκυρα στοιχεία χρήστη.',
    CustomerIdFileFileUploadError: 'Η μεταφόρτωση του δελτίου ταυτότητας απέτυχε.',

    // Σύνδεση. Ένα κοινό μήνυμα για λάθος όνομα και για λάθος κωδικό — αν λέγαμε
    // «δεν υπάρχει τέτοιος χρήστης», η φόρμα θα επιβεβαίωνε σε οποιονδήποτε ποια
    // ονόματα χρήστη υπάρχουν στην τράπεζα.
    INVALID_CREDENTIALS: 'Λάθος όνομα χρήστη ή κωδικός.',
    ACCOUNT_DISABLED: 'Ο λογαριασμός σας είναι απενεργοποιημένος. Επικοινωνήστε με την τράπεζα.',
    ACCOUNT_LOCKED: 'Ο λογαριασμός σας έχει κλειδωθεί. Επικοινωνήστε με την τράπεζα.',
    ACCOUNT_EXPIRED: 'Ο λογαριασμός σας έχει λήξει. Επικοινωνήστε με την τράπεζα.',
    CREDENTIALS_EXPIRED: 'Ο κωδικός σας έχει λήξει. Πρέπει να τον αλλάξετε.',
    AUTHENTICATION_ERROR: 'Η σύνδεση απέτυχε. Δοκιμάστε ξανά.',
    UNAUTHORIZED: 'Η σύνδεσή σας έληξε. Συνδεθείτε ξανά.',

    // Γενικά
    ACCESS_DENIED: 'Δεν έχετε δικαίωμα για αυτή την ενέργεια.',
    DATABASE_ERROR: 'Πρόβλημα στη βάση δεδομένων. Δοκιμάστε ξανά σε λίγο.',
    INTERNAL_SERVER_ERROR: 'Κάτι πήγε στραβά στον διακομιστή. Δοκιμάστε ξανά σε λίγο.',
};

/** Ονόματα πεδίων του backend → ελληνική ετικέτα, για τα validation σφάλματα. */
const FIELD_LABELS = {
    firstname: 'Όνομα', lastname: 'Επώνυμο', vat: 'ΑΦΜ', email: 'Email', phone: 'Τηλέφωνο',
    regionId: 'Περιφέρεια', username: 'Όνομα χρήστη', password: 'Κωδικός',
    idNumber: 'Αριθμός ταυτότητας', placeOfBirth: 'Τόπος γέννησης',
    municipalityOfRegistration: 'Δήμος εγγραφής', dateOfBirth: 'Ημερομηνία γέννησης',
    homeAddress: 'Διεύθυνση', gender: 'Φύλο', accountType: 'Τύπος λογαριασμού',
    initialDeposit: 'Αρχικό ποσό', amount: 'Ποσό', iban: 'IBAN',
    myIban: 'IBAN αποστολέα', toIban: 'IBAN παραλήπτη', customerUuid: 'Πελάτης',
};

/** Τα τυπικά μηνύματα του Bean Validation, που έρχονται αμετάφραστα. */
function translateFieldError(message) {
    if (/must not be (null|blank|empty)/i.test(message)) return 'είναι υποχρεωτικό';
    if (/^must match/i.test(message)) return 'δεν έχει σωστή μορφή';
    if (/size must be between (\d+) and (\d+)/i.test(message)) {
        const [, min, max] = message.match(/size must be between (\d+) and (\d+)/i);
        return `πρέπει να έχει ${min}-${max} χαρακτήρες`;
    }
    if (/same account/i.test(message)) return 'δεν μπορεί να είναι ο ίδιος λογαριασμός';
    if (/greater than zero/i.test(message)) return 'πρέπει να είναι μεγαλύτερο από μηδέν';
    return message;   // custom μηνύματα (π.χ. του password) είναι ήδη ελληνικά
}

/**
 * @param err          το σφάλμα του axios
 * @param overrides    προαιρετικά μηνύματα ανά code, για το συγκεκριμένο context.
 *                     Π.χ. στο IRIS ψάχνουμε με τηλέφωνο, οπότε το `CustomerNotFound`
 *                     διαβάζεται καλύτερα ως «δεν βρέθηκε λογαριασμός με αυτό το τηλέφωνο»
 *                     παρά ως το γενικό «δεν βρέθηκε ο πελάτης».
 */
export function getErrorMessage(err, overrides = {}) {
    if (!err.response) return 'Δεν υπάρχει σύνδεση με τον διακομιστή. Ελέγξτε το δίκτυό σας.';

    const data = err.response.data;

    // Validation ανά πεδίο: "ΑΦΜ: δεν έχει σωστή μορφή, Τηλέφωνο: είναι υποχρεωτικό"
    if (data?.errors) {
        return Object.entries(data.errors)
            .map(([field, msg]) => `${FIELD_LABELS[field] ?? field}: ${translateFieldError(msg)}`)
            .join(', ');
    }

    if (data?.code && overrides[data.code]) return overrides[data.code];
    if (data?.code && CODE_MESSAGES[data.code]) return CODE_MESSAGES[data.code];

    // Μόνο για 401 χωρίς αναγνωρίσιμο code. Τα κανονικά 401 της σύνδεσης έχουν
    // δικό τους code παραπάνω και δεν φτάνουν ποτέ εδώ — αλλιώς η φόρμα εισόδου
    // θα έλεγε «η σύνδεσή σας έληξε» σε κάποιον που μόλις πληκτρολόγησε λάθος κωδικό.
    if (err.response.status === 401) return 'Η σύνδεσή σας έληξε. Συνδεθείτε ξανά.';
    if (err.response.status === 403) return 'Δεν έχετε δικαίωμα για αυτή την ενέργεια.';

    // Άγνωστο code: δείξε ό,τι έστειλε ο server, αλλιώς γενικό μήνυμα
    return data?.description || data?.message || 'Η ενέργεια απέτυχε. Δοκιμάστε ξανά.';
}
