import { useState } from 'react';

export default function Sidebar({ children }) {
    const [open, setOpen] = useState(false);

    return (
        <>
            <button onClick={() => setOpen(o => !o)} className="p-2.5 fixed top-4 left-4 z-[1001]">☰</button>
            <aside className={`fixed top-0 left-0 w-full max-w-[20vw] h-screen bg-primary-dark text-[#eaf0f0] p-8
                box-border overflow-y-auto transition-transform duration-[800ms] ease-in-out z-[1000]
                ${open ? 'translate-x-0' : '-translate-x-full'}`}>
                {children}
            </aside>
        </>
    );
}
