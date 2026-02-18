import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    strictPort: true,
    proxy: {
      "/api": "http://backend:8080",
    },
    watch: {
      usePolling: true,
      interval: 200,
    },
    hmr: {
      clientPort: 5173,
    },
  },
});