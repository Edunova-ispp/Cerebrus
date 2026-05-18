package com.cerebrus.e2e;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Tag("e2e")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class SeleniumBaseTest {

    protected static final String BASE_URL =
            System.getProperty("selenium.baseUrl", "http://localhost:5173");
    protected static final String BACKEND_URL =
            System.getProperty("selenium.backendUrl", "http://localhost:8080");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        String url = BASE_URL + path;
        // Try to navigate via JS to avoid driver.get() page-load timeouts
        if (driver instanceof JavascriptExecutor js) {
            boolean navigated = false;
            for (int i = 0; i < 3; i++) {
                try {
                    js.executeScript("var a=document.createElement('a'); a.href=arguments[0]; a.target='_self'; document.body.appendChild(a); a.click();", url);
                    navigated = true;
                    break;
                } catch (Exception ex) {
                    try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                }
            }
            if (!navigated) {
                try {
                    driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
                } catch (Exception ignored) {}
                driver.get(url);
            }
        } else {
            driver.get(url);
        }
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

    private static void ensureBackendIsReachable() {
        try {
            URI uri = new URI(BACKEND_URL);
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
                    "No se puede conectar al backend en " + BACKEND_URL + ". " +
                            "Arranca el backend o cambia la URL con -Dselenium.backendUrl=http://localhost:8080",
                    ex
            );
        }
    }

    protected void login(String user, String password) {
        try {
            // Check backend connectivity first
            ensureBackendIsReachable();

            HttpClient client = HttpClient.newHttpClient();
            String requestBody = OBJECT_MAPPER.writeValueAsString(Map.of(
                    "identificador", user,
                    "password", password));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = null;
            int attempts = 0;
            while (attempts < 3) {
                try {
                    response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    break;
                } catch (IOException | InterruptedException ioe) {
                    attempts++;
                    if (attempts >= 3) {
                        response = null;
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {}
                }
            }

            if (response == null) {
                // Fallback to UI login if HTTP login failed after retries
                navigateTo("/auth/login");
                WebDriverWait uiWait = new WebDriverWait(driver, Duration.ofSeconds(10));
                WebElement identificador = uiWait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identificador")));
                WebElement passwordInput = driver.findElement(By.id("password"));
                identificador.clear();
                identificador.sendKeys(user);
                passwordInput.clear();
                passwordInput.sendKeys(password);
                try {
                    WebElement submit = driver.findElement(By.cssSelector("form.login-form button[type='submit']"));
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submit);
                } catch (Exception clickEx) {
                    ((JavascriptExecutor) driver).executeScript("var f=document.querySelector('form.login-form'); if(f) f.dispatchEvent(new Event('submit',{bubbles:true,cancelable:true}));");
                }
                // wait for redirect
                WebDriverWait waitUi = new WebDriverWait(driver, Duration.ofSeconds(20));
                waitUi.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
                return;
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Login fallido para " + user + " (HTTP " + response.statusCode() + "): " + response.body());
            }

            JsonNode body = OBJECT_MAPPER.readTree(response.body());
            String token = body.path("token").asText("");
            String username = body.path("username").asText(user);
            List<String> roles = new ArrayList<>();
            JsonNode rolesNode = body.path("roles");
            if (rolesNode.isArray()) {
                for (JsonNode roleNode : rolesNode) {
                    String role = roleNode.asText("");
                    if (!role.isBlank()) {
                        roles.add(role);
                    }
                }
            }

            if (token.isBlank()) {
                throw new IllegalStateException("Login fallido para " + user + ": el backend no devolvió token");
            }

            if (driver instanceof JavascriptExecutor js) {
                js.executeScript("""
                        window.localStorage.setItem('token', arguments[0]);
                        window.localStorage.setItem('username', arguments[1]);
                        window.localStorage.setItem('role', arguments[2]);
                        """, token, username, String.join(",", roles));
            }

            String destination = roles.stream().anyMatch(role -> role.contains("ORGANIZACION") || role.contains("DUENO"))
                    ? "/suscripcion"
                    : "/miscursos";

            // Try to navigate via JS to avoid driver.get() page-load timeouts on some environments.
            if (driver instanceof JavascriptExecutor js) {
                boolean navigated = false;
                for (int i = 0; i < 3; i++) {
                    try {
                        js.executeScript("var a=document.createElement('a'); a.href=arguments[0]; a.target='_self'; document.body.appendChild(a); a.click();", BASE_URL + destination);
                        navigated = true;
                        break;
                    } catch (Exception ex) {
                        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                    }
                }
                if (!navigated) {
                    try {
                        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(120));
                    } catch (Exception ignored) {}
                    navigateTo(destination);
                }
            } else {
                navigateTo(destination);
            }

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
            assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo iniciar sesión para el usuario " + user, ex);
        }
    }
}
