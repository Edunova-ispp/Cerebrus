package com.cerebrus.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.net.Socket;

@Tag("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SeleniumBaseTest {

    protected static final String BASE_URL =
            System.getProperty("selenium.baseUrl", "http://localhost:5173");

    protected WebDriver driver;

    @BeforeAll
void initDriver() {
    ensureFrontendIsReachable();

    ChromeOptions options = new ChromeOptions();
    // Volver a NORMAL — EAGER rompe React SPAs
    options.setPageLoadStrategy(PageLoadStrategy.NORMAL);

    boolean headless = Boolean.parseBoolean(
            System.getProperty("selenium.headless", "true"));
    if (headless) {
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
    }

    driver = new ChromeDriver(options);
    driver.manage().window().maximize();
}

    @BeforeEach
    void resetBrowserState() {
        try {
            ((JavascriptExecutor) driver).executeScript("window.stop();");
        } catch (WebDriverException ignored) {}

        try {
            driver.get(BASE_URL + "/");
        } catch (WebDriverException ignored) {}

        try {
            driver.manage().deleteAllCookies();
        } catch (WebDriverException ignored) {}

        if (driver instanceof JavascriptExecutor js) {
            try {
                js.executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
            } catch (WebDriverException ignored) {}
        }
    }

    @AfterAll
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected void navigateTo(String path) {
        driver.get(BASE_URL + path);
    }

    private static void ensureFrontendIsReachable() {
        try {
            URI uri = new URI(BASE_URL);
            String host = uri.getHost();
            int port = uri.getPort();

            if (port == -1) {
                port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
            }

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 2000);
            }
        } catch (URISyntaxException | IOException ex) {
            throw new IllegalStateException(
                    "No se puede conectar al frontend en " + BASE_URL + ". " +
                    "Arranca el frontend o cambia la URL con -Dselenium.baseUrl=http://localhost:5173 (o 3000).",
                    ex
            );
        }
    }

    // En SeleniumBaseTest.java — añadir este método protected
protected void login(String user, String password) {
    navigateTo("/auth/login");

    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

    WebElement usuarioInput = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("identificador")));
    usuarioInput.clear();
    usuarioInput.sendKeys(user);
    wait.until(d -> !usuarioInput.getAttribute("value").isEmpty());

    WebElement passwordInput = driver.findElement(By.id("password"));
    passwordInput.clear();
    passwordInput.sendKeys(password);
    wait.until(d -> !passwordInput.getAttribute("value").isEmpty());

    // Esperar a que el botón sea clickable (React hydration completa)
    WebElement submitBtn = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("button.pixel-btn-submit")));
    submitBtn.click();

    wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
    assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
}
}