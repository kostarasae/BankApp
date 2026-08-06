import { useAuth } from "../context/AuthContext";
import { useCustomerProfile } from "../hooks/useCustomerProfile";
import Card from "./Card";

const GENDER_LABELS = { MALE: 'Άνδρας', FEMALE: 'Γυναίκα' };

function Row({ label, value }) {
    return (
        <div className="grid grid-cols-[minmax(160px,260px)_1fr] gap-x-8 py-2.5
            border-b border-gray-200 last:border-b-0">
            <span className="font-bold">{label}</span>
            <span>{value || '—'}</span>
        </div>
    );
}

export default function ProfilePanel() {
    const { customerUuid } = useAuth();
    const { profile, loading, error } = useCustomerProfile(customerUuid);

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

    return (
        <Card>
            <h2 className="card-heading mb-3">Προφίλ</h2>

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
        </Card>
    );
}
