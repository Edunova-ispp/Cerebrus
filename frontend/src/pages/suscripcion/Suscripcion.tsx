import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './Suscripcion.css';

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
  numMeses: number;
  fechaInicio: string;
  fechaFin: string;
  nombreOrganizacion?: string;
  mensaje?: string;
}

interface FormErrors {
  numMaestros?: string;
  numAlumnos?: string;
  numMeses?: string;
}

function validateForm(
  numMaestros: number,
  numAlumnos: number,
  numMeses: number
): FormErrors {
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

  if (!numMeses || isNaN(numMeses)) {
    errors.numMeses = 'El número de meses es obligatorio.';
  } else if (!Number.isInteger(numMeses)) {
    errors.numMeses = 'Debe ser un número entero.';
  } else if (numMeses < 1) {
    errors.numMeses = 'Mínimo 1 mes.';
  } else if (numMeses > 24) {
    errors.numMeses = 'El máximo permitido es 24 meses.';
  }

  return errors;
}

export default function Suscripcion() {
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
  const navigate = useNavigate();

  const userInfo = getCurrentUserInfo() as any;
  const rol: string | undefined =
    userInfo?.rol ?? userInfo?.role ?? userInfo?.authorities?.[0];
  const organizacionId: number | undefined = userInfo?.id ?? userInfo?.userId;

  useEffect(() => {
    if (!userInfo || !rol) { navigate('/'); return; }
    const rolNorm = rol.toUpperCase().replace('ROLE_', '');
    if (rolNorm !== 'ORGANIZACION' && rolNorm !== 'DUENO') {
      navigate('/');
    }
  }, []);

  const [suscripcion, setSuscripcion] = useState<SuscripcionDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // ── Estados formulario ─────────────────────────────────
  const [numMaestros, setNumMaestros] = useState<number | ''>('');
  const [numAlumnos, setNumAlumnos] = useState<number | ''>('');
  const [numMeses, setNumMeses] = useState<number | ''>('');
  const [formErrors, setFormErrors] = useState<FormErrors>({});
  const [touched, setTouched] = useState({
    numMaestros: false,
    numAlumnos: false,
    numMeses: false,
  });
  const [calculando, setCalculando] = useState(false);

  useEffect(() => {
    if (!organizacionId) return;

    const fetchSuscripcion = async () => {
      try {
        setLoading(true);
        const res = await apiFetch(`${apiBase}/api/suscripciones/activa/${organizacionId}`);
        
        if (res.status === 200) {
          setSuscripcion(await res.json());
        } else {
          setSuscripcion(null);
        }
      } catch (err) {
        setSuscripcion(null); 
      } finally {
        setLoading(false);
      }
    };
    fetchSuscripcion();
  }, [organizacionId, apiBase]);

  useEffect(() => {
    if (touched.numMaestros || touched.numAlumnos || touched.numMeses) {
      setFormErrors(
        validateForm(
          numMaestros === '' ? NaN : numMaestros,
          numAlumnos === '' ? NaN : numAlumnos,
          numMeses === '' ? NaN : numMeses
        )
      );
    }
  }, [numMaestros, numAlumnos, numMeses, touched]);

  const handleCalcular = async (e: React.FormEvent) => {
    e.preventDefault();
    setTouched({ numMaestros: true, numAlumnos: true, numMeses: true });

    const errors = validateForm(
      numMaestros === '' ? NaN : numMaestros,
      numAlumnos === '' ? NaN : numAlumnos,
      numMeses === '' ? NaN : numMeses
    );
    setFormErrors(errors);
    if (Object.keys(errors).length > 0) return;
    if (!organizacionId) return;

    try {
      setCalculando(true);
      const res = await apiFetch(
        `${apiBase}/api/suscripciones/organizacion/${organizacionId}/resumir-suscripcion-a-comprar`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ numMaestros, numAlumnos, numMeses }),
        }
      );
      if (!res.ok) throw new Error();
      const resumen: ResumenCompraDTO = await res.json();

      navigate('/resumen-suscripcion', { state: { resumen, organizacionId } });
    } catch {
      setFormErrors(prev => ({
        ...prev,
        numMaestros: 'Error al calcular el precio. Inténtalo de nuevo.',
      }));
    } finally {
      setCalculando(false);
    }
  };


  const handleBlur = (field: keyof typeof touched) =>
    setTouched(prev => ({ ...prev, [field]: true }));

  const handleChangeInt =
    (setter: React.Dispatch<React.SetStateAction<number | ''>>) =>
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const raw = e.target.value;
      setter(raw === '' ? '' : parseInt(raw, 10) || '');
    };

  if (loading) return (
    <div className="sub-page">
      <NavbarMisCursos />
      <main className="sub-main">
        <p className="sub-loading">Cargando suscripción…</p>
      </main>
    </div>
  );

  if (error) return (
    <div className="sub-page">
      <NavbarMisCursos />
      <main className="sub-main">
        <p className="sub-error">{error}</p>
      </main>
    </div>
  );

  return (
    <div className="sub-page">
      <NavbarMisCursos />
      <main className="sub-main">
        <div className="sub-wrapper">
          <h1 className="sub-title">Mi Suscripción</h1>

          {suscripcion ? (
            <div className="sub-card">
              <div className="sub-active__header">
                <span className="sub-active__badge">Activa</span>
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
            <div className="sub-layout">
              
              {/* ── BARRA DE PRECIOS ACTUALIZADA ── */}
              <aside className="sub-precios">
                <p className="sub-precios__title">Precios</p>
                
                {/* Bloque Profesores */}
                <div className="sub-precios__tier">
                  <span className="sub-precios__tier-label">Profesor</span>
                  <span className="sub-precios__tier-price">10 € <span>/ mes</span></span>
                  
                  <div className="sub-precios__tier-extra">
                    Si tienes <strong>+20</strong> profesores:<br />
                    <span>7 € / mes</span> por profesor extra
                  </div>
                </div>

                {/* Bloque Alumnos */}
                <div className="sub-precios__tier">
                  <span className="sub-precios__tier-label">Alumno</span>
                  <span className="sub-precios__tier-price">5 € <span>/ mes</span></span>
                  
                  <div className="sub-precios__tier-extra">
                    Si tienes <strong>+50</strong> alumnos:<br />
                    <span>3 € / mes</span> por alumno extra
                  </div>
                </div>

                <p className="sub-precios__disclaimer">
                  *Precios ilustrativos sin validez comercial
                </p>
              </aside>

              <div className="sub-card">
                <h2 className="sub-form__title">Configura tu plan</h2>
                <p className="sub-form__subtitle">
                  Elige profesores, alumnos y meses para ver el precio exacto.
                </p>

                <form className="sub-form" onSubmit={handleCalcular} noValidate>
                  <div className="sub-form__field">
                    <label className="sub-form__label" htmlFor="numMaestros">
                      Número de Profesores
                    </label>
                    <input
                      id="numMaestros"
                      className={`sub-form__input${formErrors.numMaestros ? ' sub-form__input--error' : ''}`}
                      type="number" min={1} max={999} step={1}
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

                  <div className="sub-form__field">
                    <label className="sub-form__label" htmlFor="numAlumnos">
                      Número de Alumnos
                    </label>
                    <input
                      id="numAlumnos"
                      className={`sub-form__input${formErrors.numAlumnos ? ' sub-form__input--error' : ''}`}
                      type="number" min={1} max={9999} step={1}
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

                  <div className="sub-form__field">
                    <label className="sub-form__label" htmlFor="numMeses">
                      Número de Meses
                    </label>
                    <input
                      id="numMeses"
                      className={`sub-form__input${formErrors.numMeses ? ' sub-form__input--error' : ''}`}
                      type="number" min={1} max={24} step={1}
                      value={numMeses}
                      onChange={handleChangeInt(setNumMeses)}
                      onBlur={() => handleBlur('numMeses')}
                      placeholder="Ej: 12"
                      aria-describedby={formErrors.numMeses ? 'error-meses' : undefined}
                      aria-invalid={!!formErrors.numMeses}
                    />
                    {formErrors.numMeses && (
                      <span id="error-meses" className="sub-form__error" role="alert">
                        {formErrors.numMeses}
                      </span>
                    )}
                  </div>

                  <button
                    type="submit"
                    className="sub-form__btn-calcular"
                    disabled={calculando}
                  >
                    {calculando ? 'Calculando…' : 'Ver resumen y precio →'}
                  </button>
                </form>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}