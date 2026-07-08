describe('WealthWise AI - Dashboard and Navigation E2E Tests', () => {

    before(async () => {
        // Ensure user is on the dashboard
        const dashboardTitle = await $('//*[@text="Dashboard"]');
        if (!await dashboardTitle.isDisplayed()) {
            // Log in if needed
            const emailInput = await $('//*[@content-desc="Login Email Input"]');
            await emailInput.setValue('testuser@wealthwise.com');
            const passwordInput = await $('//*[@content-desc="Login Password Input"]');
            await passwordInput.setValue('password123');
            const loginBtn = await $('//*[@content-desc="Login Action Button"]');
            await loginBtn.click();
        }
    });

    it('should display essential financial metrics (Balance, Income, Expenses)', async () => {
        const totalBalanceCard = await $('//*[@content-desc="Total Balance Card"]');
        const incomeCard = await $('//*[@content-desc="Monthly Income Card"]');
        const expensesCard = await $('//*[@content-desc="Monthly Expenses Card"]');

        expect(await totalBalanceCard.isDisplayed()).toBe(true);
        expect(await incomeCard.isDisplayed()).toBe(true);
        expect(await expensesCard.isDisplayed()).toBe(true);

        const balanceText = await $('//*[@content-desc="Balance Value Text"]').getText();
        expect(balanceText).toContain('$');
    });

    it('should display recent transactions list and quick advisor tips widget', async () => {
        const transactionsHeader = await $('//*[@text="Recent Transactions"]');
        expect(await transactionsHeader.isDisplayed()).toBe(true);

        const tipsWidget = await $('//*[@content-desc="Quick Financial Tip Widget"]');
        expect(await tipsWidget.isDisplayed()).toBe(true);

        // Click next tip and verify text change
        const currentTipText = await $('//*[@content-desc="Tip Text Content"]').getText();
        const nextTipBtn = await $('//*[@content-desc="Next Tip Button"]');
        await nextTipBtn.click();

        const updatedTipText = await $('//*[@content-desc="Tip Text Content"]').getText();
        expect(currentTipText).not.toEqual(updatedTipText);
    });

    it('should verify Firestore data synchronization status indicators', async () => {
        const syncIndicator = await $('//*[@content-desc="Cloud Sync Status Indicator"]');
        expect(await syncIndicator.isDisplayed()).toBe(true);

        const syncStatusText = await syncIndicator.getAttribute('content-desc');
        expect(syncStatusText).toContain('Sync');
    });

    it('should navigate between core screens using Bottom Navigation Bar', async () => {
        // Click on Expenses Tab
        const expensesTab = await $('//*[@content-desc="Expenses"]');
        await expensesTab.click();
        
        const expenseManagerHeader = await $('//*[@text="Transactions"]');
        expect(await expenseManagerHeader.isDisplayed()).toBe(true);

        // Click on Goals Tab
        const goalsTab = await $('//*[@content-desc="Goals"]');
        await goalsTab.click();
        
        const goalPlannerHeader = await $('//*[@text="Goal Planner"]');
        expect(await goalPlannerHeader.isDisplayed()).toBe(true);

        // Click on AI Advisor Tab
        const advisorTab = await $('//*[@content-desc="AI"]');
        await advisorTab.click();
        
        const advisorHeader = await $('//*[@text="AI Financial Advisor"]');
        expect(await advisorHeader.isDisplayed()).toBe(true);

        // Return back to Home Dashboard
        const homeTab = await $('//*[@content-desc="Home"]');
        await homeTab.click();

        const dashboardTitle = await $('//*[@text="Dashboard"]');
        expect(await dashboardTitle.isDisplayed()).toBe(true);
    });
});
