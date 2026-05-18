package com.cerebrus.e2e;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Tests E2E del flujo de Temas.
 *
 * Crean un curso propio, inscriben al alumno y validan el flujo completo
 * de temas desde ambos perfiles.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TemasIT extends SeleniumBaseTest {

        private static final Duration WAIT = Duration.ofSeconds(20);
        private static final String MAESTRO_USER = "profe_snape";
        private static final String MAESTRO_PASSWORD = "123456";
        private static final String ALUMNO_USER = "alumno_harry";
        private static final String ALUMNO_PASSWORD = "123456";
        private static final String COURSE_TITLE = "Curso de Temas Selenium " + System.currentTimeMillis();
        private static final String COURSE_DESCRIPTION = "Curso de prueba para Selenium";
        private static final String COURSE_CUSTOM_CODE = "TM" + System.currentTimeMillis();
        private static final String COURSE_THEME_TITLE = "Introducción a las fracciones";
        private static final String RENAMED_THEME_TITLE = "Fracciones avanzadas";

        private record CourseContext(String title, String code, String id) {}

        private static CourseContext sharedCourse;
        private static String currentThemeTitle = COURSE_THEME_TITLE;

        @Test
        @Order(1)
        @DisplayName("El maestro crea un curso visible y deja al alumno inscrito")
        void maestroCreaCursoVisibleYDejaAlumnoInscrito() {
                login(MAESTRO_USER, MAESTRO_PASSWORD);
                sharedCourse = createCourseAndOpenDetail(COURSE_TITLE);

                login(ALUMNO_USER, ALUMNO_PASSWORD);
                enrollStudentInCourse(sharedCourse.code());

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());
                assertThat(driver.getCurrentUrl()).contains("/cursos/" + sharedCourse.id());
        }

        @Test
        @Order(2)
        @DisplayName("El maestro ve Mis Cursos y abre el curso")
        void maestroIniciaSesionVeMisCursosYAbreElCurso() {
                ensureSharedCourseReady();
                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/miscursos");

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement title = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.mis-cursos-title"))
                );

                assertThat(title.getText()).isEqualToIgnoringCase("MIS CURSOS");
