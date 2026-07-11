import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "export",          // Required for GitHub Pages static export
  trailingSlash: true,       // Ensures proper routing on Pages
  images: {
    unoptimized: true,       // Required when output: 'export' (no server)
  },
};

export default nextConfig;
