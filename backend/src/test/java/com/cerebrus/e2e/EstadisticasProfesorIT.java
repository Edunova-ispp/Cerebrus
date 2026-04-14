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

         
    @Test
    @DisplayName("Sin autenticacion, rutas de estadisticas de profesor redirigen a login")
    void rutasEstadisticasSinAutenticacionRedirigenALogin() {
        driver.manage().deleteAllCookies();

        List<String> rutasProtegidas = List.of(
                "/cursos/10101"
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

        navigateTo("/cursos/10101");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(normalize-space(), 'Estadísticas')]"))).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(normalize-space(), 'Resumen general')]"))).click();

        // 6. Ver detalle de un tema específico (Volviendo a Temas y pulsando 'Ver')
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(normalize-space(), 'Alumnos')]"))).click();
        driver.findElements(By.tagName("button"))
      .forEach(b -> System.out.println("BTN: [" + b.getText() + "]"));
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(normalize-space(), 'Estadísticas')]"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(normalize-space(), 'Tendencias')]"))).click();
                wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(normalize-space(), 'Análisis')]"))).click();



    }

    @Test
    @DisplayName("Profesor no propietario no puede cargar estadisticas del curso ajeno")
    void profesorNoPropietarioNoPuedeCargarEstadisticasCursoAjeno() {
        login(PROFESOR_NO_OWNER, PASSWORD);
        navigateTo("/cursos/10101");
        
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
}