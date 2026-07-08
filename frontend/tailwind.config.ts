import type { Config } from "tailwindcss";

/**
 * Design tokens for InTrack. Use these semantic colors instead of hard-coded hex values
 * (CLAUDE.md §12): a dark sidebar surface and a gold accent for primary actions.
 */
const config: Config = {
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        sidebar: {
          DEFAULT: "#0f172a", // dark slate — sidebar background
          foreground: "#e2e8f0",
          muted: "#94a3b8",
          active: "#1e293b",
        },
        accent: {
          DEFAULT: "#d4a72c", // gold — primary actions
          foreground: "#1f2937",
          hover: "#c1962233",
        },
      },
    },
  },
  plugins: [],
};

export default config;
