interface FormatKrwOptions {
    round?: boolean;
}

const krwNumberFormatter = new Intl.NumberFormat('ko-KR');

export const formatKrw = (amount: number, options: FormatKrwOptions = {}) => {
    const value = options.round ? Math.round(amount) : amount;
    return krwNumberFormatter.format(value);
};
