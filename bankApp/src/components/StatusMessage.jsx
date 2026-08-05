export default function StatusMessage({ status, className = '' }) {
    if (!status?.text) return null;
    return (
        <p className={`font-bold ${status.ok ? 'text-green-700' : 'text-red-500'} ${className}`}>
            {status.text}
        </p>
    );
}
