import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import type { ResumenCompraDTO } from './Suscripcion';
import './Suscripcion.css';

interface LocationState {
  resumen: ResumenCompraDTO;
  organizacionId: number;
}

interface CrearSuscripcionResponseDTO {
  urlPago: string;
  suscripcionId: number;
  transaccionId: string;
}

const calcularMeses = (fechaInicio: string, fechaFin: string) => {
  if (!fechaInicio || !fechaFin) return 1; 
  const inicio = new Date(fechaInicio);
  const fin = new Date(fechaFin);
  return (fin.getFullYear() - inicio.getFullYear()) * 12 + (fin.getMonth() - inicio.getMonth());
};

export default function ResumenSuscripcion() {
  const navigate  = useNavigate();
  const location  = useLocation();
  const state     = location.state as LocationState | null;

  const [cargandoPago, setCargandoPago] = useState(false);
  const [errorPago, setErrorPago]       = useState<string | null>(null);

  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');

  if (!state?.resumen || !state?.organizacionId) {
    return (
      <div className="sub-page">
        <NavbarMisCursos />
        <main className="sub-main">
          <div className="sub-card" style={{ textAlign: 'center', gap: '20px' }}>
            <p className="sub-error">No hay datos de suscripción. Vuelve al formulario.</p>
            <button className="sub-btn-secondary" onClick={() => navigate('/suscripcion')}>
              ← Volver
            </button>
          </div>
        </main>
      </div>
    );
  }

  const { resumen, organizacionId } = state;

  const mesesSeguros = resumen.numMeses || calcularMeses(resumen.fechaInicio, resumen.fechaFin);

  const handleIrAPagar = async () => {
    try {
      setCargandoPago(true);
      setErrorPago(null);

      const res = await apiFetch(
        `${apiBase}/api/suscripciones/organizacion/${organizacionId}/crear`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            numMaestros: resumen.numMaestros,
            numAlumnos:  resumen.numAlumnos,
            numMeses:    mesesSeguros, 
          }),
        }
      );

      if (!res.ok) throw new Error(`Error ${res.status}`);

      const data: CrearSuscripcionResponseDTO = await res.json();

      window.location.href = data.urlPago;
    } catch (err) {
      console.error(err);
      setErrorPago('No se pudo iniciar el pago. Inténtalo de nuevo.');
    } finally {
      setCargandoPago(false);
    }
  };

  return (
    <div className="sub-page">
      <NavbarMisCursos />
      <main className="sub-main">
        <div className="sub-wrapper">

          <h1 className="sub-title">Resumen de tu suscripción</h1>

          <div className="sub-card rsm-card">

            {resumen.nombreOrganizacion && (
              <p className="rsm-org-name">{resumen.nombreOrganizacion}</p>
            )}

            <div className="rsm-grid">
              <div className="rsm-item">
                <span className="rsm-item__label">Profesores</span>
                <span className="rsm-item__value">{resumen.numMaestros}</span>
              </div>
              <div className="rsm-item">
                <span className="rsm-item__label">Alumnos</span>
                <span className="rsm-item__value">{resumen.numAlumnos}</span>
              </div>
              <div className="rsm-item">
                <span className="rsm-item__label">Duración</span>
                <span className="rsm-item__value">{mesesSeguros} mes{mesesSeguros !== 1 ? 'es' : ''}</span>
              </div>
              <div className="rsm-item">
                <span className="rsm-item__label">Inicio</span>
                <span className="rsm-item__value">
                  {new Date(resumen.fechaInicio).toLocaleDateString('es-ES')}
                </span>
              </div>
              <div className="rsm-item">
                <span className="rsm-item__label">Fin</span>
                <span className="rsm-item__value">
                  {new Date(resumen.fechaFin).toLocaleDateString('es-ES')}
                </span>
              </div>
            </div>

            <hr className="sub-active__divider" />

            <div className="rsm-precio-total">
              <span className="rsm-precio-total__label">Total a pagar</span>
              <span className="rsm-precio-total__amount">{resumen.precioTotal} €</span>
            </div>

            {resumen.mensaje && (
              <p className="sub-resumen__mensaje">{resumen.mensaje}</p>
            )}

            {errorPago && (
              <p className="sub-error" style={{ marginTop: 0 }}>{errorPago}</p>
            )}

            <div className="rsm-actions">
              <button
                className="sub-btn-secondary"
                onClick={() => navigate('/suscripcion')}
                disabled={cargandoPago}
              >
                ← Modificar
              </button>
              <button
                className="sub-btn-pago"
                onClick={handleIrAPagar}
                disabled={cargandoPago}
              >
                {cargandoPago ? 'Redirigiendo…' : 'Ir a pagar →'}
              </button>
            </div>
          </div>

        </div>
      </main>
    </div>
  );
}