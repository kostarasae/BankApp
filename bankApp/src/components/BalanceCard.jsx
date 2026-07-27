export default function BalanceCard({ balance }) {
    return (
        <section className="bg-white p-5 mb-[2%] ml-[4%] rounded-2xl hover:shadow-2xl">
            <h2 className="text-lg font-bold text-[#1f3c88] mb-2">Υπόλοιπο</h2>
            <p className="text-2xl font-bold text-[#2e7d32] pl-5">{balance}</p>
        </section>
    );
}