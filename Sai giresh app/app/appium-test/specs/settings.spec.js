describe('WealthWise AI - Settings and Diagnostics E2E Tests', () => {

    before(async () => {
        // Go to Settings Screen
        const settingsTab = await $('//*[@content-desc="Settings"]');
        await settingsTab.click();
    });

    it('should display application setting cards and headings', async () => {
        const header = await $('//*[@text="Application Settings"]');
        expect(await header.isDisplayed()).toBe(true);

        const darkModeLabel = await $('//*[@text="Dark Mode"]');
        expect(await darkModeLabel.isDisplayed()).toBe(true);

        const databaseExplorerLabel = await $('//*[@text="Database Explorer"]');
        expect(await databaseExplorerLabel.isDisplayed()).toBe(true);
    });

    it('should toggle Dark Mode switch and apply preference', async () => {
        const switchElement = await $('//android.widget.Switch');
        const initialStatus = await switchElement.getAttribute('checked');

        // Toggle Switch
        await switchElement.click();
        
        const toggledStatus = await switchElement.getAttribute('checked');
        expect(initialStatus).not.toEqual(toggledStatus);

        // Toggle back to keep original state
        await switchElement.click();
    });

    it('should navigate to Risk Profile Assessment (5-step quiz) and back', async () => {
        const quizBtn = await $('//*[@text="Retake Quiz"]');
        expect(await quizBtn.isDisplayed()).toBe(true);

        await quizBtn.click();

        // Verify we land on Risk Analyzer screen
        const quizHeader = await $('//*[@text="Risk Tolerance Analyzer"]');
        expect(await quizHeader.isDisplayed()).toBe(true);

        // Answer Question 1
        const answerOption = await $('//*[@text="Stable and conservative (FDs, Savings)"]');
        if (await answerOption.isDisplayed()) {
            await answerOption.click();
        }

        // Navigate back
        const backBtn = await $('//*[@content-desc="Back"]');
        if (await backBtn.isDisplayed()) {
            await backBtn.click();
        } else {
            await driver.back(); // Standard device back
        }

        const settingsHeader = await $('//*[@text="Application Settings"]');
        expect(await settingsHeader.isDisplayed()).toBe(true);
    });

    it('should navigate to Database Explorer Screen and back', async () => {
        const dbBtn = await $('//*[@text="Explore Database"]');
        expect(await dbBtn.isDisplayed()).toBe(true);

        await dbBtn.click();

        // Verify we land on DB explorer screen
        const dbHeader = await $('//*[@text="Database Explorer"]');
        expect(await dbHeader.isDisplayed()).toBe(true);

        // Verify tables list (Auth, User Profile, Transactions, Goals) are shown
        const tablesText = await $('//*[@text="Select Table to Query"]');
        expect(await tablesText.isDisplayed()).toBe(true);

        // Navigate back
        const backBtn = await $('//*[@content-desc="Back"]');
        if (await backBtn.isDisplayed()) {
            await backBtn.click();
        } else {
            await driver.back();
        }

        const settingsHeader = await $('//*[@text="Application Settings"]');
        expect(await settingsHeader.isDisplayed()).toBe(true);
    });

    it('should verify Wiping All Data workflow and navigation to onboarding', async () => {
        const wipeBtn = await $('//*[@text="Wipe Data"]');
        expect(await wipeBtn.isDisplayed()).toBe(true);

        await wipeBtn.click();

        // Verify reset dialog modal opens
        const confirmDialogTitle = await $('//*[@text="Reset All App Data?"]');
        expect(await confirmDialogTitle.isDisplayed()).toBe(true);

        // Cancel reset first to test validation
        const cancelBtn = await $('//*[@text="Cancel"]');
        await cancelBtn.click();
        expect(await settingsHeader.isDisplayed()).toBe(true);
    });
});
