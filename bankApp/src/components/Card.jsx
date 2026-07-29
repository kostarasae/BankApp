export default function Card({ children, className = '' }) {
    return (
        <section className={`card bg-white p-5 mb-[2%] mx-2.5 md:ml-[4%] md:mr-0 rounded-2xl hover:shadow-2xl ${className}`}>
            {children}
        </section>
    );
}