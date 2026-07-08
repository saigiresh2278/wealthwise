import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  allowedDevOrigins: ["172.23.50.202"],
  turbopack: {
    root: process.cwd(),
  },
};

export default nextConfig;


