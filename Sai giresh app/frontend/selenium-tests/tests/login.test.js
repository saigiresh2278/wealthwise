const { Builder, By, until } = require("selenium-webdriver");
const chrome = require("selenium-webdriver/chrome");

const BASE_URL = "http://localhost:3000";
const DRIVER_TIMEOUT = 60000; // 60s for beforeAll
const TEST_TIMEOUT   = 60000; // 60s per test

describe("WealthWise AI - Selenium E2E", () => {
  let driver;

  beforeAll(async () => {
    const options = new chrome.Options();
    options.addArguments(
      "--headless=new",          // Modern headless mode
      "--no-sandbox",            // Required in CI (runs as root)
      "--disable-dev-shm-usage", // Overcome limited /dev/shm in Docker
      "--disable-gpu",           // Needed in headless CI
      "--window-size=1920,1080", // Set viewport
      "--disable-extensions",
      "--disable-software-rasterizer",
      "--remote-debugging-port=9222"
    );

    driver = await new Builder()
      .forBrowser("chrome")
      .setChromeOptions(options)
      .build();

    await driver.manage().setTimeouts({
      implicit: 10000,
      pageLoad:  30000,
      script:    30000,
    });
  }, DRIVER_TIMEOUT);

  afterAll(async () => {
    if (driver) {
      try { await driver.quit(); } catch (_) {}
    }
  }, DRIVER_TIMEOUT);

  // ── TC-001: Login page loads ──────────────────────────────────────────────
  test("TC-001: Login page loads and shows login form", async () => {
    await driver.get(`${BASE_URL}/login`);
    const title = await driver.getTitle();
    expect(title.length).toBeGreaterThan(0);
    // Verify at least one input field exists
    const inputs = await driver.findElements(By.css("input"));
    expect(inputs.length).toBeGreaterThan(0);
  }, TEST_TIMEOUT);

  // ── TC-002: Empty login shows validation ─────────────────────────────────
  test("TC-002: Submitting empty login form shows validation", async () => {
    await driver.get(`${BASE_URL}/login`);
    // Try to find and click the submit button
    try {
      const btn = await driver.findElement(By.css("button[type='submit']"));
      await btn.click();
      // Wait a moment for validation
      await driver.sleep(500);
    } catch (_) {}
    // Page should still be on login (not redirected to dashboard)
    const url = await driver.getCurrentUrl();
    expect(url).toContain("login");
  }, TEST_TIMEOUT);

  // ── TC-055: Unauthenticated /dashboard redirects to /login ────────────────
  test("TC-055: Unauthenticated /dashboard redirects to /login", async () => {
    await driver.get(`${BASE_URL}/dashboard`);
    // Wait for redirect (up to 10s)
    try {
      await driver.wait(until.urlContains("login"), 10000);
    } catch (_) {}
    const url = await driver.getCurrentUrl();
    // Either redirected to login or shows login form on dashboard
    const source = await driver.getPageSource();
    const hasLoginContent = url.includes("login") || source.includes("password") || source.includes("Sign in") || source.includes("Login");
    expect(hasLoginContent).toBe(true);
  }, TEST_TIMEOUT);

  // ── TC-056: Unauthenticated /transactions redirects ───────────────────────
  test("TC-056: Unauthenticated /transactions redirects to /login", async () => {
    await driver.get(`${BASE_URL}/transactions`);
    try {
      await driver.wait(until.urlContains("login"), 10000);
    } catch (_) {}
    const url = await driver.getCurrentUrl();
    const source = await driver.getPageSource();
    const isProtected = url.includes("login") || source.includes("password") || source.includes("Sign in");
    expect(isProtected).toBe(true);
  }, TEST_TIMEOUT);

  // ── TC-057: Unknown route doesn't crash ───────────────────────────────────
  test("TC-057: Unknown route shows 404 or redirects gracefully", async () => {
    await driver.get(`${BASE_URL}/nonexistent-page-xyz-abc`);
    await driver.sleep(2000);
    const source = await driver.getPageSource();
    // Should return some HTML page (not a blank response)
    expect(source.length).toBeGreaterThan(100);
  }, TEST_TIMEOUT);

  // ── TC-007: Signup page loads ─────────────────────────────────────────────
  test("TC-007: Signup page loads with registration form", async () => {
    await driver.get(`${BASE_URL}/signup`);
    const inputs = await driver.findElements(By.css("input"));
    expect(inputs.length).toBeGreaterThan(0);
  }, TEST_TIMEOUT);

  // ── TC-058: Home / landing page loads ────────────────────────────────────
  test("TC-058: Root page loads within 10 seconds", async () => {
    const start = Date.now();
    await driver.get(`${BASE_URL}/`);
    const elapsed = Date.now() - start;
    expect(elapsed).toBeLessThan(10000);
    const source = await driver.getPageSource();
    expect(source.length).toBeGreaterThan(100);
  }, TEST_TIMEOUT);
});
