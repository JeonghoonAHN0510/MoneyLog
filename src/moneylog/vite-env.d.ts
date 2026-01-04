interface ImportMetaEnv {
    // .env에 추가된 환경변수 목록
    readonly VITE_API_BASE_URL: string;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}