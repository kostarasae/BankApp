export default function Header({ className = '' }) {
    return (
        <header className={`flex flex-col items-center justify-center bg-primary text-[#eaf0f0] p-5 ${className}`}>
            <img className="w-20 mx-auto" src="/bank_logo.png" alt="KostaBank" />
            <h1 className="text-lg font-bold mt-2">KostaBank</h1>
        </header>
    );
}