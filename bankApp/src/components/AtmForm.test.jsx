import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import AtmForm from './AtmForm';
import { deposit } from '../api/restBankApi';

// Replaces the whole module with fakes: every export becomes a vi.fn(), so no
// request leaves the process and we can ask afterwards whether it was called.
// This is the equivalent of @Mock in Mockito.
vi.mock('../api/restBankApi');

// AtmForm needs an account to work against.
const iban = 'GR1600000000000000000000001';
const noop = () => {};

describe('AtmForm', () => {

    // Without this, one test's calls would still be counted in the next.
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('keeps the amount the user types', async () => {
        // The 'user'. Always setup() before interacting.
        const user = userEvent.setup();
        render(<AtmForm iban={iban} onSuccess={noop} />);

        // The labels here are not tied to their fields — no htmlFor, no id — so
        // getByLabelText would find nothing. A number input has the role
        // 'spinbutton', not 'textbox'.
        const amountField = screen.getByRole('spinbutton');

        // userEvent is async: without await, the expect would run before React
        // has updated the state.
        await user.type(amountField, '100');

        // On a number input toHaveValue compares against a number, not '100'.
        expect(amountField).toHaveValue(100);
    });

    it('refuses a deposit with no ATM chosen', async () => {
        const user = userEvent.setup();
        render(<AtmForm iban={iban} onSuccess={noop} />);

        // Amount filled in, ATM left empty.
        await user.type(screen.getByRole('spinbutton'), '100');
        await user.click(screen.getByRole('button', { name: 'Κατάθεση' }));

        // The message is rendered through StatusMessage.
        expect(screen.getByText('Επιλέξτε ATM')).toBeInTheDocument();
        // And the point of it: no request ever left for the backend.
        expect(deposit).not.toHaveBeenCalled();
    });

    it('refuses a deposit with no amount', async () => {
        const user = userEvent.setup();
        render(<AtmForm iban={iban} onSuccess={noop} />);

        // The other way round: an ATM is chosen but the amount is left empty.
        // selectOptions takes the option's value, not its visible text.
        await user.selectOptions(screen.getByRole('combobox'), 'Σύνταγμα');
        await user.click(screen.getByRole('button', { name: 'Κατάθεση' }));

        expect(screen.getByText('Εισάγετε έγκυρο ποσό')).toBeInTheDocument();
        expect(deposit).not.toHaveBeenCalled();
    });

    it('sends the deposit once everything is filled in', async () => {
        const user = userEvent.setup();
        // The component expects an account with a balance back.
        deposit.mockResolvedValue({ balance: 1450 });
        render(<AtmForm iban={iban} onSuccess={noop} />);

        await user.selectOptions(screen.getByRole('combobox'), 'Σύνταγμα');
        await user.type(screen.getByRole('spinbutton'), '100');
        await user.click(screen.getByRole('button', { name: 'Κατάθεση' }));

        // Here we check what it was called with — Mockito's verify(...).
        expect(deposit).toHaveBeenCalledWith(iban, 'ATM Σύνταγμα', 100);
    });
});
