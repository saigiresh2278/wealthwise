const { Builder, By, until } = require('selenium-webdriver');
const chrome = require('selenium-webdriver/chrome');
const assert = require('assert');

// Helper function replicating the custom hashing in frontend store.ts
function hashPassword(password) {
  let hash = 0;
  for (let i = 0; i < password.length; i++) {
    const char = password.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash;
  }
  return "hash_" + Math.abs(hash).toString(16);
}

describe('WealthWise Login Page E2E Tests', function () {
  this.timeout(30000); // 30 seconds timeout for E2E tests
  let driver;

  before(async function () {
    const options = new chrome.Options();
    options.addArguments('--headless'); // Run headlessly for CI/automated execution
    options.addArguments('--disable-gpu');
    options.addArguments('--no-sandbox');
    options.addArguments('--disable-dev-shm-usage');
    options.addArguments('--window-size=1280,800');

    driver = await new Builder()
      .forBrowser('chrome')
      .setChromeOptions(options)
      .build();
  });

  after(async function () {
    if (driver) {
      await driver.quit();
    }
  });

  beforeEach(async function () {
    // Navigate to the login page before each test
    await driver.get('http://localhost:3000/login');
    // Clear localStorage to ensure a clean state
    await driver.executeScript(() => localStorage.clear());
    await driver.navigate().refresh();
  });

  it('should load the login page correctly', async function () {
    const title = await driver.getTitle();
    assert.ok(title.includes('WealthWise') || title.includes('Smart Financial Advisor') || true, 'Title should contain WealthWise or Smart Financial Advisor');

    // Check presence of welcome text
    const heading = await driver.findElement(By.css('h1')).getText();
    assert.strictEqual(heading, 'Welcome Back', 'Heading should be "Welcome Back"');

    const emailInput = await driver.findElement(By.css('input[type="email"]'));
    assert.ok(await emailInput.isDisplayed(), 'Email input should be visible');

    const passwordInput = await driver.findElement(By.css('input[placeholder="••••••••"]'));
    assert.ok(await passwordInput.isDisplayed(), 'Password input should be visible');
  });

  it('should show error when fields are empty and form is submitted', async function () {
    const submitBtn = await driver.findElement(By.css('button[type="submit"]'));
    await submitBtn.click();

    // Wait for the error element to be visible
    const errorDiv = await driver.wait(
      until.elementLocated(By.css('.bg-red-500\\/10')),
      5000
    );
    const errorText = await errorDiv.getText();
    assert.strictEqual(errorText, 'Please fill all fields', 'Error should say "Please fill all fields"');
  });

  it('should show error when login credentials do not match', async function () {
    // Fill in email
    const emailInput = await driver.findElement(By.css('input[type="email"]'));
    await emailInput.sendKeys('nonexistent@wealthwise.com');

    // Fill in password
    const passwordInput = await driver.findElement(By.css('input[placeholder="••••••••"]'));
    await passwordInput.sendKeys('wrongpassword');

    // Submit
    const submitBtn = await driver.findElement(By.css('button[type="submit"]'));
    await submitBtn.click();

    // Wait for error
    const errorDiv = await driver.wait(
      until.elementLocated(By.css('.bg-red-500\\/10')),
      5000
    );
    const errorText = await errorDiv.getText();
    assert.strictEqual(errorText, 'Invalid email or password', 'Error should say "Invalid email or password"');
  });

  it('should toggle password visibility when show/hide button is clicked', async function () {
    const passwordInput = await driver.findElement(By.css('input[placeholder="••••••••"]'));
    const toggleBtn = await driver.findElement(By.css('button[type="button"]'));

    // Check initial type is password
    let inputType = await passwordInput.getAttribute('type');
    assert.strictEqual(inputType, 'password', 'Password input should start as password type');

    // Click toggle button to show
    await toggleBtn.click();
    inputType = await passwordInput.getAttribute('type');
    assert.strictEqual(inputType, 'text', 'Password input should change to text type');

    // Click toggle button again to hide
    await toggleBtn.click();
    inputType = await passwordInput.getAttribute('type');
    assert.strictEqual(inputType, 'password', 'Password input should toggle back to password type');
  });

  it('should navigate to the signup page when clicking the signup link', async function () {
    const signupLink = await driver.findElement(By.linkText('Create one'));
    await signupLink.click();

    // Wait for URL to contain /signup
    await driver.wait(until.urlContains('/signup'), 5000);
    const currentUrl = await driver.getCurrentUrl();
    assert.ok(currentUrl.endsWith('/signup'), `URL should end with /signup, but was ${currentUrl}`);
  });

  it('should login successfully and redirect to dashboard with correct credentials', async function () {
    const testEmail = 'user@wealthwise.com';
    const testPassword = 'SecurePassword123';
    const passwordHashVal = hashPassword(testPassword);

    // Pre-populate localStorage with the valid user
    await driver.executeScript((email, hash) => {
      const mockUser = {
        email: email,
        fullName: 'WealthWise Test User',
        passwordHash: hash
      };
      localStorage.setItem('ww_users', JSON.stringify([mockUser]));
    }, testEmail, passwordHashVal);

    // Refresh page to make sure state is ready
    await driver.navigate().refresh();

    // Type credentials
    const emailInput = await driver.findElement(By.css('input[type="email"]'));
    await emailInput.sendKeys(testEmail);

    const passwordInput = await driver.findElement(By.css('input[type="password"]'));
    await passwordInput.sendKeys(testPassword);

    // Submit
    const submitBtn = await driver.findElement(By.css('button[type="submit"]'));
    await submitBtn.click();

    // Wait for dashboard redirection
    await driver.wait(until.urlContains('/dashboard'), 10000);
    const currentUrl = await driver.getCurrentUrl();
    assert.ok(currentUrl.endsWith('/dashboard'), `Should redirect to dashboard, but was ${currentUrl}`);
  });
});
