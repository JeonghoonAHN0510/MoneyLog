/// <reference types="vite/client" />

interface ImportMetaEnv {
    // .env에 추가된 환경변수 목록
    readonly VITE_API_BASE_URL: string;
    readonly DEV: boolean;
}

interface ImportMeta {
    readonly env: ImportMetaEnv;
}
