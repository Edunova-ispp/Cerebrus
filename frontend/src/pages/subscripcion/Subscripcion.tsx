import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './Subscripcion.css';

export interface SuscripcionDTO {
  id: number;
  numMaestros: number;
  numAlumnos: number;
  precio: number;
  fechaInicio: string;
  fechaFin: string;
  estadoPago: string;
  isActiva: boolean;
}

export interface ResumenCompraDTO {
  precioTotal: number;
  numMaestros: number;
  numAlumnos: number;
  mensaje?: string;
}

// ── Validación ────────────────────────────────────────────
interface FormErrors {
  numMaestros?: string;
  numAlumnos?: string;
}

function validateForm(numMaestros: number, numAlumnos: number): FormErrors {
  const errors: FormErrors = {};

  if (!numMaestros || isNaN(numMaestros)) {
    errors.numMaestros = 'El número de profesores es obligatorio.';
  } else if (!Number.isInteger(numMaestros)) {
    errors.numMaestros = 'Debe ser un número entero.';
  } else if (numMaestros < 1) {
    errors.numMaestros = 'Debe haber al menos 1 profesor.';
  } else if (numMaestros > 999) {
    errors.numMaestros = 'El máximo permitido es 999 profesores.';
  }

  if (!numAlumnos || isNaN(numAlumnos)) {
    errors.numAlumnos = 'El número de alumnos es obligatorio.';
  } else if (!Number.isInteger(numAlumnos)) {
    errors.numAlumnos = 'Debe ser un número entero.';
  } else if (numAlumnos < 1) {
    errors.numAlumnos = 'Debe haber al menos 1 alumno.';
  } else if (numAlumnos > 9999) {
    errors.numAlumnos = 'El máximo permitido es 9999 alumnos.';
  }

  return errors;
}