pageWait.until(d -> !d.findElements(By.cssSelector("div.curso-card")).isEmpty());
assertThat(driver.findElements(By.cssSelector("div.curso-card"))).isNotEmpty();
                navigateTo("/cursos/" + sharedCourse.id());
                pageWait.until(ExpectedConditions.urlContains("/cursos/" + sharedCourse.id()));

                WebElement addThemeButton = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.ltp-btn-añadir"))
                );
                assertThat(addThemeButton.getText()).contains("Añadir tema");

                List<WebElement> themeItems = driver.findElements(By.cssSelector("div.ltp-tema-bloque"));
                if (themeItems.isEmpty()) {
                        WebElement emptyState = pageWait.until(
                                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.ltp-vacio"))
                        );
                        assertThat(emptyState.getText()).contains("No hay temas todavía");
                } else {
                        assertThat(themeItems).isNotEmpty();
                }
        }

        @Test
        @Order(3)
        @DisplayName("El maestro abre el formulario y crea un tema")
        void maestroPuedeAbrirFormularioCrearTemaYCrearTema() {
                ensureSharedCourseReady();
                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement addThemeButton = pageWait.until(
                                ExpectedConditions.presenceOfElementLocated(By.cssSelector("button.ltp-btn-añadir"))
                );
                if (driver instanceof JavascriptExecutor javascriptExecutor) {
                        javascriptExecutor.executeScript("arguments[0].click();", addThemeButton);
                } else {
                        addThemeButton.click();
                }

                WebElement heading = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.welcome-text"))
                );
                assertThat(heading.getText()).isEqualToIgnoringCase("Crear tema");

                WebElement titleInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))
                );
                assertThat(titleInput.isDisplayed()).isTrue();

                WebElement submitButton = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.pixel-btn-submit-main"))
                );
                assertThat(submitButton.getText()).contains("Crear Tema");

                createThemeInCurrentCourse(COURSE_THEME_TITLE);
                assertTeacherSeesTheme(COURSE_THEME_TITLE);
        }

        @Test
        @Order(4)
        @DisplayName("El maestro edita el tema y el alumno ve el nuevo nombre")
        void maestroPuedeEditarTemaYAlumnoVeTituloActualizado() {
                ensureSharedCourseReady();

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                pageWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("p.ltp-estado")));
                if (!themeExists(currentThemeTitle)) {
                        createThemeInCurrentCourse(currentThemeTitle);
                        navigateTo("/cursos/" + sharedCourse.id());
                        pageWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("p.ltp-estado")));
                }
                WebElement themeItem = waitForThemeInList(currentThemeTitle, Duration.ofSeconds(60));
                WebElement editButton = themeItem.findElement(By.cssSelector("button[title='Editar']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);

                WebElement heading = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.welcome-text"))
                );
                assertThat(heading.getText()).isEqualToIgnoringCase("Editar tema");

                WebElement titleInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))
                );
                titleInput.clear();
                titleInput.sendKeys(RENAMED_THEME_TITLE);

                WebElement submitButton = pageWait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button.pixel-btn-submit-main"))
                );
                submitButton.click();

                pageWait.until(ExpectedConditions.urlContains("/cursos/" + sharedCourse.id()));
                assertTeacherSeesTheme(RENAMED_THEME_TITLE);

                currentThemeTitle = RENAMED_THEME_TITLE;

                login(ALUMNO_USER, ALUMNO_PASSWORD);
                openStudentCourseMap(sharedCourse.id());
                assertStudentSeesThemeInMap(currentThemeTitle);
        }

        @Test
        @Order(5)
        @DisplayName("El maestro no puede crear un tema con título inválido y permanece en el formulario")
        void maestroNoPuedeCrearTemaConTituloInvalidoYPermaneceEnFormulario() {
                ensureSharedCourseReady();
                login(MAESTRO_USER, MAESTRO_PASSWORD);

                navigateTo("/cursos/" + sharedCourse.id());
                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);

                WebElement addThemeButton = pageWait.until(
                        ExpectedConditions.presenceOfElementLocated(By.cssSelector("button.ltp-btn-añadir"))
                );
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addThemeButton);

                pageWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.welcome-text")));

                WebElement titleInput = pageWait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))
                );
                titleInput.clear();
                titleInput.sendKeys("   ");

                driver.findElement(By.cssSelector("button.pixel-btn-submit-main")).click();

                // Cerrar alerta si aparece tras submit inválido
                try {
                        new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.alertIsPresent());
                        driver.switchTo().alert().dismiss();
                } catch (org.openqa.selenium.TimeoutException ignored) {}

                WebElement errorMsg = pageWait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error-msg"))
                );
                assertThat(errorMsg.getText()).containsIgnoringCase("requerido");
                assertThat(driver.getCurrentUrl()).contains("/cursos/" + sharedCourse.id());
        }

        @Test
        @Order(6)
        @DisplayName("El maestro no puede editar un tema con título inválido y permanece en el formulario")
        void maestroNoPuedeEditarTemaConTituloInvalidoYPermaneceEnFormulario() {
                ensureSharedCourseReady();
                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                pageWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("p.ltp-estado")));
                if (!themeExists(currentThemeTitle)) {
                        createThemeInCurrentCourse(currentThemeTitle);
                        navigateTo("/cursos/" + sharedCourse.id());
                        pageWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("p.ltp-estado")));
                }
                WebElement themeItem = waitForThemeInList(currentThemeTitle, Duration.ofSeconds(60));
                WebElement editButton = themeItem.findElement(By.cssSelector("button[title='Editar']"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editButton);
                WebElement heading = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.welcome-text"))
                );
                assertThat(heading.getText()).isEqualToIgnoringCase("Editar tema");

                WebElement titleInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))
                );
                titleInput.clear();
                titleInput.sendKeys("   ");

                WebElement submitButton = pageWait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button.pixel-btn-submit-main"))
                );
                submitButton.click();

                WebElement errorMsg = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.error-msg"))
                );
                assertThat(errorMsg.getText()).containsIgnoringCase("obligatorio");
                assertThat(driver.getCurrentUrl()).contains("/cursos/" + sharedCourse.id());
        }

        @Test
        @Order(7)
        @DisplayName("Si el curso se oculta, el alumno deja de ver el tema")
        void alumnoNoVeElTemaCuandoElCursoEsNoVisible() {
                ensureSharedCourseReady();

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                setCourseVisibilityInUi(false);

                login(ALUMNO_USER, ALUMNO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                pageWait.until(ExpectedConditions.urlContains("/misCursos"));

                assertThat(driver.getCurrentUrl()).contains("/misCursos");
                assertThat(driver.findElements(By.cssSelector("span.ltp-tema-titulo"))).isEmpty();

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                setCourseVisibilityInUi(true);
        }

        @Test
        @Order(8)
        @DisplayName("El alumno ve el tema y después deja de verlo al borrarlo")
        void alumnoPuedeVerTemaCreadoYLuegoDejaDeVerloTrasBorrado() {
                ensureSharedCourseReady();

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());
                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                pageWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("p.ltp-estado")));
                if (!themeExists(currentThemeTitle)) {
                        createThemeInCurrentCourse(currentThemeTitle);
                        navigateTo("/cursos/" + sharedCourse.id());
                        pageWait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("p.ltp-estado")));
                }

                login(ALUMNO_USER, ALUMNO_PASSWORD);
                openStudentCourseMap(sharedCourse.id());
                assertStudentSeesThemeInMap(currentThemeTitle);

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());
                assertTeacherSeesTheme(currentThemeTitle);

                deleteThemeByTitle(currentThemeTitle);
                assertTeacherDoesNotSeeTheme(currentThemeTitle);

                login(ALUMNO_USER, ALUMNO_PASSWORD);
                openStudentCourseMap(sharedCourse.id());
                assertStudentDoesNotSeeThemeInMap(currentThemeTitle);
        }

        @Test
        @Order(9)
        @DisplayName("El alumno no puede acceder directamente al formulario de crear tema")
        void alumnoNoPuedeAccederDirectamenteARutaCrearTema() {
                ensureSharedCourseReady();
                login(ALUMNO_USER, ALUMNO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id() + "/temas/crear");

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement landingTitle = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.landing-title"))
                );

                assertThat(landingTitle.getText().replaceAll("\\s+", "")).isEqualToIgnoringCase("cerebrus");
                assertThat(driver.getCurrentUrl()).doesNotContain("/temas/crear");
        }

        @Test
        @Order(10)
        @DisplayName("Un usuario externo no puede ver el tema")
        void usuarioExternoNoPuedeVerTema() {
                ensureSharedCourseReady();

                clearClientSession();
                navigateTo("/mapa/" + sharedCourse.id());

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                pageWait.until(ExpectedConditions.urlContains("/auth/login"));
                WebElement userInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("identificador"))
                );
                assertThat(userInput.isDisplayed()).isTrue();
        }

        @Test
        @Order(11)
        @DisplayName("Otro alumno no puede acceder al tema")
        void otroAlumnoNoPuedeAccederATema() {
                ensureSharedCourseReady();
                login("alumno_hermione", ALUMNO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                pageWait.until(ExpectedConditions.urlContains("/misCursos"));

                assertThat(driver.getCurrentUrl()).contains("/misCursos");
                assertThat(driver.findElements(By.cssSelector("button.ltp-btn-añadir"))).isEmpty();
        }

        @Test
        @Order(12)
        @DisplayName("El maestro elimina el curso creado al terminar las pruebas")
        void maestroEliminaElCursoCreadoAlFinal() {
                ensureSharedCourseReady();
                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/miscursos");

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement courseCard = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//div[contains(@class, 'curso-card')][.//span[contains(@class, 'curso-card__titulo') and normalize-space() = '" + COURSE_TITLE + "']]")
                                )
                );

                courseCard.findElement(By.cssSelector("button.curso-card__delete-btn")).click();
                pageWait.until(ExpectedConditions.alertIsPresent());
                driver.switchTo().alert().accept();

                // Esperar a que el backend procese la eliminación
                try {
                        Thread.sleep(1000);
                } catch (InterruptedException ignored) {}

                navigateTo("/miscursos");
                pageWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.mis-cursos-title")));

                By createdCourseCard = By.xpath("//div[contains(@class, 'curso-card')][.//span[contains(@class, 'curso-card__titulo') and normalize-space() = '" + COURSE_TITLE + "']]");
                
                // Esperar con reintentos a que el elemento desaparezca
                WebDriverWait longerWait = new WebDriverWait(driver, Duration.ofSeconds(30));
                longerWait.until(d -> d.findElements(createdCourseCard).isEmpty());

                // Assertions finales para verificar eliminación
                assertThat(driver.findElements(createdCourseCard)).isEmpty();
                assertThat(driver.findElement(By.cssSelector("h1.mis-cursos-title")).getText()).isEqualToIgnoringCase("MIS CURSOS");

                // Resetear el estado compartido para que el siguiente test pueda recrear el curso
                sharedCourse = null;
                currentThemeTitle = COURSE_THEME_TITLE;
        }

        private CourseContext createCourseAndOpenDetail(String courseTitle) {
                navigateTo("/crearCurso");

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement titleInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))
                );
                titleInput.clear();
                titleInput.sendKeys(courseTitle);

                WebElement descriptionInput = driver.findElement(By.id("descripcion"));
                descriptionInput.clear();
                descriptionInput.sendKeys(COURSE_DESCRIPTION);

                WebElement codeInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("codigo"))
                );
                codeInput.clear();
                codeInput.sendKeys(COURSE_CUSTOM_CODE);

                driver.findElement(By.cssSelector("button.pixel-btn-submit-main")).click();

                try {
                        pageWait.until(ExpectedConditions.alertIsPresent());
                        driver.switchTo().alert().accept();
                } catch (org.openqa.selenium.TimeoutException ignored) {
                        // Si el navegador no muestra alerta, seguimos con la navegación.
                }

                try {
                        pageWait.until(ExpectedConditions.urlContains("/miscursos"));
                } catch (org.openqa.selenium.TimeoutException timeoutException) {
                        List<WebElement> errors = driver.findElements(By.cssSelector("p.error-msg"));
                        if (!errors.isEmpty()) {
                                throw new AssertionError("No se pudo crear el curso: " + errors.get(0).getText(), timeoutException);
                        }
                        throw timeoutException;
                }
                WebElement courseCard = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//div[contains(@class, 'curso-card')][.//span[contains(@class, 'curso-card__titulo') and normalize-space() = '" + courseTitle + "']]")
                                )
                );

                String courseCode = courseCard.findElement(By.cssSelector("span.curso-card__codigo")).getText().trim();
                ensureCourseIsVisible(courseCard);
                courseCard.click();

                pageWait.until(ExpectedConditions.urlContains("/cursos/"));
                String currentUrl = driver.getCurrentUrl();
                Matcher matcher = Pattern.compile("/cursos/(\\d+)").matcher(currentUrl);
                if (matcher.find()) {
                        return new CourseContext(courseTitle, courseCode, matcher.group(1));
                }

                throw new IllegalStateException("No se pudo obtener el id del curso desde la URL: " + currentUrl);
        }

        private void enrollStudentInCourse(String courseCode) {
                navigateTo("/miscursos");

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                pageWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.mis-cursos-title")));

                WebElement codeInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("codigoCursoInput"))
                );
                codeInput.clear();
                codeInput.sendKeys(courseCode);

                WebElement joinButton = pageWait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button.join-btn"))
                );
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", joinButton);
                try {
                        joinButton.click();
                } catch (org.openqa.selenium.ElementClickInterceptedException intercepted) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", joinButton);
                }

                pageWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("p.mis-cursos-feedback--success")));
        }

        private void ensureCourseIsVisible(WebElement courseCard) {
                WebElement visibilityLabel = courseCard.findElement(
                        By.cssSelector("label[aria-label='Visibilidad del curso']")
                );
                WebElement visibilityToggle = courseCard.findElement(
                                By.cssSelector("label[aria-label='Visibilidad del curso'] input[type='checkbox']")
                );

                if (!visibilityToggle.isSelected()) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", visibilityLabel);
                        new WebDriverWait(driver, WAIT).until(ExpectedConditions.elementToBeSelected(visibilityToggle));
                }
        }

        private void createThemeInCurrentCourse(String themeTitle) {
                WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(20));

                try {
                        navigateTo("/cursos/" + sharedCourse.id() + "/temas/crear");
                } catch (org.openqa.selenium.WebDriverException e) {
                        try { Thread.sleep(2000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                        navigateTo("/cursos/" + sharedCourse.id() + "/temas/crear");
                }

                try {
                        pageWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.welcome-text")));
                } catch (org.openqa.selenium.TimeoutException e) {
                        throw new AssertionError("No cargó el formulario de crear tema. URL: " + driver.getCurrentUrl(), e);
                }

                WebElement titleInput = pageWait.until(
                        ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))
                );
                titleInput.clear();
                titleInput.sendKeys(themeTitle);

                WebElement submitButton = pageWait.until(
                        ExpectedConditions.elementToBeClickable(By.cssSelector("button.pixel-btn-submit-main"))
                );
                submitButton.click();

                // Esperar a que el formulario desaparezca (indica que se envió)
                pageWait.until(ExpectedConditions.stalenessOf(submitButton));
    
                // Esperar redirección a página de curso
                pageWait.until(ExpectedConditions.urlContains("/cursos/" + sharedCourse.id()));
    
                // Esperar a que el tema creado sea visible en la lista
                pageWait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//span[contains(@class, 'ltp-tema-titulo') and contains(normalize-space(), '" + themeTitle + "')]")
                ));
}

        private void assertTeacherSeesTheme(String themeTitle) {
                System.out.println("[TemasIT] assertTeacherSeesTheme themeTitle=" + themeTitle + " url=" + driver.getCurrentUrl());
                WebElement createdTheme = waitForThemeInList(themeTitle, Duration.ofSeconds(60));
                System.out.println("[TemasIT] assertTeacherSeesTheme foundText=" + createdTheme.getText());
                assertThat(createdTheme.getText()).contains(themeTitle);
        }

        private void assertTeacherDoesNotSeeTheme(String themeTitle) {
                assertThat(driver.findElements(
                                By.xpath("//span[contains(@class, 'ltp-tema-titulo') and contains(normalize-space(), '" + themeTitle + "')]")
                )).isEmpty();
        }

        private boolean themeExists(String themeTitle) {
                return !driver.findElements(By.xpath(
                                "//div[contains(@class, 'ltp-tema-bloque')][.//span[contains(@class, 'ltp-tema-titulo') and contains(normalize-space(), '" + themeTitle + "')]]"
                )).isEmpty();
        }

        private WebElement waitForThemeInList(String themeTitle, Duration timeout) {
                By themeLocator = By.xpath(
                                "//div[contains(@class, 'ltp-tema-bloque')][.//span[contains(@class, 'ltp-tema-titulo') and contains(normalize-space(), '" + themeTitle + "')]]"
                );

                long end = System.currentTimeMillis() + timeout.toMillis();
                boolean refreshed = false;
                int attempt = 0;

                while (System.currentTimeMillis() < end) {
                        attempt++;
                        List<WebElement> elems = driver.findElements(themeLocator);
                        System.out.println("[TemasIT] waitForThemeInList attempt=" + attempt + " matches=" + elems.size() + " url=" + driver.getCurrentUrl());
                        if (!elems.isEmpty()) {
                                for (WebElement e : elems) {
                                        if (e.isDisplayed()) {
                                                System.out.println("[TemasIT] waitForThemeInList visibleText=" + e.getText());
                                                return e;
                                        }
                                }
                        }

                        List<WebElement> loaders = driver.findElements(By.cssSelector("p.ltp-estado"));
                        System.out.println("[TemasIT] waitForThemeInList attempt=" + attempt + " loaders=" + loaders.size());
                        if (!loaders.isEmpty()) {
                                try {
                                        new WebDriverWait(driver, Duration.ofSeconds(5)).until(
                                                        ExpectedConditions.invisibilityOfElementLocated(By.cssSelector("p.ltp-estado"))
                                        );
                                } catch (org.openqa.selenium.TimeoutException ignored) {
                                }
                        }

                        try {
                                Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                                break;
                        }

                        // Intenta un refresh una vez a mitad del timeout para forzar re-render
                        if (!refreshed && System.currentTimeMillis() > (end - timeout.toMillis() / 2)) {
                                try {
                                        driver.navigate().refresh();
                                } catch (Exception ignored) {
                                }
                                refreshed = true;
                        }
                }

                String snapshotPath = "target/test-output/theme-snapshot-" + System.currentTimeMillis() + ".html";
                try {
                        java.nio.file.Files.createDirectories(java.nio.file.Paths.get("target/test-output"));
                        java.nio.file.Files.write(java.nio.file.Paths.get(snapshotPath), driver.getPageSource().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                } catch (java.io.IOException ignored) {
                }

                System.out.println("[TemasIT] waitForThemeInList timeout snapshot=" + snapshotPath);
                System.out.println("[TemasIT] waitForThemeInList pageSourceLength=" + driver.getPageSource().length());

                throw new AssertionError("Tema '" + themeTitle + "' no apareció en la lista tras " + timeout.toSeconds() + "s. Captura: " + snapshotPath);
        }

        private void openStudentCourseMap(String courseId) {
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
                try {
                        navigateTo("/cursos/" + courseId);
                } catch (org.openqa.selenium.TimeoutException ignored) {
                        // La página SPA puede no disparar load — continuar si el DOM está listo
                } finally {
                        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(300));
                }

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                List<WebElement> heroButtons = driver.findElements(By.cssSelector("button.detalle-hero-btn"));
                if (!heroButtons.isEmpty()) {
                        WebElement continueButton = pageWait.until(
                                ExpectedConditions.visibilityOf(heroButtons.get(0)));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", continueButton);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueButton);
                } else {
                        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(15));
                        try {
                        navigateTo("/mapa/" + courseId);
                        } catch (org.openqa.selenium.TimeoutException ignored) {}
                        finally {
                        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(300));
                        }
                }

                pageWait.until(ExpectedConditions.urlContains("/mapa/" + courseId));
                }

        private void assertStudentDoesNotSeeThemeInMap(String themeTitle) {
                assertThat(driver.findElements(
                                By.xpath("//button[contains(@class, 'mapa-tema-btn') and normalize-space() = '" + themeTitle + "']")
                )).isEmpty();
        }

        private void deleteThemeByTitle(String themeTitle) {
                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                By themeLocator = By.xpath(
                                "//div[contains(@class, 'ltp-tema-bloque')][.//span[contains(@class, 'ltp-tema-titulo') and contains(normalize-space(), '" + themeTitle + "')]]"
                );

                int safetyCounter = 0;
                while (!driver.findElements(themeLocator).isEmpty() && safetyCounter < 10) {
                        safetyCounter++;
                        int previousCount = driver.findElements(themeLocator).size();

                        WebElement themeItem = pageWait.until(
                                        ExpectedConditions.visibilityOfElementLocated(themeLocator)
                        );

                        WebElement deleteButton = themeItem.findElement(By.cssSelector("button[title='Borrar']"));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteButton);

                        try {
                                new WebDriverWait(driver, Duration.ofSeconds(2)).until(ExpectedConditions.alertIsPresent());
                                driver.switchTo().alert().accept();
                        } catch (org.openqa.selenium.TimeoutException ignored) {
                                // No todos los flujos muestran confirmación al borrar tema.
                        }

                        // Esperamos a que disminuya al menos una ocurrencia antes de continuar.
                        try {
                                new WebDriverWait(driver, Duration.ofSeconds(6)).until(d -> d.findElements(themeLocator).size() < previousCount);
                        } catch (org.openqa.selenium.TimeoutException ignored) {
                                navigateTo("/cursos/" + sharedCourse.id());
                        }
                }

                assertThat(driver.findElements(themeLocator)).isEmpty();
        }

        private void clearClientSession() {
                try {
                        driver.manage().deleteAllCookies();
                } catch (org.openqa.selenium.WebDriverException ignored) {
                        // Si el driver no permite gestionar cookies en este estado, continuamos con storage.
                }

                if (driver instanceof JavascriptExecutor javascriptExecutor) {
                        try {
                                javascriptExecutor.executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
                        } catch (org.openqa.selenium.WebDriverException ignored) {
                                // Algunas páginas temporales como data: no exponen storage.
                        }
                }
        }

        private void ensureSharedCourseReady() {
                ensureSharedCourseReadyInternal(0);
        }

        private void ensureSharedCourseReadyInternal(int retries) {
                if (retries > 1) {
                        throw new IllegalStateException("No se pudo asegurar que el curso compartido esté listo después de 2 intentos");
                }

                if (sharedCourse == null) {
                        login(MAESTRO_USER, MAESTRO_PASSWORD);
                        sharedCourse = createCourseAndOpenDetail(COURSE_TITLE);
                        login(ALUMNO_USER, ALUMNO_PASSWORD);
                        enrollStudentInCourse(sharedCourse.code());
                }

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());

                // Validar que el curso existe; si no, recrearlo
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                try {
                        shortWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.ltp-btn-añadir")));
                } catch (org.openqa.selenium.TimeoutException e) {
                        // El curso no existe, resetear y recrear (solo 1 reintento)
                        System.out.println("Curso " + sharedCourse.id() + " no existe, recreando... (intento " + (retries + 1) + ")");
                        sharedCourse = null;
                        currentThemeTitle = COURSE_THEME_TITLE;
                        ensureSharedCourseReadyInternal(retries + 1);
                        return;
                }

                        By currentThemeLocator = By.xpath(
                                "//span[contains(@class, 'ltp-tema-titulo') and contains(normalize-space(), '" 
                                + currentThemeTitle + "')]"
                        );

                List<WebElement> currentThemeMatches = driver.findElements(currentThemeLocator);

                if (currentThemeMatches.isEmpty()) {
                                List<WebElement> anyThemeTitles = driver.findElements(
                                        By.cssSelector("span.ltp-tema-titulo"));
                        anyThemeTitles.forEach(t -> System.out.println("  - " + t.getText()));

                                if (!anyThemeTitles.isEmpty()) {
                                        currentThemeTitle = anyThemeTitles.get(0).getText().trim();
                                } else {
                        }
                }
        }

        private void setCourseVisibilityInUi(boolean visible) {
                navigateTo("/miscursos");

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement courseCard = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//div[contains(@class, 'curso-card')][.//span[contains(@class, 'curso-card__titulo') and normalize-space() = '" + COURSE_TITLE + "']]")
                                )
                );

                WebElement visibilityLabel = courseCard.findElement(
                        By.cssSelector("label[aria-label='Visibilidad del curso']")
                );
                WebElement visibilityToggle = courseCard.findElement(
                                By.cssSelector("label[aria-label='Visibilidad del curso'] input[type='checkbox']")
                );

                if (visibilityToggle.isSelected() != visible) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", visibilityLabel);
                        pageWait.until(ExpectedConditions.elementSelectionStateToBe(visibilityToggle, visible));
                }
        }
        private void assertStudentSeesThemeInMap(String themeTitle) {
                WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(20));

                driver.navigate().refresh();
                
                WebElement themeButton = pageWait.until(
                        ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//button[contains(@class, 'mapa-tema-btn') and normalize-space() = '" + themeTitle + "']")
                        )
                );
                assertThat(themeButton.getText()).isEqualTo(themeTitle);
        }

}