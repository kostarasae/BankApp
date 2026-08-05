export default function Card({ children, className = '' }) {
    return (
        <section className={`card bg-white p-5 mb-4 rounded-[14px] border border-marble ${className}`}>
            {children}
        </section>
    );
}