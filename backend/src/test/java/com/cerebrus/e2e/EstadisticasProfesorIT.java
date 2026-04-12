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

class EstadisticasProfesorIT extends SeleniumBaseTest {

    private static final Duration WAIT = Duration.ofSeconds(20);
    private static final String PROFESOR_OWNER = "carlos_pro";
    private static final String PROFESOR_NO_OWNER = "profe_snape";
    private static final String PASSWORD = "123456";

         @BeforeEach
void clearData() {
    // Primero navegar al dominio para que las cookies sean del dominio correcto
    navigateTo("/");
    driver.manage().deleteAllCookies();
    ((org.openqa.selenium.JavascriptExecutor) driver)
        .executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
    // Forzar recarga para que el frontend procese el estado vacío
    driver.navigate().refresh();
}
    @Test
    @DisplayName("Sin autenticacion, rutas de estadisticas de profesor redirigen a login")
    void rutasEstadisticasSinAutenticacionRedirigenALogin() {
        driver.manage().deleteAllCookies();

        List<String> rutasProtegidas = List.of(
                "/estadisticas/4001/actividades",
                "/estadisticas/4001/temas",
                "/estadisticas/actividades/6001",
                "/estadisticas/temas/5001"
        );

        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        for (String ruta : rutasProtegidas) {
            navigateTo(ruta);
            wait.until(ExpectedConditions.urlContains("/auth/login"));
            assertThat(driver.getCurrentUrl()).contains("/auth/login");
        }
    }

    @Test
    @DisplayName("Profesor propietario puede recorrer el flujo de estadisticas del curso")
    void profesorPropietarioPuedeRecorrerFlujoEstadisticas() {
        login(PROFESOR_OWNER, PASSWORD);

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        // Resumen de actividades
        navigateTo("/estadisticas/4001/actividades");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(normalize-space(), 'Actividades del Curso')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[contains(normalize-space(), 'Nota Media')]")));

        // Desglose por temas
        navigateTo("/estadisticas/4001/temas");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(normalize-space(), 'Temas del Curso')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[contains(normalize-space(), 'Completado por todos')]")));

        // Estadística de una actividad concreta
        navigateTo("/estadisticas/actividades/6001");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(normalize-space(), 'Estadísticas de la Actividad')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[contains(normalize-space(), 'Tiempo Invertido')]")));

        // Estadística de un tema concreto
        navigateTo("/estadisticas/temas/5001");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h1[contains(normalize-space(), 'Estadísticas del Tema')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//th[contains(normalize-space(), 'Alumno')]")));
    }

    @Test
    @DisplayName("Profesor no propietario no puede cargar estadisticas del curso ajeno")
    void profesorNoPropietarioNoPuedeCargarEstadisticasCursoAjeno() {
        login(PROFESOR_NO_OWNER, PASSWORD);
        navigateTo("/estadisticas/4001/actividades");
        
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        // Verificar que NO aparece el contenido esperado (acceso denegado)
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'No tienes permisos')]")));
            // Si aparece el mensaje de error, el test pasa
        } catch (org.openqa.selenium.TimeoutException e) {
            // Si no aparece el mensaje de error, verificar que tampoco aparecen los datos
            assertThat(driver.getPageSource()).doesNotContain("Nota Media");
        }
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

        // Presionar Enter en el campo de password para enviar el formulario
        passwordInput.sendKeys(org.openqa.selenium.Keys.RETURN);

        // Esperar a que la URL cambie (salga de /auth/login)
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
        assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
    }
}
