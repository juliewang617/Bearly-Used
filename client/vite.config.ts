import react from "@vitejs/plugin-react";
import { defineConfig, loadEnv } from "vite";

export default defineConfig(({mode}) => {
  const env = loadEnv(mode, process.cwd(), "");
  return {
    build: {
      outDir: "build",
    },
    plugins: [react()],
    server: {
      port: 8000,
    },
    test: {
      exclude: ["**/e2e/**", "**/node_modules/**"],
    },
  };
});
