/**
 * Το backend στέλνει το πραγματικό μήνυμα σφάλματος στο body, αλλά με ασυνεπές όνομα
 * πεδίου: άλλοτε `errors` (ανά πεδίο, από validators), άλλοτε `description`
 * (EntityNotFound/AlreadyExists), άλλοτε `message` (γενικά σφάλματα).
 * Το axios `err.message` είναι μόνο το γενικό "Request failed with status code 404".
 */
export function getErrorMessage(err) {
    const data = err.response?.data;
    if (data?.errors) return Object.values(data.errors).join(', ');
    return data?.description || data?.message || err.message;
}
