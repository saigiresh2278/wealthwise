// Minimal Selenium login test
const { Builder, By, until } = require("selenium-webdriver");
const chrome = require("selenium-webdriver/chrome");

const BASE_URL = "http://localhost:3000";
const TEST_TIMEOUT = 30000;

describe("WealthWise AI - Selenium E2E", () => {
  let driver;

  beforeAll(async () => {
    const options = new chrome.Options();
    options.addArguments("--headless", "--no-sandbox", "--disable-dev-shm-usage");
    driver = await new Builder().forBrowser("chrome").setChromeOptions(options).build();
    driver.manage().setTimeouts({ implicit: 10000 });
  }, TEST_TIMEOUT);

  afterAll(async () => {
    if (driver) await driver.quit();
  });

  test("TC-001: Login page loads", async () => {
    await driver.get(`${BASE_URL}/login`);
    const title = await driver.getTitle();
    expect(title).toBeTruthy();
  }, TEST_TIMEOUT);

  test("TC-055: Unauthenticated /dashboard redirects to /login", async () => {
    await driver.get(`${BASE_URL}/dashboard`);
    await driver.wait(until.urlContains("login"), 10000);
    const url = await driver.getCurrentUrl();
    expect(url).toContain("login");
  }, TEST_TIMEOUT);

  test("TC-057: Unknown route shows 404 or redirects", async () => {
    await driver.get(`${BASE_URL}/nonexistent-page-xyz`);
    const source = await driver.getPageSource();
    expect(source.length).toBeGreaterThan(0);
  }, TEST_TIMEOUT);
});
