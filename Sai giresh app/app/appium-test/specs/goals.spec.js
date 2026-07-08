describe('WealthWise AI - Goal Planner E2E Tests', () => {

    before(async () => {
        // Navigate to Goals Screen
        const goalsTab = await $('//*[@content-desc="Goals"]');
        await goalsTab.click();
    });

    it('should load Goal Planner Screen UI elements', async () => {
        const header = await $('//*[@text="Goal Planner"]');
        expect(await header.isDisplayed()).toBe(true);

        const addGoalBtn = await $('//*[@content-desc="Create New Goal Button"]');
        expect(await addGoalBtn.isDisplayed()).toBe(true);
    });

    it('should validate form fields on new goal dialog creation', async () => {
        const addGoalBtn = await $('//*[@content-desc="Create New Goal Button"]');
        await addGoalBtn.click();

        const saveBtn = await $('//*[@content-desc="Save Goal Button"]');
        await saveBtn.click();

        // Check validation error text
        const errorText = await $('//*[@text="Goal name and target amount are required"]');
        expect(await errorText.isDisplayed()).toBe(true);

        const cancelBtn = await $('//*[@content-desc="Cancel Goal Form Button"]');
        await cancelBtn.click();
    });

    it('should successfully create a new financial goal', async () => {
        const addGoalBtn = await $('//*[@content-desc="Create New Goal Button"]');
        await addGoalBtn.click();

        const nameInput = await $('//*[@content-desc="Goal Name Input"]');
        await nameInput.setValue('New Laptop');

        const targetInput = await $('//*[@content-desc="Goal Target Amount Input"]');
        await targetInput.setValue('1200.00');

        const startingInput = await $('//*[@content-desc="Goal Saved Amount Input"]');
        await startingInput.setValue('300.00'); // 25% completed

        const dateInput = await $('//*[@content-desc="Goal Target Date Input"]');
        await dateInput.setValue('2026-12-31');

        const saveBtn = await $('//*[@content-desc="Save Goal Button"]');
        await saveBtn.click();

        // Verify goal card exists
        const goalCard = await $('//*[@text="New Laptop"]');
        expect(await goalCard.isDisplayed()).toBe(true);

        // Verify starting percentage progress display (300 / 1200 = 25%)
        const progressPercentage = await $('//*[@text="25%"]');
        expect(await progressPercentage.isDisplayed()).toBe(true);
    });

    it('should support adding savings contributions to a goal', async () => {
        const laptopGoal = await $('//*[@text="New Laptop"]');
        await laptopGoal.click();

        const addContributionBtn = await $('//*[@content-desc="Add Savings Contribution Button"]');
        await addContributionBtn.click();

        const contributionInput = await $('//*[@content-desc="Savings Contribution Input"]');
        await contributionInput.setValue('300.00'); // Add another $300 (total $600 = 50%)

        const saveContributionBtn = await $('//*[@content-desc="Save Contribution Button"]');
        await saveContributionBtn.click();

        // Verify updated percentage progress display (50%)
        const progressPercentage = await $('//*[@text="50%"]');
        expect(await progressPercentage.isDisplayed()).toBe(true);
    });

    it('should complete a goal when target savings are fully met', async () => {
        const laptopGoal = await $('//*[@text="New Laptop"]');
        await laptopGoal.click();

        const addContributionBtn = await $('//*[@content-desc="Add Savings Contribution Button"]');
        await addContributionBtn.click();

        const contributionInput = await $('//*[@content-desc="Savings Contribution Input"]');
        await contributionInput.setValue('600.00'); // Meets the $1200 goal

        const saveContributionBtn = await $('//*[@content-desc="Save Contribution Button"]');
        await saveContributionBtn.click();

        // Verify milestone success banner/animation
        const completedBadge = await $('//*[@text="Goal Achieved!"]');
        expect(await completedBadge.isDisplayed()).toBe(true);
    });

    it('should support deleting an active goal', async () => {
        const laptopGoal = await $('//*[@text="New Laptop"]');
        await laptopGoal.click();

        const deleteGoalBtn = await $('//*[@content-desc="Delete Goal Button"]');
        await deleteGoalBtn.click();

        const confirmBtn = await $('//*[@text="Confirm Delete"]');
        await confirmBtn.click();

        // Verify goal card is deleted
        const laptopGoalCheck = await $('//*[@text="New Laptop"]');
        expect(await laptopGoalCheck.isDisplayed().catch(() => false)).toBe(false);
    });
});
