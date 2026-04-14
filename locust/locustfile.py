import os

from locust import HttpUser, between, task


LOGIN_PATH = "/auth/login"

DEFAULT_ALUMNO_USERNAME = "alumno_harry"
DEFAULT_ALUMNO_PASSWORD = "123456"
DEFAULT_MAESTRO_USERNAME = "carlos_pro"
DEFAULT_MAESTRO_PASSWORD = "123456"

CURSO_FRONTEND_ID = 4001
CURSO_BACKEND_ID = 4002
TEMA_FRONTEND_ID = 5001
TEMA_REACT_ID = 5002
TEMA_BACKEND_ID = 5003
ACTIVIDAD_TEST_HTML_ID = 6001
ACTIVIDAD_ORDENACION_ID = 6002
ACTIVIDAD_TEORIA_ID = 6004
ACTIVIDAD_TEST_JAVA_ID = 6005


class CerebrusAuthenticatedUser(HttpUser):
    wait_time = between(1, 3)
    abstract = True

    username_env_var = ""
    password_env_var = ""
    default_username = ""
    default_password = ""

    def on_start(self) -> None:
        self.username = os.getenv(self.username_env_var, self.default_username)
        self.password = os.getenv(self.password_env_var, self.default_password)
        self.token = None
        self.auth_headers = {}
        self._login()

    def _login(self) -> None:
        payload = {
            "identificador": self.username,
            "password": self.password,
        }

        with self.client.post(
            LOGIN_PATH,
            json=payload,
            name="POST /auth/login",
            catch_response=True,
        ) as response:
            if response.status_code != 200:
                response.failure(
                    f"Login failed with status {response.status_code}: {response.text}"
                )
                return

            data = response.json()
            token = data.get("token")
            token_type = data.get("type", "Bearer")

            if not token:
                response.failure("Login response did not include a JWT token.")
                return

            self.token = token
            self.auth_headers = {"Authorization": f"{token_type} {token}"}
            response.success()

    def _ensure_authenticated(self) -> bool:
        if self.token:
            return True

        self._login()
        return self.token is not None

    def _get(self, path: str, name: str) -> None:
        if not self._ensure_authenticated():
            return

        self.client.get(path, headers=self.auth_headers, name=name)


class CerebrusAlumnoUser(CerebrusAuthenticatedUser):
    weight = 3
    username_env_var = "CEREBRUS_USERNAME"
    password_env_var = "CEREBRUS_PASSWORD"
    default_username = DEFAULT_ALUMNO_USERNAME
    default_password = DEFAULT_ALUMNO_PASSWORD

    @task(4)
    def get_current_user(self) -> None:
        self._get("/api/usuarios/me", "GET /api/usuarios/me [alumno]")

    @task(3)
    def get_my_courses(self) -> None:
        self._get("/api/cursos", "GET /api/cursos [alumno]")

    @task(3)
    def get_frontend_course_topics(self) -> None:
        self._get(
            f"/api/temas/curso/{CURSO_FRONTEND_ID}/alumno",
            "GET /api/temas/curso/:cursoId/alumno",
        )

    @task(2)
    def get_backend_course_topics(self) -> None:
        self._get(
            f"/api/temas/curso/{CURSO_BACKEND_ID}/alumno",
            "GET /api/temas/curso/:cursoId/alumno",
        )

    @task(2)
    def get_single_topic(self) -> None:
        self._get(f"/api/temas/{TEMA_FRONTEND_ID}", "GET /api/temas/:temaId")

    @task(2)
    def get_html_test_activity(self) -> None:
        self._get(
            f"/api/generales/test/{ACTIVIDAD_TEST_HTML_ID}",
            "GET /api/generales/test/:id [alumno]",
        )

    @task(2)
    def get_theory_activity(self) -> None:
        self._get(
            f"/api/actividades/{ACTIVIDAD_TEORIA_ID}/alumno",
            "GET /api/actividades/:id/alumno",
        )

    @task(2)
    def get_ordering_activity(self) -> None:
        self._get(
            f"/api/ordenaciones/{ACTIVIDAD_ORDENACION_ID}",
            "GET /api/ordenaciones/:id [alumno]",
        )

    @task(1)
    def ensure_student_activity_exists(self) -> None:
        self._get(
            f"/api/actividades-alumno/ensure/{ACTIVIDAD_TEST_HTML_ID}",
            "GET /api/actividades-alumno/ensure/:actividadId",
        )


class CerebrusMaestroUser(CerebrusAuthenticatedUser):
    weight = 1
    username_env_var = "CEREBRUS_MAESTRO_USERNAME"
    password_env_var = "CEREBRUS_MAESTRO_PASSWORD"
    default_username = DEFAULT_MAESTRO_USERNAME
    default_password = DEFAULT_MAESTRO_PASSWORD

    @task(3)
    def get_current_user(self) -> None:
        self._get("/api/usuarios/me", "GET /api/usuarios/me [maestro]")

    @task(3)
    def get_my_courses(self) -> None:
        self._get("/api/cursos", "GET /api/cursos [maestro]")

    @task(2)
    def get_teacher_topics(self) -> None:
        self._get(
            f"/api/temas/curso/{CURSO_FRONTEND_ID}/maestro",
            "GET /api/temas/curso/:cursoId/maestro",
        )

    @task(2)
    def get_teacher_single_topic(self) -> None:
        self._get(f"/api/temas/{TEMA_REACT_ID}", "GET /api/temas/:temaId")

    @task(2)
    def get_teacher_theory_activity(self) -> None:
        self._get(
            f"/api/actividades/{ACTIVIDAD_TEORIA_ID}/maestro",
            "GET /api/actividades/:id/maestro",
        )

    @task(2)
    def get_teacher_test_activity(self) -> None:
        self._get(
            f"/api/generales/test/{ACTIVIDAD_TEST_HTML_ID}/maestro",
            "GET /api/generales/test/:id/maestro",
        )

    @task(2)
    def get_teacher_ordering_activity(self) -> None:
        self._get(
            f"/api/ordenaciones/{ACTIVIDAD_ORDENACION_ID}/maestro",
            "GET /api/ordenaciones/:id/maestro",
        )

    @task(1)
    def get_teacher_backend_test(self) -> None:
        self._get(
            f"/api/generales/test/{ACTIVIDAD_TEST_JAVA_ID}/maestro",
            "GET /api/generales/test/:id/maestro",
        )

    @task(1)
    def get_course_details(self) -> None:
        self._get(
            f"/api/cursos/{CURSO_FRONTEND_ID}/detalles",
            "GET /api/cursos/:id/detalles",
        )
