describe('WealthWise AI - Authentication and Onboarding E2E Tests', () => {
    
    it('should verify Splash Screen navigation and onboarding redirection', async () => {
        // Wait for Splash screen elements to be visible
        const splashLogo = await $('//*[@content-desc="WealthWise Logo"]');
        expect(await splashLogo.isDisplayed()).toBe(true);

        // Wait for animation to finish and navigate to Onboarding or Login
        // Splash animation usually takes 2-3 seconds, so we wait
        await driver.pause(3000);

        // Check if Onboarding or Login screen is displayed
        const onboardingTitle = await $('//*[@text="Welcome to WealthWise AI"]');
        const loginTitle = await $('//*[@text="Log In"]');

        const isOnboardingVisible = await onboardingTitle.isDisplayed().catch(() => false);
        const isLoginVisible = await loginTitle.isDisplayed().catch(() => false);

        expect(isOnboardingVisible || isLoginVisible).toBe(true);
    });

    it('should verify Onboarding Screen user profile inputs', async () => {
        // Navigate to Onboarding screen if not already there
        const onboardingHeader = await $('//*[@text="Tell Us About Yourself"]');
        if (await onboardingHeader.isDisplayed()) {
            // Fill Monthly Income
            const incomeInput = await $('//*[@content-desc="Monthly Income Input"]');
            await incomeInput.setValue('5000');

            // Fill Savings Target
            const savingsInput = await $('//*[@content-desc="Savings Goal Input"]');
            await savingsInput.setValue('1500');

            // Select Risk Tolerance Profile (using dropdown / selection tags)
            const riskDropdown = await $('//*[@content-desc="Risk Tolerance Dropdown"]');
            await riskDropdown.click();
            
            const moderateOption = await $('//*[@text="Moderate"]');
            await moderateOption.click();

            // Submit Onboarding details
            const completeButton = await $('//*[@content-desc="Complete Onboarding Button"]');
            await completeButton.click();

            // Verify navigation to Dashboard Screen
            const dashboardTitle = await $('//*[@text="Dashboard"]');
            expect(await dashboardTitle.isDisplayed()).toBe(true);
        } else {
            console.log('User is already onboarded or logged in, skipping onboarding inputs.');
        }
    });

    it('should verify Signup Screen validation and submission flows', async () => {
        // Assuming we start at Login screen and click to sign up
        const signUpLink = await $('//*[@content-desc="Sign Up Link"]');
        if (await signUpLink.isDisplayed()) {
            await signUpLink.click();

            const signupTitle = await $('//*[@text="Create Account"]');
            expect(await signupTitle.isDisplayed()).toBe(true);

            // Trigger Validation Error: Empty fields
            const registerBtn = await $('//*[@content-desc="Register Button"]');
            await registerBtn.click();

            const validationError = await $('//*[@text="All fields are required"]');
            expect(await validationError.isDisplayed()).toBe(true);

            // Fill details
            const emailInput = await $('//*[@content-desc="Email Input Field"]');
            await emailInput.setValue('newuser@wealthwise.com');

            const passwordInput = await $('//*[@content-desc="Password Input Field"]');
            await passwordInput.setValue('SecurePassword123');

            const confirmPasswordInput = await $('//*[@content-desc="Confirm Password Input Field"]');
            await confirmPasswordInput.setValue('DifferentPassword'); // mismatched

            await registerBtn.click();

            const mismatchError = await $('//*[@text="Passwords do not match"]');
            expect(await mismatchError.isDisplayed()).toBe(true);

            // Fix mismatched password
            await confirmPasswordInput.setValue('SecurePassword123');
            await registerBtn.click();

            // Successful signup should transition to Onboarding
            const onboardingTitle = await $('//*[@text="Tell Us About Yourself"]');
            expect(await onboardingTitle.isDisplayed()).toBe(true);
        }
    });

    it('should verify Login Screen error checking and successful log in', async () => {
        // Navigate to login screen
        const loginLink = await $('//*[@content-desc="Navigate to Login Link"]');
        if (await loginLink.isDisplayed()) {
            await loginLink.click();
        }

        const loginTitle = await $('//*[@text="Log In"]');
        expect(await loginTitle.isDisplayed()).toBe(true);

        const emailInput = await $('//*[@content-desc="Login Email Input"]');
        const passwordInput = await $('//*[@content-desc="Login Password Input"]');
        const loginBtn = await $('//*[@content-desc="Login Action Button"]');

        // Case 1: Wrong credentials
        await emailInput.setValue('user@example.com');
        await passwordInput.setValue('wrongpassword');
        await loginBtn.click();

        const credentialsError = await $('//*[@text="Invalid email or password"]');
        expect(await credentialsError.isDisplayed()).toBe(true);

        // Case 2: Successful log in
        await emailInput.setValue('testuser@wealthwise.com');
        await passwordInput.setValue('password123');
        await loginBtn.click();

        // Successful login should redirect to Dashboard Screen
        const dashboardTitle = await $('//*[@text="Dashboard"]');
        expect(await dashboardTitle.isDisplayed()).toBe(true);
    });

    it('should verify session persistence on app relaunch', async () => {
        // Close app and relaunch it
        await driver.terminateApp('com.company.finanicaltracker.wealthai');
        await driver.activateApp('com.company.finanicaltracker.wealthai');

        // Verify session persists (user should bypass login and land directly on Dashboard)
        const dashboardTitle = await $('//*[@text="Dashboard"]');
        expect(await dashboardTitle.isDisplayed()).toBe(true);
    });
});
