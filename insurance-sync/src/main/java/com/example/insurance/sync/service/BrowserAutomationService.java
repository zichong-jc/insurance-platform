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

            // 检查是否有同意协议按钮，自动点击
            handleAgreementIfNeeded(page);

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
     * 处理用户协议同意弹窗
     */
    private void handleAgreementIfNeeded(Page page) {
        try {
            // 常见的同意按钮选择器
            String[] agreeSelectors = {
                "button:has-text('同意')",
                "button:has-text('我同意')",
                "button:has-text('接受')",
                "button:has-text('确认')",
                "a:has-text('同意')",
                "a:has-text('我同意')",
                "[class*='agree']",
                "[class*='accept']",
                "[id*='agree']",
                "[id*='accept']",
                "input[type='checkbox'] + label:has-text('同意')",
                ".protocol-btn",
                ".agreement-btn"
            };

            for (String selector : agreeSelectors) {
                try {
                    Locator locator = page.locator(selector);
                    if (locator.count() > 0 && locator.isVisible()) {
                        log.info("Found agreement button with selector: {}", selector);
                        locator.click();
                        log.info("Clicked agreement button");
                        page.waitForTimeout(1000);
                        return;
                    }
                } catch (Exception e) {
                    // 继续尝试下一个选择器
                }
            }

            // 检查是否有复选框需要勾选
            try {
                Locator checkbox = page.locator("input[type='checkbox']").first();
                if (checkbox.isVisible() && !checkbox.isChecked()) {
                    checkbox.check();
                    log.info("Checked agreement checkbox");
                }
            } catch (Exception e) {
                // 忽略
            }

        } catch (Exception e) {
            log.debug("No agreement dialog found or failed to handle: {}", e.getMessage());
        }
    }

    /**
     * 从产品详情页提取PDF链接
     * 1. 访问产品详情页
     * 2. 处理同意协议弹窗
     * 3. 找到"我已阅读并同意"后面的所有PDF链接
     */
    public java.util.List<com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo> extractPdfLinks(String contextId, String url) {
        init();
        BrowserContext context = getContext(contextId);
        Page page = context.newPage();

        java.util.List<com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo> pdfLinks = new java.util.ArrayList<>();

        try {
            log.info("Extracting PDF links from: {}", url);

            // 设置请求头
            page.setExtraHTTPHeaders(java.util.Map.of(
                    "Referer", "https://baoxian.pingan.com",
                    "Origin", "https://baoxian.pingan.com"
            ));

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

            // 等待页面加载
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(3000);

            // 处理同意协议弹窗
            handleAgreementIfNeeded(page);

            // 额外等待动态内容加载
            page.waitForTimeout(2000);

            // 查找"我已阅读并同意"文本及其后面的PDF链接
            // 方法1：查找包含特定文本的元素后面的链接
            pdfLinks.addAll(findPdfLinksAfterAgreement(page));

            // 方法2：如果方法1没找到，尝试查找页面中所有的PDF链接
            if (pdfLinks.isEmpty()) {
                pdfLinks.addAll(findAllPdfLinks(page));
            }

            log.info("Found {} PDF links on page", pdfLinks.size());
            return pdfLinks;

        } catch (Exception e) {
            log.error("Failed to extract PDF links from: {}", url, e);
            throw new RuntimeException("Failed to extract PDF links", e);
        } finally {
            page.close();
        }
    }

    /**
     * 查找"我已阅读并同意"后面的PDF链接
     */
    private java.util.List<com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo> findPdfLinksAfterAgreement(Page page) {
        java.util.List<com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo> links = new java.util.ArrayList<>();

        try {
            // 查找包含"我已阅读并同意"或类似文本的元素
            String[] agreementTexts = {"我已阅读并同意", "阅读并同意", "同意", "我已阅读"};
            
            for (String text : agreementTexts) {
                try {
                    // 查找包含该文本的元素
                    Locator agreementLocator = page.locator("text=" + text).first();
                    if (agreementLocator.count() > 0) {
                        log.info("Found agreement text: {}", text);
                        
                        // 获取该元素后面的所有链接
                        Object result = page.evaluate("(agreementText) => {\n" +
                                "  const elements = document.querySelectorAll('*');\n" +
                                "  let foundAgreement = false;\n" +
                                "  const links = [];\n" +
                                "  for (const el of elements) {\n" +
                                "    if (!foundAgreement && el.textContent.includes(agreementText)) {\n" +
                                "      foundAgreement = true;\n" +
                                "      continue;\n" +
                                "    }\n" +
                                "    if (foundAgreement && el.tagName === 'A') {\n" +
                                "      const href = el.getAttribute('href');\n" +
                                "      if (href && (href.endsWith('.pdf') || href.includes('.pdf'))) {\n" +
                                "        links.push({\n" +
                                "          name: el.textContent.trim() || '未命名文档',\n" +
                                "          url: href.startsWith('http') ? href : window.location.origin + href\n" +
                                "        });\n" +
                                "      }\n" +
                                "    }\n" +
                                "  }\n" +
                                "  return links;\n" +
                                "}", text);

                        if (result instanceof java.util.List) {
                            java.util.List<?> rawLinks = (java.util.List<?>) result;
                            for (Object rawLink : rawLinks) {
                                if (rawLink instanceof java.util.Map) {
                                    java.util.Map<?, ?> map = (java.util.Map<?, ?>) rawLink;
                                    com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo info = 
                                        new com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo();
                                    info.setName((String) map.get("name"));
                                    info.setUrl((String) map.get("url"));
                                    links.add(info);
                                }
                            }
                        }
                        
                        if (!links.isEmpty()) {
                            break; // 找到了就退出
                        }
                    }
                } catch (Exception e) {
                    log.debug("Failed to find agreement text: {}", text);
                }
            }
        } catch (Exception e) {
            log.error("Error finding PDF links after agreement", e);
        }

        return links;
    }

    /**
     * 查找页面中所有的PDF链接
     */
    private java.util.List<com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo> findAllPdfLinks(Page page) {
        java.util.List<com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo> links = new java.util.ArrayList<>();

        try {
            Object result = page.evaluate("() => {\n" +
                    "  const links = [];\n" +
                    "  const anchors = document.querySelectorAll('a[href]');\n" +
                    "  for (const anchor of anchors) {\n" +
                    "    const href = anchor.getAttribute('href');\n" +
                    "    if (href && (href.endsWith('.pdf') || href.includes('.pdf'))) {\n" +
                    "      links.push({\n" +
                    "        name: anchor.textContent.trim() || '未命名文档',\n" +
                    "        url: href.startsWith('http') ? href : window.location.origin + href\n" +
                    "      });\n" +
                    "    }\n" +
                    "  }\n" +
                    "  return links;\n" +
                    "}");

            if (result instanceof java.util.List) {
                java.util.List<?> rawLinks = (java.util.List<?>) result;
                for (Object rawLink : rawLinks) {
                    if (rawLink instanceof java.util.Map) {
                        java.util.Map<?, ?> map = (java.util.Map<?, ?>) rawLink;
                        com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo info = 
                            new com.example.insurance.sync.strategy.PingAnSync.PdfDocumentInfo();
                        info.setName((String) map.get("name"));
                        info.setUrl((String) map.get("url"));
                        links.add(info);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error finding all PDF links", e);
        }

        return links;
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