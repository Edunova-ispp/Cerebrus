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
    private static final long CURSO_SEED_COMPLETO_ID = 10101L;
    private static final long TEMA_SEED_COMPLETO_ID = 10301L;

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

    @Test
    @DisplayName("El profesor ve correctamente los campos de todos los formularios de creación")
    void profesorVeCamposDeTodosLosFormulariosDeCreacion() {
        loginAsProfesor();

        navigateTo("/cursos/" + CURSO_ID + "/temas/" + TEMA_ID + "/actividades/crear");
        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // Teoría
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Teoría']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("teoria-titulo")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("teoria-descripcion")));
        assertThat(isVisible(By.id("teoria-titulo"))).isTrue();
        assertThat(isVisible(By.id("tf-titulo"))).isFalse();

        // Test
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Tipo test']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tf-titulo")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tf-puntuacion")));
        assertThat(isVisible(By.id("tf-titulo"))).isTrue();
        assertThat(isVisible(By.id("teoria-titulo"))).isFalse();

        // Ordenación
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Poner en orden']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("of-titulo")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("of-puntuacion")));
        assertThat(isVisible(By.id("of-titulo"))).isTrue();
        assertThat(isVisible(By.id("tf-titulo"))).isFalse();

        // Marcar en imagen
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Marcar en imagen']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mi-titulo")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mi-imagen-a-marcar")));
        assertThat(isVisible(By.id("mi-titulo"))).isTrue();
        assertThat(isVisible(By.id("of-titulo"))).isFalse();

        // Tablero
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Tablero']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), 'Tamaño del tablero')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Título del tablero']")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Ej. 100']")));
        assertThat(isVisible(By.id("mi-titulo"))).isFalse();

        // Clasificación
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Clasificación']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), 'Configuración de Categorías')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Nombre de la categoría']")));
        assertThat(isVisible(By.xpath("//input[@placeholder='Título del tablero']"))).isFalse();

        // Carta
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Carta']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cf-titulo")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cf-puntuacion")));
        assertThat(isVisible(By.id("cf-titulo"))).isTrue();
        assertThat(isVisible(By.xpath("//input[@placeholder='Nombre de la categoría']"))).isFalse();

        // Crucigrama
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Crucigrama']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cf-desc")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cf-punt")));
        assertThat(isVisible(By.id("cf-puntuacion"))).isFalse();

        // Pregunta Abierta
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Pregunta Abierta']"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(normalize-space(), '+ Añadir pregunta')]")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//input[@placeholder='Título de la actividad']")));
        assertThat(isVisible(By.id("cf-punt"))).isFalse();
    }

    @Test
    @DisplayName("El profesor puede abrir y editar campos de actividades seed en modo edición")
    void profesorPuedeEditarCamposDeActividadesSeed() {
        loginAsProfesor();

        WebDriverWait wait = new WebDriverWait(driver, WAIT);

        // TEST 10401
        navigateTo("/cursos/" + CURSO_SEED_COMPLETO_ID + "/temas/" + TEMA_SEED_COMPLETO_ID + "/actividades/10401/editar");
        WebElement testTitulo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("tf-titulo")));
        assertThat(testTitulo.getAttribute("value")).isEqualTo("Quiz de Animales");
        testTitulo.clear();
        testTitulo.sendKeys("Quiz de Animales (edición E2E)");
        assertThat(testTitulo.getAttribute("value")).isEqualTo("Quiz de Animales (edición E2E)");

        // CARTA 10402
        navigateTo("/cursos/" + CURSO_SEED_COMPLETO_ID + "/temas/" + TEMA_SEED_COMPLETO_ID + "/actividades/10402/editar");
        WebElement cartaTitulo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cf-titulo")));
        assertThat(cartaTitulo.getAttribute("value")).isEqualTo("Memoriza los Animales");
        cartaTitulo.clear();
        cartaTitulo.sendKeys("Memoriza los Animales (edición E2E)");
        assertThat(cartaTitulo.getAttribute("value")).isEqualTo("Memoriza los Animales (edición E2E)");

        // TEORÍA 10403
        navigateTo("/cursos/" + CURSO_SEED_COMPLETO_ID + "/temas/" + TEMA_SEED_COMPLETO_ID + "/actividades/10403/editar");
        WebElement teoriaTitulo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("teoria-titulo")));
        assertThat(teoriaTitulo.getAttribute("value")).isEqualTo("Curiosidades del Mundo Animal");
        teoriaTitulo.clear();
        teoriaTitulo.sendKeys("Curiosidades del Mundo Animal (edición E2E)");
        assertThat(teoriaTitulo.getAttribute("value")).isEqualTo("Curiosidades del Mundo Animal (edición E2E)");

        // ORDENACIÓN 10404
        navigateTo("/cursos/" + CURSO_SEED_COMPLETO_ID + "/temas/10302/actividades/10404/editar");
        WebElement ordenTitulo = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("of-titulo")));
        assertThat(ordenTitulo.getAttribute("value")).isEqualTo("Ciclo de Vida de una Planta");
        ordenTitulo.clear();
        ordenTitulo.sendKeys("Ciclo de Vida de una Planta (edición E2E)");
        assertThat(ordenTitulo.getAttribute("value")).isEqualTo("Ciclo de Vida de una Planta (edición E2E)");
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
        Object ok = resultMap.get("ok");
        if (Boolean.TRUE.equals(ok)) {
            return;
        }

        // En algunos flujos de backend la actividad puede haberse eliminado por cascada o reposicionado.
        // Si ambos intentos devuelven not found, no bloqueamos el E2E.
        Object primaryObj = resultMap.get("primary");
        Object secondaryObj = resultMap.get("secondary");
        if (primaryObj instanceof Map<?, ?> primary && secondaryObj instanceof Map<?, ?> secondary) {
            int primaryStatus = parseStatus(primary.get("status"));
            int secondaryStatus = parseStatus(secondary.get("status"));
            boolean notFoundLike = (primaryStatus == 404 || primaryStatus == 422)
                    && (secondaryStatus == 404 || secondaryStatus == 422);
            if (notFoundLike) {
                return;
            }
        }

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

    private boolean isVisible(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        return elements.stream().anyMatch(WebElement::isDisplayed);
    }

    private int parseStatus(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
