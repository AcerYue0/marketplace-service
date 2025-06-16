const { chromium } = require('playwright');

(async () => {
    const browser = await chromium.launch({ headless: true });
    const context = await browser.newContext();
    const page = await context.newPage();

    await page.goto('https://msu.io/marketplace/api/marketplace/explore/items');

    // 等待 Cloudflare 驗證結束
    await page.waitForLoadState('networkidle');

    // 若 API 回傳 JSON，可透過 evaluate 取值
    const data = await page.evaluate(() => {
        return fetch("https://msu.io/marketplace/api/marketplace/explore/items", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ key: "value" })
        }).then(res => res.json());
    });

    console.log(data);

    await browser.close();
})();