import { useState } from 'react';
import Button from './Button';
import StatusMessage from './StatusMessage';

/**
 * A destructive action that cannot be undone from the interface, so it asks the
 * user to type a value they can see on screen rather than accepting a single click.
 */
export default function DangerZone({ title, description, confirmLabel, confirmValue, actionLabel, onConfirm }) {
    const [open, setOpen] = useState(false);
    const [typed, setTyped] = useState('');
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({ text: '', ok: false });

    const matches = typed.trim().toLowerCase() === String(confirmValue ?? '').trim().toLowerCase();

    async function handleConfirm() {
        setLoading(true);
        setStatus({ text: '', ok: false });
        try {
            await onConfirm();
        } catch (err) {
            setStatus({ text: err?.userMessage ?? 'Η ενέργεια απέτυχε', ok: false });
        } finally {
            setLoading(false);
        }
    }

    return (
        <fieldset className="flex flex-col rounded-lg p-4 border border-red-300 bg-red-50/40 mt-4">
            <legend className="font-bold text-sm text-red-700 px-1.5">{title}</legend>

            <p className="text-sm text-muted mb-3">{description}</p>

            {!open ? (
                <button type="button" onClick={() => setOpen(true)}
                    className="self-start px-4 py-2 rounded-full border border-red-600 text-red-700
                        font-bold text-sm cursor-pointer transition hover:bg-red-600 hover:text-white">
                    {actionLabel}
                </button>
            ) : (
                <>
                    <label className="font-bold text-sm mb-[3px]">
                        {confirmLabel} <span className="text-red-700">{confirmValue}</span>
                    </label>
                    <input value={typed} onChange={e => setTyped(e.target.value)} autoFocus
                        className="p-3 text-base border border-gray-300 rounded h-12 box-border" />

                    <div className="flex gap-2 mt-3">
                        <Button type="button" disabled={!matches || loading} onClick={handleConfirm}
                            className="bg-red-600 hover:bg-red-700">
                            {loading ? 'Γίνεται...' : actionLabel}
                        </Button>
                        <button type="button" onClick={() => { setOpen(false); setTyped(''); setStatus({ text: '', ok: false }); }}
                            className="px-4 py-2 rounded-full border border-gray-400 text-ink
                                font-bold text-sm cursor-pointer">
                            Άκυρο
                        </button>
                    </div>
                    <StatusMessage status={status} className="mt-2" />
                </>
            )}
        </fieldset>
    );
}
