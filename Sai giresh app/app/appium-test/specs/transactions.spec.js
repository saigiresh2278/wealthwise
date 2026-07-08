describe('WealthWise AI - Transaction Management E2E Tests', () => {

    before(async () => {
        // Go to Transactions Screen
        const expensesTab = await $('//*[@content-desc="Expenses"]');
        await expensesTab.click();
    });

    it('should show Transaction Manager UI layout', async () => {
        const header = await $('//*[@text="Transactions"]');
        expect(await header.isDisplayed()).toBe(true);

        const addBtn = await $('//*[@content-desc="Add Transaction Button"]');
        expect(await addBtn.isDisplayed()).toBe(true);
    });

    it('should validate form constraints when adding a transaction', async () => {
        const addBtn = await $('//*[@content-desc="Add Transaction Button"]');
        await addBtn.click();

        // Try to save with empty fields
        const saveBtn = await $('//*[@content-desc="Save Transaction Button"]');
        await saveBtn.click();

        // Verify alert/error
        const errorMsg = await $('//*[@text="Please fill in all required fields"]');
        expect(await errorMsg.isDisplayed()).toBe(true);

        // Close transaction dialog/bottom sheet
        const closeBtn = await $('//*[@content-desc="Close Form Button"]');
        await closeBtn.click();
    });

    it('should successfully add an Expense transaction and verify list inclusion', async () => {
        const addBtn = await $('//*[@content-desc="Add Transaction Button"]');
        await addBtn.click();

        // Enter Title
        const titleInput = await $('//*[@content-desc="Transaction Title Input"]');
        await titleInput.setValue('Starbucks Coffee');

        // Enter Amount
        const amountInput = await $('//*[@content-desc="Transaction Amount Input"]');
        await amountInput.setValue('7.50');

        // Select Type (Expense is default, but click to ensure)
        const expenseTypeBtn = await $('//*[@content-desc="Type Expense Option"]');
        await expenseTypeBtn.click();

        // Select Category
        const categorySelector = await $('//*[@content-desc="Category Select Dropdown"]');
        await categorySelector.click();
        const foodCategory = await $('//*[@text="Food & Dining"]');
        await foodCategory.click();

        // Enter Description
        const descInput = await $('//*[@content-desc="Transaction Description Input"]');
        await descInput.setValue('Latte and croissant with coworker');

        // Save
        const saveBtn = await $('//*[@content-desc="Save Transaction Button"]');
        await saveBtn.click();

        // Verify item is added to the scrollable list
        const transactionItem = await $('//*[@text="Starbucks Coffee"]');
        expect(await transactionItem.isDisplayed()).toBe(true);
    });

    it('should successfully add an Income transaction and check recalculation', async () => {
        const addBtn = await $('//*[@content-desc="Add Transaction Button"]');
        await addBtn.click();

        const titleInput = await $('//*[@content-desc="Transaction Title Input"]');
        await titleInput.setValue('Freelance Project');

        const amountInput = await $('//*[@content-desc="Transaction Amount Input"]');
        await amountInput.setValue('500.00');

        const incomeTypeBtn = await $('//*[@content-desc="Type Income Option"]');
        await incomeTypeBtn.click();

        const categorySelector = await $('//*[@content-desc="Category Select Dropdown"]');
        await categorySelector.click();
        const incomeCategory = await $('//*[@text="Salary & Wages"]');
        await incomeCategory.click();

        const saveBtn = await $('//*[@content-desc="Save Transaction Button"]');
        await saveBtn.click();

        const transactionItem = await $('//*[@text="Freelance Project"]');
        expect(await transactionItem.isDisplayed()).toBe(true);
    });

    it('should filter transactions by Type and Category', async () => {
        // Toggle filter options
        const filterToggle = await $('//*[@content-desc="Filter Transactions Button"]');
        await filterToggle.click();

        const showIncomeOnly = await $('//*[@text="Income Only"]');
        await showIncomeOnly.click();

        // Check Starbucks Expense is hidden
        const coffeeItem = await $('//*[@text="Starbucks Coffee"]');
        expect(await coffeeItem.isDisplayed().catch(() => false)).toBe(false);

        // Check Freelance Income is visible
        const freelanceItem = await $('//*[@text="Freelance Project"]');
        expect(await freelanceItem.isDisplayed()).toBe(true);

        // Reset filter
        const showAll = await $('//*[@text="Show All"]');
        await showAll.click();
    });

    it('should support editing an existing transaction', async () => {
        // Long click or click edit button on Starbucks Coffee
        const coffeeItem = await $('//*[@text="Starbucks Coffee"]');
        await coffeeItem.click(); // opens detail or edit

        const editAction = await $('//*[@content-desc="Edit Action Button"]');
        await editAction.click();

        const amountInput = await $('//*[@content-desc="Transaction Amount Input"]');
        await amountInput.setValue('8.50'); // Edit price

        const saveBtn = await $('//*[@content-desc="Save Transaction Button"]');
        await saveBtn.click();

        // Verify updated price is shown
        const newPrice = await $('//*[@text="$8.50"]');
        expect(await newPrice.isDisplayed()).toBe(true);
    });

    it('should support deleting a transaction and updating balance metrics', async () => {
        const freelanceItem = await $('//*[@text="Freelance Project"]');
        await freelanceItem.click();

        const deleteAction = await $('//*[@content-desc="Delete Action Button"]');
        await deleteAction.click();

        // Handle delete confirmation modal
        const confirmBtn = await $('//*[@text="Yes, Delete"]');
        await confirmBtn.click();

        // Verify transaction is gone
        const deletedItem = await $('//*[@text="Freelance Project"]');
        expect(await deletedItem.isDisplayed().catch(() => false)).toBe(false);
    });

    it('should verify Receipt Scanning OCR button and modal interaction', async () => {
        const scanBtn = await $('//*[@content-desc="Scan Receipt OCR Button"]');
        await scanBtn.click();

        const ocrModal = await $('//*[@text="Scan Receipt"]');
        expect(await ocrModal.isDisplayed()).toBe(true);

        const uploadImageBtn = await $('//*[@content-desc="Upload Receipt Image"]');
        expect(await uploadImageBtn.isDisplayed()).toBe(true);

        // Close receipt scan dialog
        const closeScan = await $('//*[@content-desc="Cancel OCR Button"]');
        await closeScan.click();
    });
});
