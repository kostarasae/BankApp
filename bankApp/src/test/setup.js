// Adds the DOM matchers (toBeInTheDocument, toBeDisabled, ...) to expect,
// and clears the rendered tree between tests so they cannot leak into each other.
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

afterEach(() => {
    cleanup();
});