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

        private static final Duration WAIT = Duration.ofSeconds(10);
        private static final String MAESTRO_USER = "profe_snape";
        private static final String MAESTRO_PASSWORD = "123456";
        private static final String ALUMNO_USER = "alumno_harry";
        private static final String ALUMNO_PASSWORD = "123456";
        private static final String COURSE_TITLE = "Curso de Temas Selenium";
        private static final String COURSE_DESCRIPTION = "Curso de prueba para Selenium";
        private static final String COURSE_THEME_TITLE = "Introducción a las fracciones";

        private record CourseContext(String title, String code, String id) {}

        private static CourseContext sharedCourse;

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
        void maestroLoginShowsMisCursosYAbreElCurso() {
                ensureSharedCourseReady();
                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/miscursos");

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement title = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.mis-cursos-title"))
                );

                assertThat(title.getText()).isEqualToIgnoringCase("MIS CURSOS");
                assertThat(driver.findElements(By.cssSelector("div.curso-card"))).isNotEmpty();

                navigateTo("/cursos/" + sharedCourse.id());
                pageWait.until(ExpectedConditions.urlContains("/cursos/" + sharedCourse.id()));

                WebElement addThemeButton = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.ltp-btn-añadir"))
                );
                assertThat(addThemeButton.getText()).contains("Añadir tema");

                List<WebElement> themeItems = driver.findElements(By.cssSelector("div.ltp-item"));
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
        void maestroCanOpenCreateThemeFormAndCreateTheme() {
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
                assertThat(driver.findElements(By.cssSelector("span.ltp-item-titulo"))).isEmpty();

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                setCourseVisibilityInUi(true);
        }

        @Test
        @Order(5)
        @DisplayName("El alumno ve el tema y después deja de verlo al borrarlo")
        void alumnoCanSeeCreatedThemeAndThenStopSeeingIt() {
                ensureSharedCourseReady();

                login(ALUMNO_USER, ALUMNO_PASSWORD);
                openStudentCourseMap(sharedCourse.id());
                assertStudentSeesThemeInMap(COURSE_THEME_TITLE);

                login(MAESTRO_USER, MAESTRO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());
                assertTeacherSeesTheme(COURSE_THEME_TITLE);

                deleteThemeByTitle(COURSE_THEME_TITLE);
                assertTeacherDoesNotSeeTheme(COURSE_THEME_TITLE);

                login(ALUMNO_USER, ALUMNO_PASSWORD);
                openStudentCourseMap(sharedCourse.id());
                assertStudentDoesNotSeeThemeInMap(COURSE_THEME_TITLE);
        }

        @Test
        @Order(6)
        @DisplayName("El alumno no puede acceder directamente al formulario de crear tema")
        void alumnoCannotAccessCreateThemeRoute() {
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
        @Order(7)
        @DisplayName("Un usuario externo no puede ver el tema")
        void usuarioExternoNoPuedeVerElTema() {
                ensureSharedCourseReady();
                navigateTo("/mapa/" + sharedCourse.id());

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement loginTitle = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h2.login-title"))
                );

                assertThat(loginTitle.getText()).isEqualTo("Iniciar Sesión");
                assertThat(driver.getCurrentUrl()).contains("/auth/login");
        }

        @Test
        @Order(8)
        @DisplayName("Otro alumno no puede acceder al tema")
        void otroAlumnoNoPuedeAccederAlTema() {
                ensureSharedCourseReady();
                login("alumno_hermione", ALUMNO_PASSWORD);
                navigateTo("/cursos/" + sharedCourse.id());

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                pageWait.until(ExpectedConditions.urlContains("/misCursos"));

                assertThat(driver.getCurrentUrl()).contains("/misCursos");
                assertThat(driver.findElements(By.cssSelector("button.ltp-btn-añadir"))).isEmpty();
        }

        @Test
        @Order(9)
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

                navigateTo("/miscursos");
                pageWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.mis-cursos-title")));

                By createdCourseCard = By.xpath("//div[contains(@class, 'curso-card')][.//span[contains(@class, 'curso-card__titulo') and normalize-space() = '" + COURSE_TITLE + "']]");
                pageWait.until(d -> d.findElements(createdCourseCard).isEmpty());
        }

        private void login(String user, String password) {
                navigateTo("/auth/login");
                clearClientSession();

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement userInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("identificador"))
                );
                userInput.clear();
                userInput.sendKeys(user);

                WebElement passwordInput = driver.findElement(By.id("password"));
                passwordInput.clear();
                passwordInput.sendKeys(password);

                driver.findElement(By.cssSelector("button.pixel-btn-submit")).click();
                pageWait.until(ExpectedConditions.urlContains("/miscursos"));
                driver.navigate().refresh();
                pageWait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("h1.mis-cursos-title")));
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

                driver.findElement(By.cssSelector("button.pixel-btn-submit-main")).click();

                try {
                        pageWait.until(ExpectedConditions.alertIsPresent());
                        driver.switchTo().alert().accept();
                } catch (org.openqa.selenium.TimeoutException ignored) {
                        // Si el navegador no muestra alerta, seguimos con la navegación.
                }

                pageWait.until(ExpectedConditions.urlContains("/miscursos"));
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
                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement titleInput = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("titulo"))
                );
                titleInput.clear();
                titleInput.sendKeys(themeTitle);

                WebElement submitButton = pageWait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button.pixel-btn-submit-main"))
                );
                submitButton.click();

                pageWait.until(ExpectedConditions.urlContains("/cursos/" + sharedCourse.id()));

                navigateTo("/cursos/" + sharedCourse.id());
        }

        private void assertTeacherSeesTheme(String themeTitle) {
                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement createdTheme = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//span[contains(@class, 'ltp-item-titulo') and normalize-space() = '" + themeTitle + "']")
                                )
                );
                assertThat(createdTheme.getText()).isEqualTo(themeTitle);
        }

        private void assertTeacherDoesNotSeeTheme(String themeTitle) {
                assertThat(driver.findElements(
                                By.xpath("//span[contains(@class, 'ltp-item-titulo') and normalize-space() = '" + themeTitle + "']")
                )).isEmpty();
        }

        private void openStudentCourseMap(String courseId) {
                navigateTo("/cursos/" + courseId);

                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                List<WebElement> heroButtons = driver.findElements(By.cssSelector("button.detalle-hero-btn"));
                if (!heroButtons.isEmpty()) {
                        WebElement continueButton = pageWait.until(
                                        ExpectedConditions.visibilityOf(heroButtons.get(0))
                        );
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", continueButton);
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", continueButton);
                } else {
                        navigateTo("/mapa/" + courseId);
                }

                pageWait.until(ExpectedConditions.urlContains("/mapa/" + courseId));
        }

        private void assertStudentSeesThemeInMap(String themeTitle) {
                WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(20));
                WebElement themeButton = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//button[contains(@class, 'mapa-tema-btn') and normalize-space() = '" + themeTitle + "']")
                                )
                );
                assertThat(themeButton.getText()).isEqualTo(themeTitle);
        }

        private void assertStudentDoesNotSeeThemeInMap(String themeTitle) {
                assertThat(driver.findElements(
                                By.xpath("//button[contains(@class, 'mapa-tema-btn') and normalize-space() = '" + themeTitle + "']")
                )).isEmpty();
        }

        private void deleteThemeByTitle(String themeTitle) {
                WebDriverWait pageWait = new WebDriverWait(driver, WAIT);
                WebElement themeItem = pageWait.until(
                                ExpectedConditions.visibilityOfElementLocated(
                                                By.xpath("//div[contains(@class, 'ltp-item')][.//span[contains(@class, 'ltp-item-titulo') and normalize-space() = '" + themeTitle + "']]")
                                )
                );

                themeItem.findElement(By.cssSelector("button[title='Borrar']")).click();

                pageWait.until(ExpectedConditions.invisibilityOfElementLocated(
                                By.xpath("//div[contains(@class, 'ltp-item')][.//span[contains(@class, 'ltp-item-titulo') and normalize-space() = '" + themeTitle + "']]")
                ));
        }

        private void clearClientSession() {
                if (driver instanceof JavascriptExecutor javascriptExecutor) {
                        try {
                                javascriptExecutor.executeScript("window.localStorage.clear(); window.sessionStorage.clear();");
                        } catch (org.openqa.selenium.WebDriverException ignored) {
                                // Algunas páginas temporales como data: no exponen storage.
                        }
                }
        }

        private void ensureSharedCourseReady() {
                if (sharedCourse == null) {
                        throw new IllegalStateException("El curso compartido todavía no se ha creado.");
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

}