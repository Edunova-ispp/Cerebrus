package com.cerebrus.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

    String uniqueTitle = "Curso E2E " + System.currentTimeMillis();
    driver.findElement(By.id("titulo")).sendKeys(uniqueTitle);
    driver.findElement(By.id("descripcion")).sendKeys("Descripción temporal");
    driver.findElement(By.id("codigo")).sendKeys("COD-" + System.currentTimeMillis()); // ← añadir

    submitCursoForm(wait);

    assertThat(driver.getCurrentUrl()).contains("/miscursos");
}

    @Test
    @DisplayName("Alumno puede inscribirse en un curso")
    void alumnoPuedeInscribirseEnCurso() {
        login(ALUMNO, PASSWORD);
        navigateTo("/misCursos");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/misCursos"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//p[contains(text(), 'Cargando cursos...')]")));

        WebElement codeInput = driver.findElement(By.id("codigoCursoInput"));
        codeInput.clear();
        codeInput.sendKeys("MAT-201");

        WebElement joinButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(), 'Unirse')]")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", joinButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", joinButton);

        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), "Matemáticas Mágicas"));
    }

    @Test
    @DisplayName("Alumno puede ver detalles de un curso inscrito")
    void alumnoPuedeVerDetallesCursoInscrito() {
        login(ALUMNO, PASSWORD);
        navigateTo("/cursos/10101");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.tagName("body"), "Exploradores de la Naturaleza"));

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
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//p[contains(text(), 'Cargando cursos...')]")));

        WebElement toggle = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//label[contains(@class, 'curso-card__toggle')]")));
        WebElement input = toggle.findElement(By.xpath(".//input[@type='checkbox']"));
        boolean initialState = input.isSelected();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggle);
        wait.until(d -> input.isSelected() != initialState);
        assertThat(input.isSelected()).isNotEqualTo(initialState);

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", toggle);
        wait.until(d -> input.isSelected() == initialState);
    }

    @Test
@DisplayName("Profesor puede eliminar un curso")
void profesorPuedeEliminarCurso() {
    login(PROFESOR_OWNER, PASSWORD);
    WebDriverWait wait = new WebDriverWait(driver, WAIT);

    String nombreCurso = "Borrar-" + System.currentTimeMillis();
    navigateTo("/crearCurso");
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))).sendKeys(nombreCurso);
    driver.findElement(By.id("descripcion")).sendKeys("Temporal");
    driver.findElement(By.id("codigo")).sendKeys("COD-" + System.currentTimeMillis()); // ← añadir

    submitCursoForm(wait);

        By cardLocator = By.xpath(
                "//div[contains(@class, 'curso-card')]"
                + "[.//span[contains(@class, 'curso-card__titulo') and normalize-space() = '"
                + nombreCurso + "']]");
        WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(cardLocator));

        WebElement deleteBtn = card.findElement(By.cssSelector("button.curso-card__delete-btn"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);

        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        navigateTo("/miscursos");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.mis-cursos-title")));
        wait.until(d -> d.findElements(cardLocator).isEmpty());
        assertThat(driver.findElements(cardLocator)).isEmpty();
    }

    private void submitCursoForm(WebDriverWait wait) {
        List<WebElement> submitBtns = driver.findElements(
                By.cssSelector("button.pixel-btn-submit-main"));
        if (!submitBtns.isEmpty()) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtns.get(0));
        } else {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(normalize-space(),'Crear curso') or contains(normalize-space(),'Crear Curso')]")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }

        try {
            wait.until(ExpectedConditions.alertIsPresent());
            driver.switchTo().alert().accept();
        } catch (org.openqa.selenium.TimeoutException ignored) {}

        try {
            wait.until(ExpectedConditions.urlContains("/miscursos"));
        } catch (org.openqa.selenium.TimeoutException e) {
            List<WebElement> errors = driver.findElements(
                    By.cssSelector("p.error-msg, [class*='error']"));
            String errorText = errors.stream().filter(WebElement::isDisplayed)
                    .map(WebElement::getText).findFirst().orElse("(sin mensaje de error)");
            throw new AssertionError("Sigue en /crearCurso. Error visible: " + errorText, e);
        }
    }
}