import { useId, useState } from 'react';
import { useAuth } from "../context/AuthContext";
import { useCustomerProfile } from "../hooks/useCustomerProfile";
import { updateCustomer, deleteCustomer } from '../api/restBankApi';
import { getErrorMessage } from '../utils/apiError';
import Card from "./Card";
import Button from './Button';
import StatusMessage from './StatusMessage';
import DangerZone from './DangerZone';

const GENDER_LABELS = { MALE: 'Άνδρας', FEMALE: 'Γυναίκα' };

const REGIONS = [
    'Ανατολικής Μακεδονίας και Θράκης', 'Αττικής', 'Βορείου Αιγαίου', 'Δυτικής Ελλάδας',
    'Δυτικής Μακεδονίας', 'Ηπείρου', 'Θεσσαλίας', 'Ιονίων Νήσων', 'Κεντρικής Μακεδονίας',
    'Κρήτης', 'Νοτίου Αιγαίου', 'Πελοποννήσου', 'Στερεάς Ελλάδας',
];

function Row({ label, value }) {
    return (
        <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 py-2.5
            border-b border-gray-200 last:border-b-0">
            <span className="font-bold">{label}</span>
            <span>{value || '—'}</span>
        </div>
    );
}

function Field({ label, name, value, onChange, type = 'text' }) {
    // Rendered once per field, so each needs its own id to tie label and input together
    const id = useId();
    return (
        <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 items-center py-1.5">
            <label htmlFor={id} className="font-bold">{label}</label>
            <input id={id} name={name} type={type} value={value ?? ''} onChange={onChange}
                className="p-2.5 text-base border border-gray-300 rounded box-border" />
        </div>
    );
}

function toForm(profile) {
    const info = profile.personalInfo ?? {};
    return {
        firstname: profile.firstname, lastname: profile.lastname,
        vat: profile.vat, email: profile.email, phone: profile.phone,
        regionId: String(profile.regionId ?? 1),
        idNumber: info.idNumber, placeOfBirth: info.placeOfBirth,
        municipalityOfRegistration: info.municipalityOfRegistration,
        dateOfBirth: info.dateOfBirth, homeAddress: info.homeAddress,
        gender: info.gender,
    };
}

