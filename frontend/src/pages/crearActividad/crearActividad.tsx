import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { ClasificacionForm } from './ClasificacionForm';
import './crearActividad.css';
import { OrdenacionForm } from './OrdenacionForm';
import { TeoriaForm } from './TeoriaForm';
import { MarcarImagenForm } from './MarcarImagenForm';
import { TestForm } from './TestForm';
import { CartaForm } from './CartaForm';
import { TableroForm } from './TableroForm';

const TIPOS = ['Teoría', 'Tipo test', 'Poner en orden', 'Marcar en imagen', 'Tablero', 'Clasificación', 'Carta'];

interface CrearActividadProps {
  readonly temaIdProp?: string;
  readonly cursoIdProp?: string;
  readonly embedded?: boolean;
  readonly onDone?: () => void;
}

export default function CrearActividad({ temaIdProp, cursoIdProp, embedded, onDone }: CrearActividadProps = {}) {
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  const navigate = useNavigate();
  const [tipoSeleccionado, setTipoSeleccionado] = useState<string | null>(null);

  const formContent =
    tipoSeleccionado === 'Poner en orden' ? <OrdenacionForm temaIdProp={temaIdProp} cursoIdProp={cursoIdProp} onDone={onDone} /> :
    tipoSeleccionado === 'Tipo test' ? <TestForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoIdProp} onDone={onDone} /> :
    tipoSeleccionado === 'Teoría' ? <TeoriaForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoIdProp} onDone={onDone} /> :
    tipoSeleccionado === 'Marcar en imagen' ? <MarcarImagenForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoIdProp} onDone={onDone} /> :
    tipoSeleccionado === 'Tablero' ? <TableroForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoIdProp} onDone={onDone} /> :
    tipoSeleccionado === 'Carta' ? <CartaForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoIdProp} onDone={onDone} /> :
    tipoSeleccionado === 'Clasificación' ? <ClasificacionForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoIdProp} onDone={onDone} /> :
    <p className="ca-proximamente">Selecciona un tipo de actividad</p>;

  return (
    <div className={embedded ? 'ca-embedded' : 'ca-page'}>
      {!embedded && <NavbarMisCursos />}
      <main className="ca-main">
        <div className="ca-sidebar">
          {!embedded && (
            <button className="ca-sidebar-btn" onClick={() => navigate(`/cursos/${cursoId}`)}>
              Volver al Mapa
            </button>
          )}
          {TIPOS.map((tipo) => (
            <button
              key={tipo}
              className="ca-sidebar-btn"
              type="button"
              onClick={() => setTipoSeleccionado(tipo)}
            >
              {tipo}
            </button>
          ))}
        </div>

        <div className="ca-contenido">
          {formContent}
        </div>
      </main>
    </div>
  );
}