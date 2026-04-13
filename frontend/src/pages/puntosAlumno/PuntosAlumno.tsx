import { useEffect, useState } from "react";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import { apiFetch } from "../../utils/api";
import niv1 from "../../assets/props/perritoCerberito.png";
import niv2 from "../../assets/props/Guardian_de_la_primera_cabeza_niv2.png";
import niv3 from "../../assets/props/Cerbero_despierto_niv3.png";
import niv4 from "../../assets/props/Vigia_de_las_tres_mentes_niv4.png";
import niv5 from "../../assets/props/Rey_de_cerebrus_niv5.png";
import "./PuntosAlumno.css";

/* ── Sistema de arenas ── */
interface Arena {
  nombre: string;
  emoji: string;
  min: number;
  max: number;
  imagen: string;
}

const ARENAS: Arena[] = [
  { nombre: "Cachorro del Umbral",            emoji: "🐾", min: 0,    max: 100,      imagen: niv1 },
  { nombre: "Guardián de la Primera Cabeza",  emoji: "🔥", min: 101,  max: 300,      imagen: niv2 },
  { nombre: "Cerbero Despierto",              emoji: "⚔️", min: 301,  max: 600,      imagen: niv3 },
  { nombre: "Vigía de las Tres Mentes",       emoji: "👁️", min: 601,  max: 1000,     imagen: niv4 },
  { nombre: "Señor de Cerebrus",              emoji: "👑", min: 1001, max: Infinity,  imagen: niv5 },
];

function getArenaIndex(puntos: number): number {
  const idx = ARENAS.findIndex((a) => puntos >= a.min && puntos <= a.max);
  return idx >= 0 ? idx : 0;
}

/** Calcula la posición (%) del alumno en la línea de evolución */
function getPosicionLinea(puntos: number): number {
  const idx = getArenaIndex(puntos);
  const arena = ARENAS[idx];
  const segmento = 100 / (ARENAS.length - 1);         // % por cada tramo
  const base = idx * segmento;
  if (arena.max === Infinity) return base;             // última arena
  const avance = (puntos - arena.min) / (arena.max - arena.min + 1);
  return base + avance * segmento;
}

const apiBase = (import.meta.env.VITE_API_URL ?? "").trim().replace(/\/$/, "");

export default function PuntosAlumno() {
  const [puntos, setPuntos] = useState<number>(0);
  const [nombreUsuario, setNombreUsuario] = useState<string>("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const [resPuntos, resUser] = await Promise.all([
          apiFetch(`${apiBase}/api/alumnos/mi-puntuacion-total`),
          apiFetch(`${apiBase}/api/usuarios/me`),
        ]);
        if (resPuntos.ok) {
          const data = await resPuntos.json();
          setPuntos(data ?? 0);
        }
        if (resUser.ok) {
          const user = await resUser.json();
          setNombreUsuario(user.nombre ?? user.nombreUsuario ?? "");
        }
      } catch {
        setError("Error de conexión al cargar tus puntos.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const arenaIdx = getArenaIndex(puntos);
  const arena = ARENAS[arenaIdx];
  const siguiente = arenaIdx < ARENAS.length - 1 ? ARENAS[arenaIdx + 1] : null;
  const puntosParaSiguiente = siguiente ? siguiente.min - puntos : 0;
  const posicion = getPosicionLinea(puntos);

  if (loading) {
    return (
      <>
        <NavbarMisCursos />
        <div className="puntos-page">
          <p className="puntos-loading">Cargando tu progreso…</p>
        </div>
      </>
    );
  }

  if (error) {
    return (
      <>
        <NavbarMisCursos />
        <div className="puntos-page">
          <p className="puntos-error">{error}</p>
        </div>
      </>
    );
  }

  return (
    <>
      <NavbarMisCursos />
      <div className="puntos-page">
        <h1 className="puntos-title">Tu Progreso</h1>

        {/* ── Línea de evolución horizontal ── */}
        <div className="puntos-timeline">
          {/* Nodos de cada arena */}
          <div className="puntos-timeline-nodes">
            {ARENAS.map((a, i) => {
              const alcanzada = puntos >= a.min;
              const esActual = i === arenaIdx;
              return (
                <div
                  key={i}
                  className={`puntos-node${esActual ? " puntos-node--actual" : ""}${alcanzada ? " puntos-node--alcanzada" : ""}`}
                >
                  <img
                    src={a.imagen}
                    alt={a.nombre}
                    className="puntos-node-img"
                  />
                  <span className="puntos-node-name">{a.nombre}</span>
                </div>
              );
            })}
          </div>

          {/* Línea de progreso con puntos */}
          <div className="puntos-track">
            <div className="puntos-track-line" />
            <div
              className="puntos-track-fill"
              style={{ width: `${posicion}%` }}
            />
            {ARENAS.map((a, i) => {
              const alcanzada = puntos >= a.min;
              const pct = (i / (ARENAS.length - 1)) * 100;
              return (
                <div
                  key={i}
                  className={`puntos-track-dot${alcanzada ? " puntos-track-dot--filled" : ""}`}
                  style={{ left: `${pct}%` }}
                />
              );
            })}
            {/* Marcador del alumno */}
            <div className="puntos-marker" style={{ left: `${posicion}%` }}>
              <img src={arena.imagen} alt="" className="puntos-marker-img" />
            </div>
          </div>

          {/* Etiquetas de puntos debajo */}
          <div className="puntos-timeline-labels">
            {ARENAS.map((a, i) => (
              <span key={i} className="puntos-label-pts">
                {a.max === Infinity ? `${a.min}+` : a.min} pts
              </span>
            ))}
          </div>
        </div>

        {/* ── Resumen del alumno ── */}
        <div className="puntos-alumno-badge">
          <img src={arena.imagen} alt={arena.nombre} className="puntos-alumno-badge-img" />
          <div className="puntos-alumno-badge-info">
            <strong className="puntos-alumno-badge-name">{nombreUsuario}</strong>
            <span className="puntos-alumno-badge-pts">{puntos} puntos</span>
            {siguiente ? (
              <span className="puntos-alumno-badge-next">
                Faltan <strong>{puntosParaSiguiente}</strong> pts → {siguiente.emoji} {siguiente.nombre}
              </span>
            ) : (
              <span className="puntos-alumno-badge-max">¡Arena máxima! 👑</span>
            )}
          </div>
        </div>
      </div>
    </>
  );
}
