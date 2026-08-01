export default function Button({ children, variant = 'primary', className = '', ...props }) {
    const base = 'p-2.5 font-bold rounded cursor-pointer transition hover:opacity-90 disabled:opacity-50';
    const variants = {
        primary: 'bg-primary text-white',
        secondary: 'bg-white text-primary border border-primary',
    };
    return (
        <button className={`${base} ${variants[variant]} ${className}`} {...props}>
            {children}
        </button>
    );
}