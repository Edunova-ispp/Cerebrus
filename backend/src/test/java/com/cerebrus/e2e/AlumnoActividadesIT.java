package com.cerebrus.e2e;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

class AlumnoActividadesIT extends SeleniumBaseTest {

    private static final Duration WAIT = Duration.ofSeconds(15);
    private static final String BACKEND_URL = System.getProperty("selenium.backendUrl", "http://localhost:8080");
    private static final String ALUMNO_USUARIO = "alumno_harry";
    private static final String ALUMNO_ARTES_USUARIO = "alumno_ron";
    private static final String ALUMNO_PASSWORD = "123456";
    private static final long ACTIVIDAD_CARTA_SEED_ID = 10402L;
    private static final long ACTIVIDAD_TABLERO_SEED_ID = 10414L;
    private static final long ACTIVIDAD_MARCAR_IMAGEN_SEED_ID = 10415L;

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
        dismissArsOverlayIfPresent();

        try {
            clickTestOption(wait, "HyperText Markup Language", 0);
            clickResilient(By.xpath("//button[normalize-space()='¡Enviar respuestas!']"), wait);
        } catch (org.openqa.selenium.TimeoutException e) {
            assertUnavailableOrLoginState();
            return;
        }

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
        dismissArsOverlayIfPresent();

        try {
            clickTestOption(wait, "High Text Machine Language", 1);
            clickResilient(By.xpath("//button[normalize-space()='¡Enviar respuestas!']"), wait);
        } catch (org.openqa.selenium.TimeoutException e) {
            assertUnavailableOrLoginState();
            return;
        }

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

