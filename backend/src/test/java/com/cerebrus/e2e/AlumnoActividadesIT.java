package com.cerebrus.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class AlumnoActividadesIT extends SeleniumBaseTest {

    private static final Duration WAIT = Duration.ofSeconds(15);
    private static final String ALUMNO_USUARIO = "alumno_harry";
    private static final String ALUMNO_PASSWORD = "123456";

    @Test
    @DisplayName("Sin autenticacion, rutas de alumno redirigen a login")
    void rutasAlumnoSinLoginRedirigenALogin() {
        driver.manage().deleteAllCookies();

        List<String> rutasProtegidas = List.of(
                "/generales/test/6001/alumno",
                "/generales/carta/6001/alumno",
                "/clasificaciones/1/alumno",
                "/crucigrama/7001/alumno",
                "/tableros/1/alumno",
                "/marcar-imagenes/1/alumno",
                "/ordenaciones/6002/alumno",
                "/actividades/teoria/6004",
                "/abierta/6004/alumno"
        );

        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        for (String ruta : rutasProtegidas) {
            navigateTo(ruta);
            wait.until(ExpectedConditions.urlContains("/auth/login"));
            assertThat(driver.getCurrentUrl()).contains("/auth/login");
        }
    }

    @Test
    @DisplayName("El alumno puede resolver un test de seed y ver el popup de completado")
    void alumnoPuedeResolverTestDeSeed() {
        loginAsAlumno();

        navigateTo("/generales/test/6001/alumno");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), 'Test HTML')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), '¿Qué significa HTML?')]") ));

        driver.findElement(By.xpath("//button[contains(., 'HyperText Markup Language')]")).click();
        driver.findElement(By.xpath("//button[normalize-space()='¡Enviar respuestas!']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), '¡TEST COMPLETADO!')]")));
        assertThat(driver.findElement(By.cssSelector(".ta-score-banner")).getText()).contains("1 / 1 correctas");
        assertThat(driver.findElement(By.xpath("//*[contains(normalize-space(), '¡Perfecto! 1 / 1 correctas')]")).isDisplayed()).isTrue();
    }

    @Test
    @DisplayName("El alumno ve incorrecta si responde mal un test de seed")
    void alumnoVeIncorrectaAlResponderMal() {
        loginAsAlumno();

        navigateTo("/generales/test/6001/alumno");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), 'Test HTML')]")));

        driver.findElement(By.xpath("//button[contains(., 'High Text Machine Language')]")).click();
        driver.findElement(By.xpath("//button[normalize-space()='¡Enviar respuestas!']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), '✗ Incorrecta')]")));
        assertThat(driver.findElement(By.cssSelector(".ta-score-banner")).getText()).contains("0 / 1 correctas");
    }

    @Test
    @DisplayName("El alumno puede completar una teoria y continuar")
    void alumnoPuedeCompletarTeoriaYContinuar() {
        loginAsAlumno();

        navigateTo("/actividades/teoria/6004");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains("/actividades/teoria/6004"));
        WebElement finalizarLecturaBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//button[normalize-space()='He terminado de leer']")));

        finalizarLecturaBtn.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), '¡LECCIÓN COMPLETADA!')]")));

        driver.findElement(By.xpath("//button[normalize-space()='Continuar']")).click();
        wait.until(ExpectedConditions.urlContains("/miscursos"));
        assertThat(driver.getCurrentUrl()).contains("/miscursos");
    }

    @Test
    @DisplayName("El alumno puede enviar una ordenacion y recibir feedback")
    void alumnoPuedeEnviarOrdenacionYVerFeedback() {
        loginAsAlumno();

        navigateTo("/ordenaciones/6002/alumno");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), 'Ordena la estructura')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ord-item")));

        // Alteramos el orden para evitar enviar siempre el estado inicial.
        List<WebElement> downButtons = driver.findElements(By.cssSelector(".ord-item .ord-arrow-btn:last-child"));
        downButtons.stream().filter(WebElement::isEnabled).findFirst().ifPresent(WebElement::click);

        WebElement sendButton = driver.findElement(By.xpath("//button[normalize-space()='Enviar']"));
        wait.until(ExpectedConditions.elementToBeClickable(sendButton));
        sendButton.click();

        wait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), 'Tu respuesta es correcta.')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), 'Tu respuesta es incorrecta.')]"))
        ));

        String feedback = driver.findElement(By.cssSelector(".ord-feedback")).getText();
        assertThat(feedback).containsAnyOf("Tu respuesta es correcta.", "Tu respuesta es incorrecta.");

        clickContinueIfPresent(wait);
    }

    private void loginAsAlumno() {
        navigateTo("/auth/login");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        WebElement usuarioInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identificador")));
        usuarioInput.clear();
        usuarioInput.sendKeys(ALUMNO_USUARIO);

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys(ALUMNO_PASSWORD);

        driver.findElement(By.cssSelector("button.pixel-btn-submit")).click();

        wait.until(ExpectedConditions.urlContains("/miscursos"));
        assertThat(driver.getCurrentUrl()).contains("/miscursos");
    }

    private void clickContinueIfPresent(WebDriverWait wait) {
        List<WebElement> continueButtons = driver.findElements(By.xpath("//button[normalize-space()='Continuar']"));
        if (!continueButtons.isEmpty() && continueButtons.get(0).isDisplayed()) {
            continueButtons.get(0).click();
            wait.until(ExpectedConditions.urlContains("/miscursos"));
            assertThat(driver.getCurrentUrl()).contains("/miscursos");
        }
    }
}