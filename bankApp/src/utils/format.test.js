import { describe, it, expect } from 'vitest';
import { formatEuro, formatAmount } from './format';

// Greek formatting puts the thousands separator and the decimal comma the other way
// round from English, so these assertions use a non-breaking space before the € —
// that is the character Intl actually emits, not a plain space.
describe('formatEuro', () => {
    it('formats a thousands amount the Greek way', () => {
        expect(formatEuro(1241.5)).toBe('1.241,50 €');
    });

    it('always shows two decimals', () => {
        expect(formatEuro(5)).toBe('5,00 €');
    });

    it('accepts a numeric string, since the API sends amounts as strings', () => {
        expect(formatEuro('1350.00')).toBe('1.350,00 €');
    });

    it('keeps the minus sign on a negative amount', () => {
        expect(formatEuro(-20.5)).toBe('-20,50 €');
    });

    it('falls back to a dash instead of printing NaN', () => {
        expect(formatEuro(undefined)).toBe('—');
        expect(formatEuro('not a number')).toBe('—');
    });
});

describe('formatAmount', () => {
    it('formats without a currency symbol', () => {
        expect(formatAmount(1241.5)).toBe('1.241,50');
    });

    it('falls back to a dash on rubbish input', () => {
        expect(formatAmount(null)).toBe('0,00');
        expect(formatAmount('abc')).toBe('—');
    });
});