import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import DangerZone from './DangerZone';

// The guard that stops an irreversible action from happening on a single click:
// the confirm button stays disabled until the user types the value shown on screen.
const props = {
    title: 'Delete customer',
    description: 'This cannot be undone from the interface.',
    confirmLabel: 'Type the surname to confirm:',
    confirmValue: 'Παπαδοπούλου',
    actionLabel: 'Delete',
};

function renderZone(onConfirm = vi.fn()) {
    render(<DangerZone {...props} onConfirm={onConfirm} />);
    return onConfirm;
}

describe('DangerZone', () => {

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('starts closed, with nothing to type into', () => {
        renderZone();

        expect(screen.getByRole('button', { name: 'Delete' })).toBeInTheDocument();
        // Nothing destructive is reachable yet — there is no field at all.
        expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
    });

    it('opens a confirmation field, but keeps the action disabled', async () => {
        const user = userEvent.setup();
        renderZone();

        await user.click(screen.getByRole('button', { name: 'Delete' }));

        expect(screen.getByRole('textbox')).toBeInTheDocument();
        // The field is there, the action is not yet allowed.
        expect(screen.getByRole('button', { name: 'Delete' })).toBeDisabled();
    });

    it('stays disabled while the typed value does not match', async () => {
        const user = userEvent.setup();
        renderZone();

        await user.click(screen.getByRole('button', { name: 'Delete' }));
        await user.type(screen.getByRole('textbox'), 'Γεωργίου');

        expect(screen.getByRole('button', { name: 'Delete' })).toBeDisabled();
    });

    it('enables the action once the value matches', async () => {
        const user = userEvent.setup();
        renderZone();

        await user.click(screen.getByRole('button', { name: 'Delete' }));
        await user.type(screen.getByRole('textbox'), 'Παπαδοπούλου');

        expect(screen.getByRole('button', { name: 'Delete' })).toBeEnabled();
    });

    it('accepts a different case, so the guard is not a spelling test', async () => {
        const user = userEvent.setup();
        renderZone();

        await user.click(screen.getByRole('button', { name: 'Delete' }));
        await user.type(screen.getByRole('textbox'), 'ΠΑΠΑΔΟΠΟΥΛΟΥ');

        expect(screen.getByRole('button', { name: 'Delete' })).toBeEnabled();
    });

    it('runs the action once when confirmed', async () => {
        const user = userEvent.setup();
        const onConfirm = renderZone();

        await user.click(screen.getByRole('button', { name: 'Delete' }));
        await user.type(screen.getByRole('textbox'), 'Παπαδοπούλου');
        await user.click(screen.getByRole('button', { name: 'Delete' }));

        expect(onConfirm).toHaveBeenCalledTimes(1);
    });

    it('never runs the action while the value is wrong', async () => {
        const user = userEvent.setup();
        const onConfirm = renderZone();

        await user.click(screen.getByRole('button', { name: 'Delete' }));
        await user.type(screen.getByRole('textbox'), 'wrong');
        // userEvent refuses to click a disabled button, which is the point.
        await user.click(screen.getByRole('button', { name: 'Delete' }));

        expect(onConfirm).not.toHaveBeenCalled();
    });

    it('closes again on cancel', async () => {
        const user = userEvent.setup();
        renderZone();

        await user.click(screen.getByRole('button', { name: 'Delete' }));
        await user.click(screen.getByRole('button', { name: 'Άκυρο' }));

        expect(screen.queryByRole('textbox')).not.toBeInTheDocument();
    });
});
