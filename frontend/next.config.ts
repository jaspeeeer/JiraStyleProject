import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  // Pin the file-tracing root to this app so an unrelated lockfile elsewhere on the
  // machine can't be inferred as the workspace root.
  outputFileTracingRoot: __dirname,
};

export default nextConfig;
