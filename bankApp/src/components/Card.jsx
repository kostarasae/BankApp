// Το hover shadow και το 3D transform ορίζονται στην κλάση `.card` (index.css),
// portαρισμένα από το vanilla — όχι με Tailwind utilities, γιατί το vanilla shadow
// είναι εντονότερο από το `shadow-2xl` και το transition είναι 1.5s cubic-bezier.
export default function Card({ children, className = '' }) {
    return (
        <section className={`card bg-white p-5 mb-[2%] rounded-2xl ${className}`}>
            {children}
        </section>
    );
}