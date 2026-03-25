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
        <Route path="/infoDueños" element={<div>Info Dueños Mock</div>} />
      </Routes>
    </MemoryRouter>
  );
}

describe("LoginPage", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it("muestra campos de usuario y contraseña", () => {
    renderLoginWithRoutes();

    expect(screen.getByText("Iniciar Sesión")).toBeInTheDocument();
    expect(screen.getByLabelText("Usuario:")).toBeInTheDocument();
    expect(screen.getByLabelText("Contraseña:")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Iniciar sesión" })
    ).toBeInTheDocument();
  });

  it("muestra error si la API responde 401", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValueOnce({
        ok: false,
        status: 401,
        json: async () => ({ message: "Credenciales incorrectas" }),
      } as Response);

    renderLoginWithRoutes();

    const user = userEvent.setup();
    await user.type(screen.getByLabelText("Usuario:"), "alumno1");
    await user.type(screen.getByLabelText("Contraseña:"), "bad-password");
    await user.click(screen.getByRole("button", { name: "Iniciar sesión" }));

    expect(await screen.findByText("Credenciales incorrectas")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it("guarda token y navega a /miscursos en login correcto", async () => {
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
    await user.type(screen.getByLabelText("Usuario:"), "alumno1");
    await user.type(screen.getByLabelText("Contraseña:"), "1234");
    await user.click(screen.getByRole("button", { name: "Iniciar sesión" }));

    expect(await screen.findByText("Mis Cursos Mock")).toBeInTheDocument();

    await waitFor(() => {
      expect(localStorage.getItem("token")).toBe("fake-jwt");
      expect(localStorage.getItem("username")).toBe("alumno1");
      expect(localStorage.getItem("role")).toBe("ROLE_ALUMNO");
    });
  });
});
