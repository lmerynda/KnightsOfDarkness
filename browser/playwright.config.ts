import { defineConfig } from "@playwright/test";

/**
 * Black-box scenarios against the real game: the Spring backend on 8080 and the Vite dev server on 3000.
 * Both are started when they are not already running. Tests drive the installed Chrome, no browser download.
 */
export default defineConfig({
  testDir: "e2e",
  outputDir: "test-results",
  timeout: 60_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  workers: 1,
  reporter: [["list"]],
  use: {
    baseURL: "http://localhost:3000",
    channel: "chrome",
    viewport: { width: 1440, height: 900 },
    trace: "retain-on-failure",
  },
  webServer: [
    {
      command: "./gradlew bootRun --console=plain",
      cwd: "../backend",
      url: "http://localhost:8080/health",
      reuseExistingServer: true,
      timeout: 180_000,
    },
    {
      command: "npm run dev",
      url: "http://localhost:3000",
      reuseExistingServer: true,
      timeout: 60_000,
    },
  ],
});
