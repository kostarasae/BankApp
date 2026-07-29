export default function Button({ children, variant = 'primary', ...props }) {
    const base = 'p-2.5 font-bold rounded cursor-pointer transition hover:opacity-90';
    const variants = {
        primary: 'bg-primary text-white',
        secondary: 'bg-white text-primary border border-primary',
    };
    return (
        <button className={`${base} ${variants[variant]}`} {...props}>
            {children}
        </button>
    );
}