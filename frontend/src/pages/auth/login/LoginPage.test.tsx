import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import LoginPage from "./LoginPage";

function renderLoginWithRoutes() {
  return render(
    <MemoryRouter initialEntries={["/auth/login"]}>
      <Routes>
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/miscursos" element={<div>Mis Cursos Mock</div>} />
        <Route path="/suscripcion" element={<div>Suscripción Mock</div>} />
        <Route path="/auth/verify-email" element={<div>Verificación Mock</div>} />
        <Route path="/auth/register" element={<div>Registro Mock</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe("LoginPage", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    localStorage.clear();
  });

  it("muestra los controles principales y accesos secundarios", () => {
    renderLoginWithRoutes();

    expect(screen.getByText("Iniciar Sesión")).toBeInTheDocument();
    expect(screen.getByLabelText(/correo electrónico o usuario:/i)).toBeInTheDocument();
    expect(screen.getByLabelText("Contraseña:")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /entrar/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /tengo un código de verificación/i })).toBeInTheDocument();
    expect(screen.getByText(/¿no tienes cuenta\?/i)).toBeInTheDocument();
  });

  it("muestra el mensaje devuelto por la API cuando falla el login", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValueOnce({
      ok: false,
      status: 401,
      json: async () => ({ message: "Credenciales incorrectas" }),
    } as Response);

    renderLoginWithRoutes();

    const user = userEvent.setup();
    await user.type(screen.getByLabelText(/correo electrónico o usuario:/i), "alumno1");
    await user.type(screen.getByLabelText("Contraseña:"), "bad-password");
    await user.click(screen.getByRole("button", { name: /entrar/i }));

    expect(await screen.findByText("Credenciales incorrectas")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it("guarda token y navega a mis cursos en login correcto de alumno", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValueOnce({
      ok: true,
      status: 200,
      json: async () => ({
        token: "fake-jwt",
        username: "alumno1",
        roles: ["ROLE_ALUMNO"],
      }),
    } as Response);

    renderLoginWithRoutes();

    const user = userEvent.setup();
    await user.type(screen.getByLabelText(/correo electrónico o usuario:/i), "alumno1");
    await user.type(screen.getByLabelText("Contraseña:"), "1234");
    await user.click(screen.getByRole("button", { name: /entrar/i }));

    expect(await screen.findByText("Mis Cursos Mock")).toBeInTheDocument();

    await waitFor(() => {
      expect(localStorage.getItem("token")).toBe("fake-jwt");
      expect(localStorage.getItem("username")).toBe("alumno1");
      expect(localStorage.getItem("role")).toBe("ROLE_ALUMNO");
    });
  });

  it("navega a verificación cuando se pulsa el acceso de código", async () => {
    renderLoginWithRoutes();

    const user = userEvent.setup();
    await user.click(screen.getByRole("button", { name: /tengo un código de verificación/i }));

    expect(await screen.findByText("Verificación Mock")).toBeInTheDocument();
  });
});
