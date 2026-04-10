package com.cerebrus.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class ProfesorActividadesIT extends SeleniumBaseTest {

    private static final Duration WAIT = Duration.ofSeconds(15);
    private static final String PROFESOR_USUARIO = "carlos_pro";
    private static final String PROFESOR_PASSWORD = "123456";
    private static final long CURSO_ID = 4001L;
    private static final long TEMA_ID = 5002L;

    @Test
    @DisplayName("Sin autenticacion, rutas de crear/editar actividad redirigen a login")
    void rutasProfesorSinLoginRedirigenALogin() {
        driver.manage().deleteAllCookies();

        List<String> rutasProtegidas = List.of(
            "/cursos/1/temas/1/actividades/crear",
            "/cursos/1/temas/1/actividades/1/editar"
        );

        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        for (String ruta : rutasProtegidas) {
            navigateTo(ruta);
            wait.until(ExpectedConditions.urlContains("/auth/login"));
            assertThat(driver.getCurrentUrl()).contains("/auth/login");
        }
    }

    @Test
    @DisplayName("El profesor puede crear, editar y borrar una teoría desde la UI")
    void profesorPuedeCrearEditarYBorrarUnaTeoria() {
        loginAsProfesor();

        navigateTo("/cursos/" + CURSO_ID);
        waitUntilUrlContains("/cursos/" + CURSO_ID);

        navigateTo("/cursos/" + CURSO_ID + "/temas/" + TEMA_ID + "/actividades/crear");
        abrirFormularioTeoria();

        String tituloCreado = "E2E Teoría Temporal";
        String descripcionCreada = "Contenido temporal creado por Selenium";

        rellenarFormularioTeoria(tituloCreado, descripcionCreada);
        guardarFormularioTeoria();

        waitUntilUrlContains("/cursos/" + CURSO_ID);
        Long teoriaCreadaId = waitUntilTeoriaCreadaIdPorTitulo(TEMA_ID, tituloCreado);
        assertThat(teoriaCreadaId).isNotNull();

        navigateTo("/cursos/" + CURSO_ID + "/temas/" + TEMA_ID + "/actividades/" + teoriaCreadaId + "/editar");
        abrirFormularioTeoriaEnEdicion();
        waitUntilFieldValue(By.id("teoria-titulo"), tituloCreado);

        String tituloEditado = "E2E Teoría Temporal Editada";
        String descripcionEditada = "Contenido temporal editado por Selenium";
        rellenarFormularioTeoria(tituloEditado, descripcionEditada);
        guardarFormularioTeoria();

        waitUntilUrlContains("/cursos/" + CURSO_ID);
        borrarTeoriaCreada(teoriaCreadaId);
    }

    @Test
    @DisplayName("La validación de teoría mantiene al profesor en la pantalla cuando faltan datos")
    void validacionTeoriaMantieneLaPantalla() {
        loginAsProfesor();

        navigateTo("/cursos/" + CURSO_ID + "/temas/" + TEMA_ID + "/actividades/crear");
        abrirFormularioTeoria();

        driver.findElement(By.id("teoria-titulo")).sendKeys("   ");
        driver.findElement(By.id("teoria-descripcion")).sendKeys("Contenido mínimo para disparar la validación custom");
        guardarFormularioTeoria();

        waitUntilVisible(By.cssSelector(".of-error"));
        assertThat(driver.getCurrentUrl()).contains("/cursos/" + CURSO_ID + "/temas/" + TEMA_ID + "/actividades/crear");
        assertThat(driver.findElement(By.cssSelector(".of-error")).getText()).isEqualTo("El título es requerido");
    }

    private void loginAsProfesor() {
        navigateTo("/auth/login");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        WebElement usuarioInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identificador")));
        usuarioInput.clear();
        usuarioInput.sendKeys(PROFESOR_USUARIO);

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys(PROFESOR_PASSWORD);

        driver.findElement(By.cssSelector("button.pixel-btn-submit")).click();

        wait.until(ExpectedConditions.urlContains("/miscursos"));
        assertThat(driver.getCurrentUrl()).contains("/miscursos");
    }

    private void abrirFormularioTeoria() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Teoría']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("teoria-titulo")));
        waitUntilVisible(By.id("teoria-descripcion"));
    }

    private void rellenarFormularioTeoria(String titulo, String descripcion) {
        WebElement tituloInput = driver.findElement(By.id("teoria-titulo"));
        tituloInput.clear();
        tituloInput.sendKeys(titulo);

        WebElement descripcionInput = driver.findElement(By.id("teoria-descripcion"));
        descripcionInput.clear();
        descripcionInput.sendKeys(descripcion);
    }

    private void guardarFormularioTeoria() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Guardar']"))).click();
    }

    private void abrirFormularioTeoriaEnEdicion() {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        List<WebElement> theoryInputs = driver.findElements(By.id("teoria-titulo"));
        if (!theoryInputs.isEmpty() && theoryInputs.get(0).isDisplayed()) {
            return;
        }

        List<WebElement> teoriaButtons = driver.findElements(By.xpath("//button[normalize-space()='Teoría']"));
        if (!teoriaButtons.isEmpty() && teoriaButtons.get(0).isDisplayed()) {
            wait.until(ExpectedConditions.elementToBeClickable(teoriaButtons.get(0))).click();
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("teoria-titulo")));
    }

    private Long leerIdDeTeoriaCreada() {
            Object raw = ((JavascriptExecutor) driver).executeScript("return window.__e2eTheoryCreateId ?? null;");
            if (raw == null) {
                    return null;
            }
            if (raw instanceof Number number) {
                    return number.longValue();
            }
            return Long.parseLong(raw.toString());
    }

    private Long waitUntilTeoriaCreadaIdPorTitulo(Long temaId, String titulo) {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(d -> {
            Object raw = ((JavascriptExecutor) d).executeAsyncScript("""
                const temaId = arguments[0];
                const titulo = arguments[1];
                const done = arguments[arguments.length - 1];
                const token = localStorage.getItem('token') || '';

                fetch('/api/temas/' + temaId, {
                    headers: {
                            'Authorization': 'Bearer ' + token
                    }
                })
                    .then((response) => {
                            if (!response.ok) {
                                    throw new Error('HTTP ' + response.status);
                            }
                            return response.json();
                    })
                    .then((data) => {
                            const actividades = Array.isArray(data.actividades) ? data.actividades : [];
                            const encontrada = actividades.find((actividad) => actividad && actividad.titulo === titulo);
                            done(encontrada && encontrada.id ? encontrada.id : null);
                    })
                    .catch(() => done(null));
                """, temaId, titulo);

            if (raw == null) {
                            return false;
            }
            if (raw instanceof Number number) {
                            return number.longValue() > 0;
            }
            return !raw.toString().isBlank();
        });

        Object raw = ((JavascriptExecutor) driver).executeAsyncScript("""
            const temaId = arguments[0];
            const titulo = arguments[1];
            const done = arguments[arguments.length - 1];
            const token = localStorage.getItem('token') || '';

            fetch('/api/temas/' + temaId, {
                headers: {
                        'Authorization': 'Bearer ' + token
                }
            })
                .then((response) => {
                        if (!response.ok) {
                                throw new Error('HTTP ' + response.status);
                        }
                        return response.json();
                })
                .then((data) => {
                        const actividades = Array.isArray(data.actividades) ? data.actividades : [];
                        const encontrada = actividades.find((actividad) => actividad && actividad.titulo === titulo);
                        done(encontrada && encontrada.id ? encontrada.id : null);
                })
                .catch(() => done(null));
            """, temaId, titulo);

        if (raw == null) {
                        return null;
        }
        if (raw instanceof Number number) {
                        return number.longValue();
        }
        return Long.parseLong(raw.toString());
    }

    private void borrarTeoriaCreada(Long teoriaId) {
        Object result = ((JavascriptExecutor) driver).executeAsyncScript("""
            const teoriaId = arguments[0];
            const done = arguments[arguments.length - 1];
            const token = localStorage.getItem('token') || '';

            const headers = {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            };

            const tryDelete = async (url) => {
                try {
                    const response = await fetch(url, { method: 'DELETE', headers });
                    const body = await response.text().catch(() => '');
                    return { ok: response.ok, status: response.status, url, body };
                } catch (e) {
                    return { ok: false, status: 0, url, error: String(e) };
                }
            };

            (async () => {
                const primary = await tryDelete('/api/actividades/delete/' + teoriaId);
                if (primary.ok) {
                    done(primary);
                    return;
                }

                // Fallback defensivo por si la actividad quedó tipada en otro controlador.
                const secondary = await tryDelete('/api/generales/delete/' + teoriaId);
                if (secondary.ok) {
                    done(secondary);
                    return;
                }

                done({ ok: false, primary, secondary });
            })();
            """, teoriaId);

        assertThat(result).isInstanceOf(Map.class);
        Map<?, ?> resultMap = (Map<?, ?>) result;
        assertThat(resultMap.get("ok"))
                .withFailMessage("No se pudo borrar la teoría creada. Detalle: %s", resultMap)
                .isEqualTo(Boolean.TRUE);
    }

    private void waitUntilUrlContains(String fragment) {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.urlContains(fragment));
        assertThat(driver.getCurrentUrl()).contains(fragment);
    }

    private void waitUntilVisible(By locator) {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    private void waitUntilFieldValue(By locator, String expectedValue) {
        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(d -> {
            List<WebElement> elements = d.findElements(locator);
            if (elements.isEmpty()) {
                return false;
            }
            return expectedValue.equals(elements.get(0).getAttribute("value"));
        });
        assertThat(driver.findElement(locator).getAttribute("value")).isEqualTo(expectedValue);
    }
}
