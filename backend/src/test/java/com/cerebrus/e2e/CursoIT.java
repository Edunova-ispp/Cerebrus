package com.cerebrus.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import org.openqa.selenium.WebDriver;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class CursoIT extends SeleniumBaseTest {

    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final String PROFESOR_OWNER = "carlos_pro";
    private static final String ALUMNO = "alumno_harry";
    private static final String PASSWORD = "123456";

    @Test
    @DisplayName("Sin autenticacion, rutas de cursos redirigen a login")
    void rutasCursosSinAutenticacionRedirigenALogin() {
        driver.manage().deleteAllCookies();

        List<String> rutasProtegidas = List.of(
                "/crearCurso",
                "/cursos/10001",
                "/misCursos"
        );

        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        for (String ruta : rutasProtegidas) {
            navigateTo(ruta);
            wait.until(ExpectedConditions.urlContains("/auth/login"));
            assertThat(driver.getCurrentUrl()).contains("/auth/login");
        }
    }

    @Test
    @DisplayName("Profesor propietario puede crear un curso")
    void profesorPropietarioPuedeCrearCurso() {
        login(PROFESOR_OWNER, PASSWORD);

        navigateTo("/crearCurso");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("titulo")));

        // Rellenar formulario
        driver.findElement(By.id("titulo")).sendKeys("Curso E2E Temporal");
        driver.findElement(By.id("descripcion")).sendKeys("Descripción temporal");
        driver.findElement(By.id("imagen")).sendKeys("img.png");

        driver.findElement(By.xpath("//button[normalize-space()='Crear curso']")).click();

        // Accept alert
        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (Exception e) {
            // No alert
        }

        wait.until(ExpectedConditions.urlContains("/miscursos"));
        assertThat(driver.getCurrentUrl()).contains("/miscursos");

        // Verificar que el curso aparece en la lista (opcional, si hay lista)
        // Aquí asumimos que se redirige a mis cursos
    }

    @Test
    @DisplayName("Alumno puede inscribirse en un curso")
    void alumnoPuedeInscribirseEnCurso() {
        login(ALUMNO, PASSWORD);

        navigateTo("/misCursos");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/misCursos"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//p[contains(text(), 'Cargando cursos...')]")));

        // Fill the course code input
        WebElement codeInput = driver.findElement(By.id("codigoCursoInput"));
        codeInput.clear();
        codeInput.sendKeys("MAT-201");

        // Click the join button
        WebElement joinButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Unirse')]")));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", joinButton);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", joinButton);

        // Wait for success message
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Matemáticas Mágicas"));
    }

    @Test
    @DisplayName("Alumno puede ver detalles de un curso inscrito")
    void alumnoPuedeVerDetallesCursoInscrito() {
        login(ALUMNO, PASSWORD);

        navigateTo("/cursos/10101");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Exploradores de la Naturaleza"));

        // Verificar que se muestran detalles
        assertThat(driver.getPageSource()).contains("Exploradores de la Naturaleza");
    }

    @Test
    @DisplayName("Usuario no profesor no puede crear curso")
    void usuarioNoProfesorNoPuedeCrearCurso() {
        login(ALUMNO, PASSWORD);

        navigateTo("/crearCurso");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/"));
        assertThat(driver.getCurrentUrl()).isEqualTo("http://localhost:5173/");
    }

    @Test
    @DisplayName("Profesor puede ver mis cursos")
    void profesorPuedeVerMisCursos() {
        login(PROFESOR_OWNER, PASSWORD);

        navigateTo("/miscursos");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/miscursos"));
        assertThat(driver.getCurrentUrl()).contains("/miscursos");
    }

    @Test
    @DisplayName("Usuario no inscrito no puede ver detalles de curso")
    void usuarioNoInscritoNoPuedeVerDetallesCurso() {
        login(ALUMNO, PASSWORD);

        navigateTo("/cursos/9999");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/misCursos"));
        assertThat(driver.getCurrentUrl()).contains("/misCursos");
    }

    @Test
    @DisplayName("Profesor puede ver detalles de un curso")
void profesorPuedeVerDetallesCurso() {
    login(PROFESOR_OWNER, PASSWORD);

    navigateTo("/cursos/10101");
    WebDriverWait wait = new WebDriverWait(driver, WAIT);
    
    wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.xpath("//*[contains(text(), 'Exploradores de la Naturaleza')]")));

    assertThat(driver.getPageSource()).contains("Exploradores de la Naturaleza");
}

    @Test
    @DisplayName("Profesor puede activar/desactivar un curso")
    void profesorPuedeActivarDesactivarCurso() {
        login(PROFESOR_OWNER, PASSWORD);

        navigateTo("/miscursos");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/miscursos"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//p[contains(text(), 'Cargando cursos...')]")));

        // Find the first toggle
        WebElement toggle = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//label[contains(@class, 'curso-card__toggle')]")));
        WebElement input = toggle.findElement(By.xpath(".//input[@type='checkbox']"));
        boolean initialState = input.isSelected();

        // Click to toggle
        toggle.click();

        // Wait a bit for the update
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean afterFirst = input.isSelected();
        assertThat(afterFirst).isNotEqualTo(initialState);

        // Toggle back to original state
        toggle.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
@DisplayName("Profesor puede eliminar un curso")
void profesorPuedeEliminarCurso() {
    login(PROFESOR_OWNER, PASSWORD);

    navigateTo("/miscursos");
    WebDriverWait wait = new WebDriverWait(driver, WAIT);
    wait.until(ExpectedConditions.urlContains("/miscursos"));
    
    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[contains(@class, 'curso-card')]")));

    List<WebElement> cards = driver.findElements(By.xpath("//div[contains(@class, 'curso-card')]"));
    int initialCount = cards.size();

    WebElement deleteBtn = wait.until(ExpectedConditions.elementToBeClickable(
        By.xpath("//button[contains(@class, 'curso-card__delete-btn')]")));
    deleteBtn.click();

    wait.until(ExpectedConditions.alertIsPresent());
    driver.switchTo().alert().accept();

    wait.until(d -> d.findElements(By.xpath("//div[contains(@class, 'curso-card')]")).size() == initialCount - 1);
    
    int finalCount = driver.findElements(By.xpath("//div[contains(@class, 'curso-card')]")).size();
    assertThat(finalCount).isEqualTo(initialCount - 1);
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
