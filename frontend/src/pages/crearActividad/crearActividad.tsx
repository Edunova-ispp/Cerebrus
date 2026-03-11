import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { ClasificacionForm } from './ClasificacionForm';
import './crearActividad.css';
import { OrdenacionForm } from './OrdenacionForm';
import { TeoriaForm } from './TeoriaForm';
import { MarcarImagenForm } from './MarcarImagenForm';
import { TestForm } from './TestForm';
import { TableroForm } from './TableroForm';

const TIPOS = ['Teoría', 'Tipo test', 'Poner en orden', 'Marcar en imagen', 'Tablero', 'Clasificación'];

export default function CrearActividad() {
  const { id: cursoId } = useParams<{ id: string; temaId: string }>();
  const navigate = useNavigate();
  const [tipoSeleccionado, setTipoSeleccionado] = useState<string | null>(null);

  const formContent =
    tipoSeleccionado === 'Poner en orden' ? <OrdenacionForm /> :
    tipoSeleccionado === 'Tipo test' ? <TestForm mode="create" /> :
    tipoSeleccionado === 'Teoría' ? <TeoriaForm mode="create" /> :
    tipoSeleccionado === 'Marcar en imagen' ? <MarcarImagenForm mode="create" /> :
    tipoSeleccionado === 'Tablero' ? <TableroForm mode="create" /> :
    tipoSeleccionado === 'Clasificación' ? <ClasificacionForm mode="create" /> :
    <p className="ca-proximamente">Selecciona un tipo de actividad</p>;

  return (
    <div className="ca-page">
      <NavbarMisCursos />
      <main className="ca-main">
        <div className="ca-sidebar">
          <button className="ca-sidebar-btn" onClick={() => navigate(`/cursos/${cursoId}/temas`)}>
            Volver al Mapa
          </button>
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