import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getErrorMessage } from '../utils/apiError';
import StatusMessage from '../components/StatusMessage';
import Button from '../components/Button';
import Footer from '../components/Footer';

export default function LoginPage() {
    const { login } = useAuth();
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [status, setStatus] = useState({ text: '', ok: false });
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        if (!username || !password) {
            setStatus({ text: 'Συμπληρώστε όνομα χρήστη και κωδικό', ok: false });
            return;
        }
        try {
            setLoading(true);
            setStatus({ text: '', ok: false });
            await login(username, password);
        } catch (err) {
            setStatus({ text: getErrorMessage(err), ok: false });
        } finally {
            setLoading(false);
        }
    }

    const inputCls = "p-3 text-base border border-gray-300 rounded h-12 box-border";

    return (
        <div className="min-h-screen bg-marble flex flex-col items-center justify-center gap-4 p-5">
            <section className="bg-white p-8 rounded-2xl shadow-lg w-full max-w-[400px]">
                <img className="w-20 mx-auto" src="/bank_logo.png" alt="KostaBank" />
                <h1 className="text-xl font-bold text-[#1f3c88] text-center mt-2 mb-6">KostaBank</h1>

                <form onSubmit={handleSubmit} className="flex flex-col gap-1">
                    <label className="font-bold text-sm mb-[3px]">Όνομα χρήστη</label>
                    <input value={username} onChange={e => setUsername(e.target.value)}
                        autoComplete="username" className={inputCls} />

                    <label className="font-bold text-sm mt-2.5 mb-[3px]">Κωδικός</label>
                    <input type="password" value={password} onChange={e => setPassword(e.target.value)}
                        autoComplete="current-password" className={inputCls} />

                    <Button type="submit" disabled={loading} className="mt-4 w-full">
                        {loading ? 'Σύνδεση...' : 'Σύνδεση'}
                    </Button>
                    <StatusMessage status={status} className="mt-2 text-center" />
                </form>
            </section>
            <Footer />
        </div>
    );
}