// ── Componente principal ──────────────────────────────────
export default function Subscripcion() {
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
  const navigate = useNavigate();

  // ── Guard de rol ───────────────────────────────────────
  const userInfo = getCurrentUserInfo() as any;
  const rol: string | undefined = userInfo?.rol ?? userInfo?.role ?? userInfo?.authorities?.[0];
  const organizacionId: number | undefined = userInfo?.id ?? userInfo?.userId;

  useEffect(() => {
    // Si no hay sesión o el rol no es ORGANIZACION, redirige al inicio
    if (!userInfo || !rol) {
      navigate('/');
      return;
    }
    const rolNormalizado = rol.toUpperCase().replace('ROLE_', '');
    if (rolNormalizado !== 'ORGANIZACION' && rolNormalizado !== 'ORGANIZACION_ADMIN') {
      navigate('/');
    }
  }, []);

  // ── Estados principales ────────────────────────────────
  const [suscripcion, setSuscripcion] = useState<SuscripcionDTO | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  // ── Estados formulario ─────────────────────────────────
  const [numMaestros, setNumMaestros] = useState<number | ''>('');
  const [numAlumnos, setNumAlumnos] = useState<number | ''>('');
  const [formErrors, setFormErrors] = useState<FormErrors>({});
  const [touched, setTouched] = useState<{ numMaestros: boolean; numAlumnos: boolean }>({
    numMaestros: false,
    numAlumnos: false,
  });
  const [resumen, setResumen] = useState<ResumenCompraDTO | null>(null);
  const [calculando, setCalculando] = useState<boolean>(false);

  // ── Cargar suscripción activa ──────────────────────────
  useEffect(() => {
    if (!organizacionId) {
      setError('No se pudo identificar a la organización. Por favor, vuelve a iniciar sesión.');
      setLoading(false);
      return;
    }

    const fetchSuscripcion = async () => {
      try {
        setLoading(true);
        const res = await apiFetch(`${apiBase}/api/suscripciones/activa/${organizacionId}`);

        if (res.status === 200) {
          const data: SuscripcionDTO = await res.json();
          setSuscripcion(data);
        } else if (res.status === 204 || res.status === 404) {
          setSuscripcion(null);
        } else {
          throw new Error('Error al obtener la suscripción');
        }
      } catch (err) {
        console.error(err);
        setError('Ocurrió un error al cargar los datos de tu suscripción.');
      } finally {
        setLoading(false);
      }
    };

    fetchSuscripcion();
  }, [organizacionId, apiBase]);

  // ── Validación en tiempo real (solo en campos tocados) ──
  useEffect(() => {
    if (touched.numMaestros || touched.numAlumnos) {
      const errors = validateForm(
        numMaestros === '' ? NaN : numMaestros,
        numAlumnos === '' ? NaN : numAlumnos
      );
      setFormErrors(errors);
    }
  }, [numMaestros, numAlumnos, touched]);

  // ── Calcular precio ────────────────────────────────────
  const handleCalcularPrecio = async (e: React.FormEvent) => {
    e.preventDefault();

    // Marcar todos como tocados para mostrar todos los errores
    setTouched({ numMaestros: true, numAlumnos: true });

    const errors = validateForm(
      numMaestros === '' ? NaN : numMaestros,
      numAlumnos === '' ? NaN : numAlumnos
    );
    setFormErrors(errors);

    if (Object.keys(errors).length > 0) return;
    if (!organizacionId) return;

    try {
      setCalculando(true);
      setResumen(null);
      const res = await apiFetch(
        `${apiBase}/api/suscripciones/organizacion/${organizacionId}/calcular-precio`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ numMaestros, numAlumnos }),
        }
      );

      if (!res.ok) throw new Error('Error al calcular el precio');

      const data: ResumenCompraDTO = await res.json();
      setResumen(data);
    } catch (err) {
      console.error(err);
      setFormErrors((prev) => ({
        ...prev,
        numMaestros: 'Hubo un problema al calcular el precio. Inténtalo de nuevo.',
      }));
    } finally {
      setCalculando(false);
    }
  };

  // ── Proceder al pago ───────────────────────────────────
  const handleProcederPago = () => {
    if (!resumen || !organizacionId) return;
    // Navega a la pasarela de pago pasando el resumen como state
    navigate('/pago', {
      state: {
        numMaestros: resumen.numMaestros,
        numAlumnos: resumen.numAlumnos,
        precioTotal: resumen.precioTotal,
        organizacionId,
      },
    });
  };

  // ── Helpers de campo ───────────────────────────────────
  const handleBlur = (field: 'numMaestros' | 'numAlumnos') => {
    setTouched((prev) => ({ ...prev, [field]: true }));
  };

  const handleChangeInt =
    (setter: React.Dispatch<React.SetStateAction<number | ''>>) =>
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const raw = e.target.value;
      if (raw === '') {
        setter('');
        return;
      }
      const parsed = parseInt(raw, 10);
      setter(isNaN(parsed) ? '' : parsed);
      // Resetear resumen si cambian los valores
      setResumen(null);
    };

  // ── Render ─────────────────────────────────────────────
  if (loading) {
    return (
      <div className="sub-page">
        <NavbarMisCursos />
        <main className="sub-main">
          <p className="sub-loading">Cargando suscripción…</p>
        </main>
      </div>
    );
  }

  if (error) {
    return (
      <div className="sub-page">
        <NavbarMisCursos />
        <main className="sub-main">
          <p className="sub-error">{error}</p>
        </main>
      </div>
    );
  }

  return (
    <div className="sub-page">
      <NavbarMisCursos />

      <main className="sub-main">
        <div className="sub-wrapper">
          <h1 className="sub-title">Mi Suscripción</h1>

          {/* ── ESCENARIO A: SUSCRIPCIÓN ACTIVA ─────────── */}
          {suscripcion ? (
            <div className="sub-card">
              <div className="sub-active__header">
                <span className="sub-active__badge">✅ Activa</span>
                <h2 className="sub-active__title">Tu suscripción está vigente</h2>
              </div>

              <div className="sub-active__grid">
                <div className="sub-active__item">
                  <span className="sub-active__item-label">Estado del pago</span>
                  <span className="sub-active__item-value">{suscripcion.estadoPago}</span>
                </div>
                <div className="sub-active__item">
                  <span className="sub-active__item-label">Licencias profesores</span>
                  <span className="sub-active__item-value">{suscripcion.numMaestros}</span>
                </div>
                <div className="sub-active__item">
                  <span className="sub-active__item-label">Licencias alumnos</span>
                  <span className="sub-active__item-value">{suscripcion.numAlumnos}</span>
                </div>
                <div className="sub-active__item">
                  <span className="sub-active__item-label">Válida desde</span>
                  <span className="sub-active__item-value">
                    {new Date(suscripcion.fechaInicio).toLocaleDateString('es-ES')}
                  </span>
                </div>
                <div className="sub-active__item">
                  <span className="sub-active__item-label">Válida hasta</span>
                  <span className="sub-active__item-value">
                    {new Date(suscripcion.fechaFin).toLocaleDateString('es-ES')}
                  </span>
                </div>
              </div>

              <hr className="sub-active__divider" />

              <div className="sub-active__precio">
                <span className="sub-active__precio-label">Precio pagado:</span>
                <span className="sub-active__precio-amount">{suscripcion.precio} €</span>
              </div>
            </div>
          ) : (

          /* ── ESCENARIO B: SIN SUSCRIPCIÓN ─────────────── */
            <div className="sub-card">
              <h2 className="sub-form__title">No tienes una suscripción activa</h2>
              <p className="sub-form__subtitle">
                Configura tu plan introduciendo el número de profesores y alumnos.
              </p>

              <form className="sub-form" onSubmit={handleCalcularPrecio} noValidate>

                {/* Nº Profesores */}
                <div className="sub-form__field">
                  <label className="sub-form__label" htmlFor="numMaestros">
                    Número de Profesores
                  </label>
                  <input
                    id="numMaestros"
                    className={`sub-form__input${formErrors.numMaestros ? ' sub-form__input--error' : ''}`}
                    type="number"
                    min={1}
                    max={999}
                    step={1}
                    value={numMaestros}
                    onChange={handleChangeInt(setNumMaestros)}
                    onBlur={() => handleBlur('numMaestros')}
                    placeholder="Ej: 5"
                    aria-describedby={formErrors.numMaestros ? 'error-maestros' : undefined}
                    aria-invalid={!!formErrors.numMaestros}
                  />
                  {formErrors.numMaestros && (
                    <span id="error-maestros" className="sub-form__error" role="alert">
                      {formErrors.numMaestros}
                    </span>
                  )}
                </div>

                {/* Nº Alumnos */}
                <div className="sub-form__field">
                  <label className="sub-form__label" htmlFor="numAlumnos">
                    Número de Alumnos
                  </label>
                  <input
                    id="numAlumnos"
                    className={`sub-form__input${formErrors.numAlumnos ? ' sub-form__input--error' : ''}`}
                    type="number"
                    min={1}
                    max={9999}
                    step={1}
                    value={numAlumnos}
                    onChange={handleChangeInt(setNumAlumnos)}
                    onBlur={() => handleBlur('numAlumnos')}
                    placeholder="Ej: 100"
                    aria-describedby={formErrors.numAlumnos ? 'error-alumnos' : undefined}
                    aria-invalid={!!formErrors.numAlumnos}
                  />
                  {formErrors.numAlumnos && (
                    <span id="error-alumnos" className="sub-form__error" role="alert">
                      {formErrors.numAlumnos}
                    </span>
                  )}
                </div>

                <button
                  type="submit"
                  className="sub-form__btn-calcular"
                  disabled={calculando}
                >
                  {calculando ? 'Calculando…' : 'Calcular Precio'}
                </button>
              </form>

              {/* ── Resumen de precio calculado ─────────── */}
              {resumen && (
                <div className="sub-resumen">
                  <h3 className="sub-resumen__title">Resumen de tu plan</h3>

                  <div className="sub-resumen__row">
                    <span>Profesores</span>
                    <strong>{resumen.numMaestros}</strong>
                  </div>
                  <div className="sub-resumen__row">
                    <span>Alumnos</span>
                    <strong>{resumen.numAlumnos}</strong>
                  </div>

                  <hr className="sub-resumen__divider" />

                  <div className="sub-resumen__price-row">
                    <span className="sub-resumen__price-label">Total a pagar</span>
                    <span className="sub-resumen__price-amount">
                      {resumen.precioTotal} €
                      <span className="sub-resumen__price-period"> / mes</span>
                    </span>
                  </div>

                  {resumen.mensaje && (
                    <p className="sub-resumen__mensaje">{resumen.mensaje}</p>
                  )}

                  <button
                    className="sub-btn-pago"
                    onClick={handleProcederPago}
                    type="button"
                  >
                    Ir a pasarela de pago →
                  </button>
                </div>
              )}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}