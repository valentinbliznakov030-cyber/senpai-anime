import puppeteer from 'puppeteer-extra';
import StealthPlugin from 'puppeteer-extra-plugin-stealth';

puppeteer.use(StealthPlugin());

const episodeUrl = process.argv[2];
let m3u8Link = null;
let browser = null;

async function run() {
    console.log(`START:${episodeUrl}`);

    try {
        browser = await puppeteer.launch({
            headless: false,
            args: [
                '--no-sandbox',
                '--disable-setuid-sandbox',
                '--disable-extensions',
                '--disable-infobars',
                '--start-maximized'
            ]
        });

        const page = await browser.newPage();

        await page.setExtraHTTPHeaders({
            'Referer': 'https://animepahe.si/',
            'User-Agent': 'Mozilla/5.0'
        });

        // Anti-bot
        await page.evaluateOnNewDocument(() => {
            window.open = () => null;
            Object.defineProperty(navigator, 'webdriver', { get: () => false });
        });

        page.on('popup', popup => popup.close());

        // Listen for all responses
        page.on('response', res => {
            const url = res.url();
            if (!m3u8Link && url.endsWith('.m3u8')) {
                m3u8Link = url;
                console.log(`FOUND:${url}`);
            }
        });

        // Navigate
        await page.goto(episodeUrl, {
            waitUntil: 'networkidle2',
            timeout: 30000
        });

        // Click handler
        try {
            await page.waitForSelector('.click-to-load', { timeout: 15000 });
            await page.click('.click-to-load');
        } catch (e) {
            console.log("NO-CLICK");
        }

        // Try to wait for direct response
        try {
            const response = await page.waitForResponse(
                res => res.url().endsWith('.m3u8'),
                { timeout: 40000 }
            );
            m3u8Link = response.url();
        } catch (e) {
            // Ignore — maybe listener caught it already
        }

    } catch (err) {
        console.log("ERROR:", err.message);
    }

    try { await browser.close(); } catch (e) {}

    console.log(`RESULT:${m3u8Link ? m3u8Link : "null"}`);

    process.exit(0);
}

run();
