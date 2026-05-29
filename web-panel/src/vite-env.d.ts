/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_SUPABASE_URL: string
  readonly VITE_SUPABASE_ANON_KEY: string
  readonly VITE_S3_ACCESS_KEY: string
  readonly VITE_S3_SECRET_KEY: string
  readonly VITE_S3_BUCKET_NAME: string
  readonly VITE_S3_ENDPOINT_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}