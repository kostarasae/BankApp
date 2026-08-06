import { useEffect, useRef, useState } from 'react';
import Icon from './Icon';

const GREETING = { from: 'bot', text: 'Γεια σας! Πώς μπορώ να βοηθήσω;' };

const REPLIES = [
    'Ευχαριστώ. Ένας συνεργάτης θα σας απαντήσει σύντομα.',
    'Καταγράφηκε το αίτημά σας.',
    'Μπορείτε να δείτε τις κινήσεις σας από το μενού «Ιστορικό».',
    'Για συναλλαγές, δοκιμάστε το μενού «Πληρωμές» ή «IRIS».',
];

export default function ChatWidget() {
    const [open, setOpen] = useState(false);
    const [messages, setMessages] = useState([GREETING]);
    const [draft, setDraft] = useState('');
    const endRef = useRef(null);

    useEffect(() => {
        if (open) endRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages, open]);

    function handleSubmit(e) {
        e.preventDefault();
        const text = draft.trim();
        if (!text) return;

        const replyIndex = messages.filter(m => m.from === 'me').length % REPLIES.length;
        setMessages(prev => [...prev, { from: 'me', text }]);
        setDraft('');
        setTimeout(() => {
            setMessages(prev => [...prev, { from: 'bot', text: REPLIES[replyIndex] }]);
        }, 700);
    }

    return (
        <div className="fixed bottom-5 right-5 z-[500] flex flex-col items-end gap-3">
            {open && (
                <section className="w-[300px] max-w-[calc(100vw-2.5rem)] bg-white rounded-[14px]
                    border border-marble shadow-[0_10px_30px_rgba(3,42,87,0.18)] overflow-hidden">
                    <header className="flex items-center justify-between bg-primary text-white px-4 py-3">
                        <span className="font-bold text-sm">Υποστήριξη KostaBank</span>
                        <button type="button" onClick={() => setOpen(false)} aria-label="Κλείσιμο"
                            className="cursor-pointer opacity-80 hover:opacity-100">
                            <Icon name="close" className="w-4 h-4" />
                        </button>
                    </header>

                    <div className="h-[260px] overflow-y-auto p-3 flex flex-col gap-2 bg-marble/40">
                        {messages.map((m, i) => (
                            <p key={i}
                                className={`max-w-[85%] px-3 py-2 rounded-2xl text-sm leading-snug ${
                                    m.from === 'me'
                                        ? 'self-end bg-primary text-white rounded-br-sm'
                                        : 'self-start bg-white border border-marble text-ink rounded-bl-sm'
                                }`}>
                                {m.text}
                            </p>
                        ))}
                        <span ref={endRef} />
                    </div>

                    <form onSubmit={handleSubmit} className="flex items-center gap-2 border-t border-marble p-2">
                        <input value={draft} onChange={e => setDraft(e.target.value)}
                            placeholder="Γράψτε το μήνυμά σας..."
                            aria-label="Μήνυμα"
                            className="grow min-w-0 px-3 py-2 text-sm border border-gray-300 rounded-full" />
                        <button type="submit" aria-label="Αποστολή"
                            className="shrink-0 w-9 h-9 grid place-items-center rounded-full
                                bg-primary text-white cursor-pointer transition hover:opacity-90">
                            <Icon name="send" className="w-4 h-4" />
                        </button>
                    </form>
                </section>
            )}

            <button type="button" onClick={() => setOpen(o => !o)}
                aria-label={open ? 'Κλείσιμο συνομιλίας' : 'Άνοιγμα συνομιλίας'}
                className="w-[76px] h-[76px] grid place-items-center rounded-full bg-primary-dark text-white
                    shadow-[0_6px_18px_rgba(3,42,87,0.4)] cursor-pointer transition hover:scale-105">
                <Icon name={open ? 'close' : 'chat'} className="w-[42px] h-[42px]" />
            </button>
        </div>
    );
}
