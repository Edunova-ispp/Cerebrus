package com.cerebrus.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests de humo: verifican que la aplicación arranca y las páginas
 * principales son accesibles. No requieren usuario en la BD.
 *
 * Ejecutar con: mvn verify -Pe2e
 * (asume la app corriendo en http://localhost:3000 o -Dselenium.baseUrl=...)
 */
class SmokeIT extends SeleniumBaseTest {

    private static final Duration WAIT = Duration.ofSeconds(10);

    

    @Test
    @DisplayName("La landing page carga y muestra el título CerebrUS")
    void landingPageLoads() {
        navigateTo("/");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        WebElement title = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.landing-title"))
        );

        // El título se compone de spans por letra y puede incluir saltos/espacios
        String normalizedTitle = title.getText().replaceAll("\\s+", "");
        assertThat(normalizedTitle).isEqualToIgnoringCase("cerebrus");
    }

    @Test
    @DisplayName("La página de login carga con los campos de usuario y contraseña")
    void loginPageShowsForm() {
        navigateTo("/auth/login");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // Título de la página
        WebElement heading = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.login-title"))
        );
        assertThat(heading.getText()).isEqualTo("Iniciar Sesión");

        // Campo usuario
        WebElement userInput = driver.findElement(By.id("identificador"));
        assertThat(userInput.isDisplayed()).isTrue();

        // Campo contraseña
        WebElement passwordInput = driver.findElement(By.id("password"));
        assertThat(passwordInput.isDisplayed()).isTrue();

        // Botón de envío
        WebElement submitBtn = driver.findElement(By.cssSelector("button.pixel-btn-submit"));
        assertThat(submitBtn.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("El botón de login de la landing navega a /auth/login")
    void landingLoginButtonNavigatesToLogin() {
        navigateTo("/");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // Botón "Iniciar sesión" de la landing (el que tiene el icono de perfil)
        WebElement loginBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector("button.landing-login-btn:last-of-type"))
        );
        loginBtn.click();

        wait.until(ExpectedConditions.urlContains("/auth/login"));
        assertThat(driver.getCurrentUrl()).contains("/auth/login");
    }
}
