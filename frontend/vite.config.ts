import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import svgr from "vite-plugin-svgr";

export default defineConfig({
  plugins: [react(), svgr()],
  test: {
    environment: "jsdom",
    setupFiles: "./src/test/setupTests.ts",
    globals: true,
    css: true,
  },
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