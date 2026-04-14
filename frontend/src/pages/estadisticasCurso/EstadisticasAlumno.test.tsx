// @vitest-environment node

import { describe, expect, it } from "vitest";
import { calcularNotaActualTema } from "./EstadisticasAlumno";

describe("calcularNotaActualTema", () => {
  it("promedia todas las actividades del tema incluyendo las pendientes como cero", () => {
    const nota = calcularNotaActualTema({
      actividades: [
        {
          puntuacionAlumno: 10,
          puntuacionMaxima: 10,
          notaAlumno: 10,
        },
        {
          puntuacionAlumno: null,
          puntuacionMaxima: 10,
          notaAlumno: null,
        },
      ],
    } as never);

    expect(nota).toBe(5);
  });

  it("devuelve null si el tema no tiene actividades", () => {
    expect(calcularNotaActualTema({ actividades: [] } as never)).toBeNull();
  });
});