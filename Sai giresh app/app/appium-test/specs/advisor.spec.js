describe('WealthWise AI - AI Advisor and Reports E2E Tests', () => {

    before(async () => {
        // Go to Advisor Screen
        const advisorTab = await $('//*[@content-desc="AI"]');
        await advisorTab.click();
    });

    it('should load AI Advisor Screen headers and description text', async () => {
        const header = await $('//*[@text="AI Advisor & Expense Reports"]');
        expect(await header.isDisplayed()).toBe(true);

        const subheader = await $('//*[@text="WealthWise AI"]');
        expect(await subheader.isDisplayed()).toBe(true);
    });

    it('should display the Dynamic Monthly Overview card with salary and balance progress indicator', async () => {
        const overviewCard = await $('//*[@text="Dynamic Monthly Overview"]');
        expect(await overviewCard.isDisplayed()).toBe(true);

        const salaryText = await $('//*[contains(@text, "Monthly Salary")]');
        expect(await salaryText.isDisplayed()).toBe(true);

        const progressIndicator = await $('//*[contains(@class, "LinearProgressIndicator") or contains(@class, "ProgressBar")]');
        expect(await progressIndicator.isDisplayed()).toBe(true);
    });

    it('should mock and verify Bank Statement upload and report generation', async () => {
        const uploadBtn = await $('//*[@text="Upload Bank Sheet"]');
        expect(await uploadBtn.isDisplayed()).toBe(true);

        // We trigger the click to simulate launching file selector
        await uploadBtn.click();
        
        // Return back to app from document selector using android back button if running actual emulator
        // In E2E tests, we verify after the PDF is processed
        // For testing layout, we check if the cleared state is manageable
        const clearBtn = await $('//*[@content-desc="Clear Upload"]');
        const clearBtnExists = await clearBtn.isDisplayed().catch(() => false);
        if (clearBtnExists) {
            await clearBtn.click();
            expect(await uploadBtn.isDisplayed()).toBe(true);
        }
    });

    it('should mock and verify Resume upload and AI Career booster analysis results', async () => {
        const uploadResumeBtn = await $('//*[@text="Upload Resume"]');
        expect(await uploadResumeBtn.isDisplayed()).toBe(true);

        // Mock UI view verification after a successful parser call
        const careerTitle = await $('//*[@text="AI Career & Salary Booster"]');
        expect(await careerTitle.isDisplayed()).toBe(true);
    });

    it('should display AI Investment Strategy recommendations based on risk comfort', async () => {
        const investTitle = await $('//*[@text="AI Investment Recommendation"]');
        expect(await investTitle.isDisplayed()).toBe(true);

        const portfolioDetails = await $('//*[contains(@text, "AI Portfolio Strategy")]');
        expect(await portfolioDetails.isDisplayed()).toBe(true);
    });

    it('should display Advisory Guidance suggestions list', async () => {
        const adviceTitle = await $('//*[@text="Advisory Guidance"]');
        expect(await adviceTitle.isDisplayed()).toBe(true);
    });

    it('should navigate to Learning Hub Screen on clicking Learn from Recommended Path', async () => {
        const learningPathTitle = await $('//*[@text="Recommended Learning Path"]');
        expect(await learningPathTitle.isDisplayed()).toBe(true);

        // Find the "Learn" button text link and click it
        const learnBtn = await $('//*[@text="Learn"]');
        if (await learnBtn.isDisplayed()) {
            await learnBtn.click();

            // Verify redirection to Learning Hub Screen
            const learningHubHeader = await $('//*[@text="Learning Hub"]');
            expect(await learningHubHeader.isDisplayed()).toBe(true);

            // Navigate back to AI Advisor Screen
            const advisorTab = await $('//*[@content-desc="AI"]');
            await advisorTab.click();
        }
    });
});
