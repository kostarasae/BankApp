export default function Footer({ className = '' }) {
    return (
        <footer className={`bg-primary text-[#eaf0f0] text-center p-6 mt-8 ${className}`}>
            <figure className="inline-flex items-center gap-3 m-0">
                <img src="/bank_logo.png" alt="KostaBank logo" className="w-12 opacity-90" />
                <h1 className="text-lg font-bold">KostaBank</h1>
            </figure>
        </footer>
    );
}