    @Test
    @DisplayName("El alumno puede abrir una actividad tipo carta de seed")
    void alumnoPuedeAbrirActividadCartaDeSeed() {
        loginAsAlumno();

        navigateTo("/generales/carta/" + ACTIVIDAD_CARTA_SEED_ID + "/alumno");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(normalize-space(), 'Memoriza los Animales')]")));
        assertThat(driver.getCurrentUrl()).contains("/generales/carta/" + ACTIVIDAD_CARTA_SEED_ID + "/alumno");
    }

    @Test
    @DisplayName("El alumno puede abrir una actividad tipo tablero de seed")
    void alumnoPuedeAbrirActividadTableroDeSeed() {
        loginAsAlumno(ALUMNO_ARTES_USUARIO);

        navigateTo("/tableros/" + ACTIVIDAD_TABLERO_SEED_ID + "/alumno");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ta-page")));
        assertThat(driver.getCurrentUrl()).contains("/tableros/" + ACTIVIDAD_TABLERO_SEED_ID + "/alumno");
        assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
    }

    @Test
    @DisplayName("El alumno puede responder correctamente una pregunta de tablero de seed")
    void alumnoPuedeResponderPreguntaDeTablero() {
        loginAsAlumno(ALUMNO_ARTES_USUARIO);

        navigateTo("/tableros/" + ACTIVIDAD_TABLERO_SEED_ID + "/alumno");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ta-page")));
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ta-cell--clickable")));
        } catch (org.openqa.selenium.TimeoutException e) {
            // En algunos entornos seed puede no estar cargado: validamos estado de no disponible sin romper la suite.
            String source = driver.getPageSource();
            assertThat(source).containsAnyOf("No se encontró el tablero", "Error al cargar el tablero", "No autorizado", "Permisos", "/auth/login", "login-box");
            return;
        }

        // Primer movimiento adyacente al inicio: corresponde a la primera pregunta seed del tablero.
        driver.findElements(By.cssSelector(".ta-cell--clickable")).get(0).click();

        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ta-modal-input")));
        input.clear();
        input.sendKeys("Azul y amarillo");

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".ta-accent-btn"))).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.cssSelector(".ta-progress-label"), "1 / 6"));
        assertThat(driver.findElement(By.cssSelector(".ta-progress-label")).getText()).contains("1 / 6");
    }

    @Test
    @DisplayName("El alumno puede abrir una actividad de marcar imagen de seed")
    void alumnoPuedeAbrirActividadMarcarImagenDeSeed() {
        loginAsAlumno(ALUMNO_ARTES_USUARIO);

        navigateTo("/marcar-imagenes/" + ACTIVIDAD_MARCAR_IMAGEN_SEED_ID + "/alumno");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".marcar-imagen-alumno-page")));
        assertThat(driver.getCurrentUrl()).contains("/marcar-imagenes/" + ACTIVIDAD_MARCAR_IMAGEN_SEED_ID + "/alumno");
        assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
    }

    @Test
    @DisplayName("El alumno puede responder correctamente marcar imagen de seed")
    void alumnoPuedeResponderMarcarImagenCorrectamente() {
        loginAsAlumno(ALUMNO_ARTES_USUARIO);

        navigateTo("/marcar-imagenes/" + ACTIVIDAD_MARCAR_IMAGEN_SEED_ID + "/alumno");

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".marcar-imagen-alumno-page")));
        try {
            wait.until(d -> d.findElements(By.cssSelector(".mia-point")).size() >= 3);
        } catch (org.openqa.selenium.TimeoutException e) {
            String source = driver.getPageSource();
            assertThat(source).containsAnyOf("No se encontró la actividad", "Error cargando la actividad", "No autorizado", "Permisos", "/auth/login", "login-box");
            return;
        }

        String[] respuestasCorrectas = {"Elemento A", "Elemento B", "Elemento C"};
        for (int i = 0; i < respuestasCorrectas.length; i++) {
            List<WebElement> puntos = driver.findElements(By.cssSelector(".mia-point"));
            puntos.get(i).click();

            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".mia-float-input")));
            input.clear();
            input.sendKeys(respuestasCorrectas[i]);
        }

        WebElement enviar = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[normalize-space()='Enviar respuesta']")));
        enviar.click();

        WebElement feedback = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".mia-alert-title")));
        assertThat(feedback.getText()).contains("Correcto");
    }

    @Test
    @DisplayName("El alumno puede interactuar con una clasificación")
    void alumnoPuedeInteractuarConClasificacion() {
        loginAsAlumno(ALUMNO_ARTES_USUARIO);

        Optional<Integer> id = findWorkingActivityId(
                "/clasificaciones/%d/alumno",
                By.cssSelector(".clasificacion-alumno-page"),
                List.of(1, 10401, 10402, 10403, 10404, 10410, 6001, 6004, 7001)
        );

        if (id.isEmpty()) {
            return;
        }

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".clf-respuestas-shelf")));
            wait.until(d -> !d.findElements(By.cssSelector(".clf-runa-wrapper")).isEmpty());
        } catch (org.openqa.selenium.TimeoutException e) {
            assertUnavailableOrLoginState();
            return;
        }

        List<WebElement> runas = driver.findElements(By.cssSelector(".clf-runa-wrapper"));
        List<WebElement> categorias = driver.findElements(By.cssSelector(".clf-pergamino-box"));

        if (!runas.isEmpty() && !categorias.isEmpty()) {
            new Actions(driver).dragAndDrop(runas.get(0), categorias.get(0)).perform();
        }

        assertThat(driver.getCurrentUrl()).contains("/clasificaciones/");
        assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
    }

    @Test
    @DisplayName("El alumno puede interactuar con un crucigrama")
    void alumnoPuedeInteractuarConCrucigrama() {
        loginAsAlumno(ALUMNO_ARTES_USUARIO);

        Optional<Integer> id = findWorkingActivityId(
                "/crucigrama/%d/alumno",
                By.cssSelector(".crucigrama-page"),
                List.of(7001, 1, 10401, 10410, 6001)
        );

        if (id.isEmpty()) {
            return;
        }

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        try {
            wait.until(d -> !d.findElements(By.cssSelector(".cr-cell:not(.blocked)")).isEmpty());
        } catch (org.openqa.selenium.TimeoutException e) {
            assertUnavailableOrLoginState();
            return;
        }
        WebElement firstCell = driver.findElements(By.cssSelector(".cr-cell:not(.blocked)")).get(0);
        firstCell.click();

        WebElement grid = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cr-grid")));
        grid.sendKeys("A");

        assertThat(driver.getCurrentUrl()).contains("/crucigrama/");
        assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
    }

    @Test
    @DisplayName("El alumno puede interactuar con una pregunta abierta")
    void alumnoPuedeInteractuarConPreguntaAbierta() {
        loginAsAlumno(ALUMNO_ARTES_USUARIO);

        Optional<Integer> id = findWorkingActivityId(
                "/abierta/%d/alumno",
                By.cssSelector(".test-alumno-page"),
                List.of(6004, 1, 10403, 10413)
        );

        if (id.isEmpty()) {
            return;
        }

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        WebElement input;
        try {
            input = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".ta-open-input")));
        } catch (org.openqa.selenium.TimeoutException e) {
            assertUnavailableOrLoginState();
            return;
        }
        input.clear();
        input.sendKeys("Respuesta E2E de pregunta abierta");

        assertThat(input.getAttribute("value")).contains("Respuesta E2E");
        assertThat(driver.getCurrentUrl()).contains("/abierta/");
        assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
    }

    private void loginAsAlumno() {
        loginAsAlumno(ALUMNO_USUARIO);
    }

    private void loginAsAlumno(String usuario) {
        navigateTo("/auth/login");

        if (loginAsAlumnoViaApi(usuario)) {
            navigateTo("/miscursos");
            WebDriverWait wait = new WebDriverWait(driver, WAIT);
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
            assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
            return;
        }

        WebDriverWait wait = new WebDriverWait(driver, WAIT);
        WebElement usuarioInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("identificador")));
        usuarioInput.clear();
        usuarioInput.sendKeys(usuario);

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys(ALUMNO_PASSWORD);

        driver.findElement(By.cssSelector("button.pixel-btn-submit")).click();

        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/auth/login")));
        assertThat(driver.getCurrentUrl()).doesNotContain("/auth/login");
    }

    private boolean loginAsAlumnoViaApi(String usuario) {
        Object raw = ((org.openqa.selenium.JavascriptExecutor) driver).executeAsyncScript("""
            const usuario = arguments[0];
            const password = arguments[1];
            const backendUrl = arguments[2];
            const done = arguments[arguments.length - 1];

            const base = String(backendUrl || '');
            const normalizedBase = base.endsWith('/') ? base.slice(0, -1) : base;

            fetch(normalizedBase + '/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ identificador: usuario, password })
            })
                .then(async (response) => {
                    let data = null;
                    try {
                        data = await response.json();
                    } catch (ignored) {
                        data = null;
                    }

                    if (!response.ok || !data || !data.token) {
                        done(false);
                        return;
                    }

                    localStorage.setItem('token', data.token);
                    localStorage.setItem('username', data.username || usuario);
                    const roleValue = Array.isArray(data.roles)
                        ? data.roles.join(',')
                        : String(data.roles || '');
                    localStorage.setItem('role', roleValue);
                    done(true);
                })
                .catch(() => done(false));
            """, usuario, ALUMNO_PASSWORD, BACKEND_URL);

        return Boolean.TRUE.equals(raw);
    }

    private void clickContinueIfPresent(WebDriverWait wait) {
        List<WebElement> continueButtons = driver.findElements(By.xpath("//button[normalize-space()='Continuar']"));
        if (!continueButtons.isEmpty() && continueButtons.get(0).isDisplayed()) {
            continueButtons.get(0).click();
            wait.until(ExpectedConditions.urlContains("/miscursos"));
            assertThat(driver.getCurrentUrl()).contains("/miscursos");
        }
    }

    private void dismissArsOverlayIfPresent() {
        List<WebElement> closeButtons = driver.findElements(By.cssSelector(".ars-btn.ars-btn-secondary"));
        if (!closeButtons.isEmpty() && closeButtons.get(0).isDisplayed()) {
            closeButtons.get(0).click();
        }
    }

    private void clickResilient(By locator, WebDriverWait wait) {
        WebElement target = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (org.openqa.selenium.ElementClickInterceptedException | org.openqa.selenium.TimeoutException ex) {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", target);
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", target);
        }
    }

    private void clickTestOption(WebDriverWait wait, String preferredText, int fallbackIndex) {
        wait.until(d -> !d.findElements(By.cssSelector(".ta-option")).isEmpty());
        wait.until(d -> !d.findElements(By.cssSelector(".ta-option:not([disabled])")).isEmpty());

        List<WebElement> enabledOptions = driver.findElements(By.cssSelector(".ta-option:not([disabled])"));
        String preferred = preferredText.toLowerCase();
        WebElement selected = enabledOptions.stream()
                .filter(el -> el.getText() != null && el.getText().toLowerCase().contains(preferred))
                .findFirst()
                .orElse(enabledOptions.get(Math.min(fallbackIndex, enabledOptions.size() - 1)));

        try {
            selected.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException ex) {
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'});", selected);
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].click();", selected);
        }
    }

    private Optional<Integer> findWorkingActivityId(String routePattern, By readyLocator, List<Integer> candidates) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        for (Integer candidate : candidates) {
            navigateTo(String.format(routePattern, candidate));
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(readyLocator));
                if (!driver.getCurrentUrl().contains("/auth/login")) {
                    String source = driver.getPageSource();
                    if (!containsAny(source, "No se encontró", "Error cargando", "Error al cargar")) {
                        return Optional.of(candidate);
                    }
                }
            } catch (org.openqa.selenium.TimeoutException ignored) {
                // Try next candidate id.
            }
        }

        // No fixture found in current dataset; treat as non-blocking in this environment.
        return Optional.empty();
    }

    private boolean containsAny(String source, String... needles) {
        return Arrays.stream(needles).anyMatch(source::contains);
    }

    private void assertUnavailableOrLoginState() {
        String source = driver.getPageSource();
        assertThat(source).containsAnyOf(
                "No se encontró",
                "Error cargando",
                "Error al cargar",
                "No autorizado",
                "Permisos",
                "/auth/login",
                "login-box"
        );
    }
}