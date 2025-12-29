/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_GATEWAY_URL?: string;
  readonly VITE_API_TIMEOUT?: string;
  readonly VITE_USE_MOCK?: string;
  // add other `VITE_` env vars here as needed
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
