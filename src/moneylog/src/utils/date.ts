const DAY_OF_WEEK_KO = ['일', '월', '화', '수', '목', '금', '토'];

export const getTodayIsoDate = () => {
    return new Date().toISOString().split('T')[0];
};

export const formatKoreanDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return `${date.getMonth() + 1}월 ${date.getDate()}일 (${DAY_OF_WEEK_KO[date.getDay()]})`;
};
