declare module '*.css';
declare module '*.scss';
declare module '*.sass';
declare module '*.less';

declare module '*.png';
declare module '*.jpg';
declare module '*.jpeg';
declare module '*.gif';
declare module '*.webp';
declare module '*.svg';

interface ImportMetaEnv {
	readonly VITE_API_URL?: string;
}

interface ImportMeta {
	readonly env: ImportMetaEnv;
}
