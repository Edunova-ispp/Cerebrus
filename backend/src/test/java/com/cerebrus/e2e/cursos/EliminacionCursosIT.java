package com.cerebrus.e2e.cursos;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.cerebrus.e2e.SeleniumBaseTest;

class EliminacionCursosIT extends SeleniumBaseTest {

    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final String PROFESOR_OWNER = "carlos_pro";
    private static final String PASSWORD = "123456";

    @Test
    @DisplayName("Profesor puede eliminar un curso")
    void profesorPuedeEliminarCurso() {
        login(PROFESOR_OWNER, PASSWORD);

        navigateTo("/miscursos");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/miscursos"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//p[contains(text(), 'Cargando cursos...')]")));

        // Click the delete button
        WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'curso-card__delete-btn')]")));
        deleteBtn.click();

        // Wait for confirm alert
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        // Wait for the course to be removed from the page
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//span[contains(text(), 'NAT-101')]")));
        assertThat(driver.findElements(By.xpath("//span[contains(text(), 'NAT-101')]")).size()).isEqualTo(0);
    }

    private void login(String user, String password) {
        navigateTo("/auth/login");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        WebElement usuarioInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identificador")));
        usuarioInput.clear();
        usuarioInput.sendKeys(user);

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys(password);

        // Click the submit button
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Iniciar sesión')]")));
        submitButton.click();

        // Wait for either success (URL contains /misCursos) or error
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/miscursos"),
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".login-error-msg"))
            ));

            // Check if error is present
            if (driver.findElements(By.cssSelector(".login-error-msg")).size() > 0) {
                String errorMsg = driver.findElement(By.cssSelector(".login-error-msg")).getText();
                throw new AssertionError("Login failed with error: " + errorMsg);
            }

            // If no error, assert URL
            assertThat(driver.getCurrentUrl()).contains("/miscursos");
        } catch (Exception e) {
            // If timeout, check current URL
            if (!driver.getCurrentUrl().contains("/miscursos")) {
                throw new AssertionError("Login did not redirect to /miscursos. Current URL: " + driver.getCurrentUrl(), e);
            }
        }
    }
}