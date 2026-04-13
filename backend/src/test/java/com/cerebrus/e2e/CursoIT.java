package com.cerebrus.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
    WebDriverWait wait = new WebDriverWait(driver, WAIT);

    // 1. Crear un curso único (usamos timestamp para que el nombre sea único siempre)
    String nombreCurso = "Borrar-" + System.currentTimeMillis();
    navigateTo("/crearCurso");
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))).sendKeys(nombreCurso);
    driver.findElement(By.id("descripcion")).sendKeys("Temporal");
    driver.findElement(By.xpath("//button[normalize-space()='Crear curso']")).click();
    
    // Esperar a que se procese la creación (Alerta + Redirección)
    try {
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    } catch (Exception e) { /* No alert */ }
    
    wait.until(ExpectedConditions.urlContains("/miscursos"));

    // 2. Localizar el curso específico que acabamos de crear
    // Buscamos la card que contiene el nombre único
    By cardLocator = By.xpath("//div[contains(@class, 'curso-card')][.//span[contains(@class, 'curso-card__titulo') and normalize-space() = '" + nombreCurso + "']]");
    WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(cardLocator));

    // 3. Borrar esa card específica
    WebElement deleteBtn = card.findElement(By.cssSelector("button.curso-card__delete-btn"));
    deleteBtn.click();

    wait.until(ExpectedConditions.alertIsPresent());
    driver.switchTo().alert().accept();

    // 4. Esperar a que la lista se actualice (inspirado en TemasIT)
    navigateTo("/miscursos");
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.mis-cursos-title")));

    wait.until(d -> d.findElements(cardLocator).isEmpty());
}
    
private void login(String user, String password) {
    navigateTo("/auth/login");

    WebDriverWait wait = new WebDriverWait(driver, WAIT);
    
    // 1. Espera explícita al primer campo para asegurar que la página cargó
    WebElement usuarioInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identificador")));
    usuarioInput.clear();
    usuarioInput.sendKeys(user);

    WebElement passwordInput = driver.findElement(By.id("password"));
    passwordInput.clear();
    passwordInput.sendKeys(password);

    // 2. Usar RETURN en lugar de click en el botón (evita el Timeout del botón)
    passwordInput.sendKeys(org.openqa.selenium.Keys.RETURN);

    // 3. Esperar a que la URL cambie para confirmar que el login procesó
    wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
    
    // 4. Verificación extra opcional
    assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
}
}
