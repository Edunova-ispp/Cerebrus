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
import { CrucigramaForm } from './CrucigramaForm';

const TIPOS = ['Teoría', 'Tipo test', 'Poner en orden', 'Marcar en imagen', 'Tablero', 'Clasificación', 'Carta', 'Crucigrama'];

interface CrearActividadProps {
  readonly cursoIdProp?: string;
  readonly temaIdProp?: string;
  readonly embedded?: boolean;
  readonly onDone?: () => void;
}

export default function CrearActividad({ cursoIdProp, temaIdProp, embedded, onDone }: CrearActividadProps = {}) {
  const params = useParams<{ id: string; temaId: string }>();
  const cursoId = cursoIdProp ?? params.id;
  //const navigate = useNavigate();
  const [tipoSeleccionado, setTipoSeleccionado] = useState<string | null>(null);

  const formContent =
    tipoSeleccionado === 'Poner en orden' ? <OrdenacionForm temaIdProp={temaIdProp} cursoIdProp={cursoId} onDone={onDone} /> :
    tipoSeleccionado === 'Tipo test' ? <TestForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoId} onDone={onDone} /> :
    tipoSeleccionado === 'Teoría' ? <TeoriaForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoId} onDone={onDone} /> :
    tipoSeleccionado === 'Marcar en imagen' ? <MarcarImagenForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoId} onDone={onDone} /> :
    tipoSeleccionado === 'Tablero' ? <TableroForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoId} onDone={onDone} /> :
    tipoSeleccionado === 'Carta' ? <CartaForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoId} onDone={onDone} /> :
    tipoSeleccionado === 'Clasificación' ? <ClasificacionForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoId} onDone={onDone} /> :
    tipoSeleccionado === 'Crucigrama' ? <CrucigramaForm mode="create" temaIdProp={temaIdProp} cursoIdProp={cursoId} onDone={onDone} /> :
    <p className="ca-proximamente">Selecciona un tipo de actividad</p>;
/*
  const handleVolver = () => {
    if (embedded && onDone) {
      onDone();
    } else {
      navigate(`/cursos/${cursoId}`);
    }
  };
*/
  const sidebar = (
    <div className="ca-sidebar">
   
      {TIPOS.map((tipo) => (
        <button
          key={tipo}
          className={`ca-sidebar-btn${tipoSeleccionado === tipo ? ' ca-sidebar-btn--activo' : ''}`}
          type="button"
          onClick={() => setTipoSeleccionado(tipo)}
        >
          {tipo}
        </button>
      ))}
    </div>
  );

  if (embedded) {
    return (
      <div className="ca-embedded">
        {sidebar}
        <div className="ca-contenido">
          {formContent}
        </div>
      </div>
    );
  }

  return (
    <div className="ca-page">
      <NavbarMisCursos />
      <main className="ca-main">
        {sidebar}
        <div className="ca-contenido">
          {formContent}
        </div>
      </main>
    </div>
  );
}