import { useState } from "react";
import { createCustomer, uploadIdFile, createAccount, createUser } from '../api/restBankApi';
import StatusMessage from './StatusMessage';
import { getErrorMessage } from '../utils/apiError';

const INITIAL = {
    firstname: '', lastname: '', vat: '', email: '', phone: '',
    username: '', password: '', regionId: '1',
    idNumber: '', placeOfBirth: '', municipalityOfRegistration: '',
    dateOfBirth: '', homeAddress: '', gender: '',
    accountType: 'CHECKING', initialDeposit: ''
};

const ROLE_EMPLOYEE = 2;
const ROLE_CUSTOMER = 3;
const PASSWORD_PATTERN = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=]).{8,}$/;

export default function CreateAccountForm({ isAdmin = false }) {
    const [mode, setMode] = useState('CUSTOMER');
    const [form, setForm] = useState(INITIAL);
    const [idFile, setIdFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [status, setStatus] = useState({ text: '', ok: false });

    const employee = mode === 'EMPLOYEE';

    const handleChange = e =>
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

    async function handleSubmit(e) {
        e.preventDefault();
        setStatus({ text: '', ok: false });

        // Employee: only a User (username + password), no customer / bank account
        if (employee) {
            if (form.username.trim().length < 2) {
                setStatus({ text: 'Το όνομα χρήστη πρέπει να έχει τουλάχιστον 2 χαρακτήρες', ok: false });
                return;
            }
            if (!PASSWORD_PATTERN.test(form.password)) {
                setStatus({ text: 'Ο κωδικός πρέπει να έχει τουλάχιστον 8 χαρακτήρες με 1 πεζό, 1 κεφαλαίο, 1 ψηφίο και 1 ειδικό χαρακτήρα', ok: false });
                return;
            }
            setLoading(true);
            try {
                await createUser(form.username, form.password, ROLE_EMPLOYEE);
                setStatus({ text: 'Ο υπάλληλος δημιουργήθηκε επιτυχώς!', ok: true });
                setForm(INITIAL);
            } catch (err) {
                setStatus({ text: getErrorMessage(err), ok: false });
            } finally {
                setLoading(false);
            }
            return;
        }

        if (form.firstname.trim().length < 2) {
            setStatus({ text: 'Το όνομα πρέπει να έχει τουλάχιστον 2 χαρακτήρες', ok: false });
            return;
        }
        if (form.lastname.trim().length < 2) {
            setStatus({ text: 'Το επώνυμο πρέπει να έχει τουλάχιστον 2 χαρακτήρες', ok: false });
            return;
        }
        if (!/^\d{9,}$/.test(form.vat)) {
            setStatus({ text: 'Το ΑΦΜ πρέπει να έχει τουλάχιστον 9 ψηφία', ok: false });
            return;
        }
        if (!/^[\w.-]+@[\w.-]+\.\w{2,}$/.test(form.email)) {
            setStatus({ text: 'Το email δεν έχει έγκυρη μορφή (π.χ. name@example.com)', ok: false });
            return;
        }
        if (!/^\d{10}$/.test(form.phone)) {
            setStatus({ text: 'Το τηλέφωνο πρέπει να έχει 10 ψηφία, χωρίς κωδικό χώρας (π.χ. 6912345678)', ok: false });
            return;
        }
        if (!/^[Α-ΩA-Z]{1,2}\d{6,7}$/.test(form.idNumber)) {
            setStatus({ text: 'Ο αριθμός ταυτότητας πρέπει να είναι 1-2 κεφαλαία γράμματα και 6-7 ψηφία (π.χ. ΑΒ123456)', ok: false });
            return;
        }

        setLoading(true);
        setStatus({ text: '', ok: false });
        try {
            const customerData = {
                firstname: form.firstname, lastname: form.lastname,
                vat: form.vat, email: form.email, phone: form.phone,
                regionId: Number(form.regionId),
                userInsertDTO: { username: form.username, password: form.password, roleId: ROLE_CUSTOMER },
                personalInfoInsertDTO: {
                    idNumber: form.idNumber, placeOfBirth: form.placeOfBirth,
                    municipalityOfRegistration: form.municipalityOfRegistration,
                    dateOfBirth: form.dateOfBirth, homeAddress: form.homeAddress,
                    gender: form.gender
                }
            };

            const customer = await createCustomer(customerData);
            if (idFile) await uploadIdFile(customer.uuid, idFile);
            await createAccount({
                customerUuid: customer.uuid,
                accountType: form.accountType,
                initialDeposit: Number(form.initialDeposit)
            });

            setStatus({ text: 'Ο λογαριασμός δημιουργήθηκε επιτυχώς!', ok: true });
            setForm(INITIAL);
            setIdFile(null);

        } catch (err) {
            setStatus({ text: getErrorMessage(err), ok: false });

        } finally {
            setLoading(false);
        }
    }

    return (
        <form onSubmit={handleSubmit} className="flex flex-col gap-3 p-5">
            {isAdmin && (
                <div className="flex gap-2 mb-1">
                    {[['CUSTOMER', 'Πελάτης'], ['EMPLOYEE', 'Υπάλληλος']].map(([value, label]) => (
                        <button type="button" key={value} onClick={() => { setMode(value); setStatus({ text: '', ok: false }); }}
                            className={`py-2 px-5 rounded-full border font-bold cursor-pointer ${
                                mode === value
                                    ? 'bg-[#1f3c88] text-white border-[#1f3c88]'
                                    : 'bg-white text-[#1f3c88] border-gray-300'
                            }`}>
                            {label}
                        </button>
                    ))}
                </div>
            )}

            {employee && (
                <p className="text-sm text-gray-500">
                    Δημιουργία λογαριασμού υπαλλήλου — μόνο όνομα χρήστη και κωδικός. Ο υπάλληλος βλέπει
                    λογαριασμούς πελατών αλλά δεν διαγράφει και δεν δημιουργεί άλλους υπαλλήλους.
                </p>
            )}

            {!employee && (
                <>
                    <input name="firstname" value={form.firstname} onChange={handleChange}
                        placeholder="Όνομα" className="p-3 border border-gray-300 rounded" />
                    <input name="lastname" value={form.lastname} onChange={handleChange}
                        placeholder="Επώνυμο" className="p-3 border border-gray-300 rounded" />
                    <input name="vat" value={form.vat} onChange={handleChange}
                        placeholder="ΑΦΜ" className="p-3 border border-gray-300 rounded" />
                    <input name="email" value={form.email} onChange={handleChange}
                        placeholder="Email" className="p-3 border border-gray-300 rounded" />
                    <input type="tel" name="phone" value={form.phone} onChange={handleChange}
                        placeholder="Τηλέφωνο (10 ψηφία, π.χ. 6912345678)" className="p-3 border border-gray-300 rounded" />
                </>
            )}

            <input name="username" value={form.username} onChange={handleChange}
                placeholder="Όνομα χρήστη" className="p-3 border border-gray-300 rounded" />
            <input type="password" name="password" value={form.password} onChange={handleChange}
                placeholder="Κωδικός" className="p-3 border border-gray-300 rounded" />

            {!employee && (
                <>
                    <select name="regionId" value={form.regionId} onChange={handleChange}
                        className="p-3 border border-gray-300 rounded">
                        <option value="1">Ανατολικής Μακεδονίας και Θράκης</option>
                        <option value="2">Αττικής</option>
                        <option value="3">Βορείου Αιγαίου</option>
                        <option value="4">Δυτικής Ελλάδας</option>
                        <option value="5">Δυτικής Μακεδονίας</option>
                        <option value="6">Ηπείρου</option>
                        <option value="7">Θεσσαλίας</option>
                        <option value="8">Ιονίων Νήσων</option>
                        <option value="9">Κεντρικής Μακεδονίας</option>
                        <option value="10">Κρήτης</option>
                        <option value="11">Νοτίου Αιγαίου</option>
                        <option value="12">Πελοποννήσου</option>
                        <option value="13">Στερεάς Ελλάδας</option>
                    </select>

                    <input name="idNumber" value={form.idNumber} onChange={handleChange}
                        placeholder="Αριθμός Ταυτότητας" className="p-3 border border-gray-300 rounded" />
                    <input name="placeOfBirth" value={form.placeOfBirth} onChange={handleChange}
                        placeholder="Τόπος Γέννησης" className="p-3 border border-gray-300 rounded" />
                    <input name="municipalityOfRegistration" value={form.municipalityOfRegistration} onChange={handleChange}
                        placeholder="Δήμος Εγγραφής" className="p-3 border border-gray-300 rounded" />
                    <input type="date" name="dateOfBirth" value={form.dateOfBirth} onChange={handleChange}
                        className="p-3 border border-gray-300 rounded" />
                    <input name="homeAddress" value={form.homeAddress} onChange={handleChange}
                        placeholder="Διεύθυνση" className="p-3 border border-gray-300 rounded" />

                    <div className="flex gap-4 items-center">
                        <label className="flex items-center gap-1">
                            <input type="radio" name="gender" value="MALE"
                                checked={form.gender === 'MALE'} onChange={handleChange} />
                            Άνδρας
                        </label>
                        <label className="flex items-center gap-1">
                            <input type="radio" name="gender" value="FEMALE"
                                checked={form.gender === 'FEMALE'} onChange={handleChange} />
                            Γυναίκα
                        </label>
                    </div>

                    <select name="accountType" value={form.accountType} onChange={handleChange}
                        className="p-3 border border-gray-300 rounded">
                        <option value="CHECKING">Λογαριασμός Όψεως</option>
                        <option value="SAVINGS">Λογαριασμός Ταμιευτηρίου</option>
                    </select>

                    <input type="number" name="initialDeposit" value={form.initialDeposit} onChange={handleChange}
                        placeholder="Αρχικό Ποσό" className="p-3 border border-gray-300 rounded" />

                    <input type="file" onChange={e => setIdFile(e.target.files[0])} />
                </>
            )}

            <StatusMessage status={status} />
            <button type="submit" disabled={loading}
                className="p-3 bg-[#1f3c88] text-white font-bold rounded disabled:opacity-50 cursor-pointer">
                {loading ? 'Δημιουργία...' : employee ? 'Δημιουργία υπαλλήλου' : 'Δημιουργία'}
            </button>
        </form>
    );
}
