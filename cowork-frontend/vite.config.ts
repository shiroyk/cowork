import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import monacoEditorPlugin from "vite-plugin-monaco-editor";

export default defineConfig({
  server: {
    https: false,
    host: "0.0.0.0",
    port: 1122,
    cors: true,
    proxy: {
      "/api": {
        target: "http://api.cowork.local",
        rewrite: (path) => path.replace(/^\/api/, ""),
        changeOrigin: true,
        secure: false,
        ws: true,
      },
    },
  },
  plugins: [react(), (monacoEditorPlugin as any).default({})],
});
