import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import AccountCard from './AccountCard';

// Data, not a function: a plain object standing in for the account we hand to
// the component. The field names are the ones AccountCard reads off the prop.
const account = {
    iban: 'GR1600000000000000000000001',
    accountNumber: 'ACC-MARIA-CHK',
    balance: 1350,
    accountType: 'CHECKING',
};

// The component also wants two callbacks. What they do is irrelevant here;
// they just must not be undefined if something gets clicked.
const noop = () => {};

describe('AccountCard', () => {

    it('shows the account type in Greek, never the raw enum', () => {
        render(<AccountCard account={account} onSelect={noop} onOpenHistory={noop} />);

        // The user sees «Τρεχούμενος», never the 'CHECKING' stored in the database.
        expect(screen.getByText('Τρεχούμενος')).toBeInTheDocument();
    });

    it('shows the balance formatted, not as a bare number', () => {
        render(<AccountCard account={account} onSelect={noop} onOpenHistory={noop} />);

        // Note: the space before the euro sign is non-breaking (U+00A0) — that is what Intl emits.
        expect(screen.getByText('1.350,00 €')).toBeInTheDocument();
        // And explicitly: the bare number leaks nowhere.
        expect(screen.queryByText('1350')).not.toBeInTheDocument();
    });

    it('offers a button to open the statement', () => {
        render(<AccountCard account={account} onSelect={noop} onOpenHistory={noop} />);

        // Queried by role and name — the same way a screen-reader user finds it.
        // The name is needed because the card itself is a <button> too.
        expect(screen.getByRole('button', { name: 'Κινήσεις' })).toBeInTheDocument();
    });

    it('hides closing the account from anyone but an administrator', () => {
        render(<AccountCard account={account} onSelect={noop} onOpenHistory={noop} />);

        // queryBy... returns null instead of throwing, so it is the only one that can
        // assert absence. getBy... would throw before reaching the expect.
        expect(screen.queryByText('Κλείσιμο λογαριασμού')).not.toBeInTheDocument();
    });

    it('shows it to an administrator', () => {
        render(<AccountCard account={account} onSelect={noop} onOpenHistory={noop} isAdmin />);

        expect(screen.getByText('Κλείσιμο λογαριασμού')).toBeInTheDocument();
    });
});
