exports.config = {
    //
    // ====================
    // Runner Configuration
    // ====================
    //
    runner: 'local',
    port: 4723,
    path: '/',

    //
    // ==================
    // Specify Test Files
    // ==================
    //
    specs: [
        './specs/**/*.spec.js'
    ],
    exclude: [],

    //
    // ============
    // Capabilities
    // ============
    //
    maxInstances: 1,
    capabilities: [{
        // The defaults for Android UiAutomator2
        platformName: 'Android',
        'appium:automationName': 'UiAutomator2',
        'appium:deviceName': 'Android Emulator',
        
        // Relative path to the app's debug APK
        'appium:app': '../build/outputs/apk/debug/app-debug.apk',
        
        // App packages and main activity
        'appium:appPackage': 'com.company.finanicaltracker.wealthai',
        'appium:appActivity': 'com.example.wealthwiseai.MainActivity',
        
        // Timeout configurations
        'appium:newCommandTimeout': 240,
        
        // Reset strategy
        'appium:noReset': false,
        'appium:fullReset': false,
        
        // Grant permissions automatically
        'appium:autoGrantPermissions': true
    }],

    //
    // ===================
    // Test Configurations
    // ===================
    //
    logLevel: 'info',
    bail: 0,
    baseUrl: 'http://localhost',
    waitforTimeout: 10000,
    connectionRetryTimeout: 120000,
    connectionRetryCount: 3,

    services: ['appium'],

    framework: 'mocha',
    reporters: ['spec'],
    
    mochaOpts: {
        ui: 'bdd',
        timeout: 60000
    },

    //
    // =====
    // Hooks
    // =====
    //
    before: async function (capabilities, specs) {
        // Set implicit timeout or other setups
        await driver.setTimeouts(5000);
    },

    afterTest: async function(test, context, { error, result, duration, passed, retries }) {
        if (!passed) {
            // Take screenshot on failure
            const filepath = `./screenshots/${test.title.replace(/\s+/g, '_')}.png`;
            await driver.saveScreenshot(filepath);
            console.log(`[FAILURE SCREENSHOT] Saved failed test screenshot to ${filepath}`);
        }
    }
};
