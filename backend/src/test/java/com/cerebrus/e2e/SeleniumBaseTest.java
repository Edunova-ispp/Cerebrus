package com.cerebrus.e2e;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.Socket;

/**
 * Clase base para todos los tests E2E con Selenium.
 *
 * Uso:
 *   - Extender esta clase en cada IT (e.g. LoginIT extends SeleniumBaseTest)
 *   - Ejecutar con: mvn verify -Pe2e
 *   - Opciones configurables por sistema:
 *       -Dselenium.baseUrl=http://localhost:5173   (default en perfil e2e)
 *         (si usas compose no-dev, normalmente será http://localhost:3000)
 *       -Dselenium.headless=false                  (default: true en CI)
 */
@Tag("e2e")
public abstract class SeleniumBaseTest {

    /** URL base de la aplicación frontend contra la que corren los tests. */
    protected static final String BASE_URL =
            System.getProperty("selenium.baseUrl", "http://localhost:5173");

    protected WebDriver driver;

    @BeforeEach
    void initDriver() {
        ensureFrontendIsReachable();

        ChromeOptions options = new ChromeOptions();

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

    @AfterEach
    void tearDownDriver() {
        if (driver != null) {
            driver.quit();
        }
    }

    /** Navega a una ruta relativa de la aplicación (e.g. "/auth/login"). */
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
}