export default function ProfilePanel({ customerUuid: propUuid, isStaff = false, isAdmin = false, onDeleted }) {
    const { customerUuid: ownUuid } = useAuth();
    const uuid = propUuid ?? ownUuid;
    const { profile, loading, error, reload } = useCustomerProfile(uuid);

    const [form, setForm] = useState(null);
    const [saving, setSaving] = useState(false);
    const [status, setStatus] = useState({ text: '', ok: false });

    const handleChange = e => setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));

    async function handleSave() {
        setSaving(true);
        setStatus({ text: '', ok: false });
        try {
            await updateCustomer(uuid, {
                uuid,
                firstname: form.firstname, lastname: form.lastname,
                vat: form.vat, email: form.email, phone: form.phone,
                regionId: Number(form.regionId),
                userUpdateDTO: { username: profile.username },
                personalInfoUpdateDTO: {
                    idNumber: form.idNumber, placeOfBirth: form.placeOfBirth,
                    municipalityOfRegistration: form.municipalityOfRegistration,
                    dateOfBirth: form.dateOfBirth, homeAddress: form.homeAddress,
                    gender: form.gender,
                },
            });
            await reload();
            setForm(null);
            setStatus({ text: 'Τα στοιχεία αποθηκεύτηκαν', ok: true });
        } catch (err) {
            setStatus({ text: getErrorMessage(err), ok: false });
        } finally {
            setSaving(false);
        }
    }

    if (loading) return <Card><p className="text-gray-400">Φόρτωση...</p></Card>;
    if (error) return <Card><p className="font-bold text-red-500">{error}</p></Card>;

    if (!profile) {
        return (
            <Card>
                <h2 className="card-heading mb-3">Προφίλ</h2>
                <p className="text-gray-500">Ο λογαριασμός σας δεν αντιστοιχεί σε πελάτη, οπότε δεν υπάρχουν στοιχεία προφίλ.</p>
            </Card>
        );
    }

    const info = profile.personalInfo ?? {};
    const editing = form !== null;

    return (
        <Card>
            <div className="flex flex-wrap items-baseline justify-between gap-2 mb-3">
                <h2 className="card-heading">Προφίλ</h2>
                {isStaff && !editing && (
                    <Button variant="secondary" onClick={() => setForm(toForm(profile))}>
                        Επεξεργασία
                    </Button>
                )}
            </div>

            {editing ? (
                <>
                    <fieldset className="flex flex-col rounded-lg p-4 border border-gray-300 mb-4">
                        <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Στοιχεία Κατόχου</legend>
                        <Field label="Όνομα" name="firstname" value={form.firstname} onChange={handleChange} />
                        <Field label="Επώνυμο" name="lastname" value={form.lastname} onChange={handleChange} />
                        <Field label="ΑΦΜ" name="vat" value={form.vat} onChange={handleChange} />
                        <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 items-center py-1.5">
                            <label htmlFor="profile-region" className="font-bold">Περιοχή</label>
                            <select id="profile-region" name="regionId" value={form.regionId} onChange={handleChange}
                                className="p-2.5 text-base border border-gray-300 rounded box-border">
                                {REGIONS.map((name, i) => (
                                    <option key={name} value={i + 1}>{name}</option>
                                ))}
                            </select>
                        </div>
                    </fieldset>

                    <fieldset className="flex flex-col rounded-lg p-4 border border-gray-300 mb-4">
                        <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Επικοινωνία</legend>
                        <Field label="Email" name="email" value={form.email} onChange={handleChange} />
                        <Field label="Τηλέφωνο" name="phone" value={form.phone} onChange={handleChange} />
                        <Field label="Διεύθυνση" name="homeAddress" value={form.homeAddress} onChange={handleChange} />
                    </fieldset>

                    <fieldset className="flex flex-col rounded-lg p-4 border border-gray-300">
                        <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Προσωπικά Στοιχεία</legend>
                        <Field label="Αριθμός Ταυτότητας" name="idNumber" value={form.idNumber} onChange={handleChange} />
                        <Field label="Τόπος Γέννησης" name="placeOfBirth" value={form.placeOfBirth} onChange={handleChange} />
                        <Field label="Δήμος Εγγραφής" name="municipalityOfRegistration" value={form.municipalityOfRegistration} onChange={handleChange} />
                        <Field label="Ημ. Γέννησης" name="dateOfBirth" type="date" value={form.dateOfBirth} onChange={handleChange} />
                        <div className="grid grid-cols-[minmax(200px,38%)_1fr] gap-x-8 items-center py-1.5">
                            <label htmlFor="profile-gender" className="font-bold">Φύλο</label>
                            <select id="profile-gender" name="gender" value={form.gender ?? ''} onChange={handleChange}
                                className="p-2.5 text-base border border-gray-300 rounded box-border">
                                <option value="MALE">Άνδρας</option>
                                <option value="FEMALE">Γυναίκα</option>
                            </select>
                        </div>
                    </fieldset>

                    <div className="flex gap-2 mt-4">
                        <Button onClick={handleSave} disabled={saving}>
                            {saving ? 'Αποθήκευση...' : 'Αποθήκευση'}
                        </Button>
                        <Button variant="secondary" onClick={() => { setForm(null); setStatus({ text: '', ok: false }); }}>
                            Άκυρο
                        </Button>
                    </div>
                    <StatusMessage status={status} className="mt-2" />
                </>
            ) : (
                <>
                    <fieldset className="flex flex-col rounded-lg p-4 border border-gray-300 mb-4">
                        <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Στοιχεία Κατόχου</legend>
                        <Row label="Όνομα" value={profile.firstname} />
                        <Row label="Επώνυμο" value={profile.lastname} />
                        <Row label="ΑΦΜ" value={profile.vat} />
                        <Row label="Περιοχή" value={profile.region} />
                    </fieldset>

                    <fieldset className="flex flex-col rounded-lg p-4 border border-gray-300 mb-4">
                        <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Επικοινωνία</legend>
                        <Row label="Email" value={profile.email} />
                        <Row label="Τηλέφωνο" value={profile.phone} />
                        <Row label="Διεύθυνση" value={info.homeAddress} />
                    </fieldset>

                    <fieldset className="flex flex-col rounded-lg p-4 border border-gray-300">
                        <legend className="font-bold text-sm text-[#1f3c88] px-1.5">Προσωπικά Στοιχεία</legend>
                        <Row label="Όνομα χρήστη" value={profile.username} />
                        <Row label="Αριθμός Ταυτότητας" value={info.idNumber} />
                        <Row label="Τόπος Γέννησης" value={info.placeOfBirth} />
                        <Row label="Δήμος Εγγραφής" value={info.municipalityOfRegistration} />
                        <Row label="Ημ. Γέννησης" value={info.dateOfBirth} />
                        <Row label="Φύλο" value={GENDER_LABELS[info.gender] ?? info.gender} />
                    </fieldset>

                    <StatusMessage status={status} className="mt-2" />

                    {isAdmin && propUuid && (
                        <DangerZone
                            title="Διαγραφή πελάτη"
                            description={`Ο πελάτης ${profile.firstname} ${profile.lastname} και ο λογαριασμός χρήστη του θα πάψουν να είναι προσβάσιμοι. Οι τραπεζικοί λογαριασμοί του παραμένουν — κλείσ' τους ξεχωριστά αν χρειάζεται.`}
                            confirmLabel="Για επιβεβαίωση γράψε το επώνυμο:"
                            confirmValue={profile.lastname}
                            actionLabel="Διαγραφή πελάτη"
                            onConfirm={async () => {
                                await deleteCustomer(uuid);
                                onDeleted?.();
                            }}
                        />
                    )}
                </>
            )}
        </Card>
    );
}
