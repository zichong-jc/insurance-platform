package com.example.insurance.sync.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 浏览器自动化服务 - 用于模拟真实浏览器行为，绕过反爬虫
 */
@Slf4j
@Service
public class BrowserAutomationService {

    private final ConcurrentHashMap<String, BrowserContext> contextCache = new ConcurrentHashMap<>();
    private Playwright playwright;
    private Browser browser;

    /**
     * 初始化浏览器
     */
    public synchronized void init() {
        if (playwright == null) {
            log.info("Initializing Playwright browser...");
            playwright = Playwright.create();

            // Mac M系列芯片需要特殊处理
            String osName = System.getProperty("os.name").toLowerCase();
            String osArch = System.getProperty("os.arch").toLowerCase();
            boolean isMacARM = osName.contains("mac") && (osArch.contains("aarch64") || osArch.contains("arm"));

            BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(java.util.List.of(
                            "--disable-blink-features=AutomationControlled",
                            "--disable-web-security",
                            "--disable-features=IsolateOrigins,site-per-process"
                    ));

            if (isMacARM) {
                log.info("Detected Mac ARM architecture, using WebKit instead of Chromium");
                // Mac ARM 上使用 WebKit 更稳定
                browser = playwright.webkit().launch(launchOptions);
            } else {
                browser = playwright.chromium().launch(launchOptions);
            }

            log.info("Playwright browser initialized: {}", browser.browserType().name());
        }
    }

    /**
     * 获取或创建浏览器上下文
     */
    public BrowserContext getContext(String contextId) {
        init();
        return contextCache.computeIfAbsent(contextId, id -> {
            log.debug("Creating new browser context for: {}", id);
            return browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .setViewportSize(1920, 1080)
                    .setLocale("zh-CN")
                    .setTimezoneId("Asia/Shanghai")
                    .setExtraHTTPHeaders(java.util.Map.of(
                            "Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8",
                            "Accept", "application/json, text/plain, */*"
                    )));
        });
    }

    /**
     * 使用浏览器获取页面内容
     *
     * @param contextId 上下文ID（用于复用会话）
     * @param url       目标URL
     * @return 页面内容
     */
    public String fetchPageContent(String contextId, String url) {
        init();
        BrowserContext context = getContext(contextId);
        Page page = context.newPage();

        try {
            log.info("Fetching page with browser: {}", url);

            // 设置额外的请求头
            page.setExtraHTTPHeaders(java.util.Map.of(
                    "Referer", "https://baoxian.pingan.com",
                    "Origin", "https://baoxian.pingan.com"
            ));

            // 拦截并修改请求，移除自动化标记
            page.route("**/*", route -> {
                route.resume();
            });

            // 注入脚本隐藏自动化特征
            page.addInitScript("() => {\n" +
                    "  Object.defineProperty(navigator, 'webdriver', { get: () => undefined });\n" +
                    "  Object.defineProperty(navigator, 'plugins', { get: () => [1, 2, 3, 4, 5] });\n" +
                    "  Object.defineProperty(navigator, 'languages', { get: () => ['zh-CN', 'zh', 'en'] });\n" +
                    "  window.chrome = { runtime: {} };\n" +
                    "}");

            // 导航到页面
            Response response = page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE)
                    .setTimeout(60000));

            if (!response.ok()) {
                throw new RuntimeException("Failed to load page: " + response.status());
            }

            // 等待页面加载完成
            page.waitForLoadState(LoadState.NETWORKIDLE);

            // 额外等待确保动态内容加载
            page.waitForTimeout(2000);

            // 获取页面内容
            String content = page.content();
            log.info("Page loaded successfully, content length: {}", content.length());

            return content;

        } catch (Exception e) {
            log.error("Failed to fetch page: {}", url, e);
            throw new RuntimeException("Browser automation failed", e);
        } finally {
            page.close();
        }
    }

    /**
     * 执行JavaScript并获取结果
     */
    public Object executeScript(String contextId, String url, String script) {
        init();
        BrowserContext context = getContext(contextId);
        Page page = context.newPage();

        try {
            page.navigate(url, new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.NETWORKIDLE));

            return page.evaluate(script);
        } finally {
            page.close();
        }
    }

    /**
     * 关闭指定上下文
     */
    public void closeContext(String contextId) {
        BrowserContext context = contextCache.remove(contextId);
        if (context != null) {
            context.close();
            log.debug("Closed browser context: {}", contextId);
        }
    }

    /**
     * 关闭所有资源
     */
    public synchronized void shutdown() {
        log.info("Shutting down Playwright browser...");
        contextCache.values().forEach(BrowserContext::close);
        contextCache.clear();

        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
        log.info("Playwright browser shut down");
    }
}