import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { OrdenacionForm } from './OrdenacionForm';
import { TeoriaForm } from './TeoriaForm';
import { TestForm } from './TestForm';
import { CartaForm } from './CartaForm';
import { TableroForm } from './TableroForm';
import './crearActividad.css';

const TIPOS = ['Teoría', 'Tipo test', 'Poner en orden', 'Tablero', 'Carta'];

export default function CrearActividad() {
  const { id: cursoId } = useParams<{ id: string; temaId: string }>();
  const navigate = useNavigate();
  const [tipoSeleccionado, setTipoSeleccionado] = useState<string | null>(null);

  const formContent =
    tipoSeleccionado === 'Poner en orden' ? <OrdenacionForm /> :
    tipoSeleccionado === 'Tipo test' ? <TestForm mode="create" /> :
    tipoSeleccionado === 'Teoría' ? <TeoriaForm mode="create" /> :
    tipoSeleccionado === 'Tablero' ? <TableroForm mode="create" /> :
    tipoSeleccionado === 'Carta' ? <CartaForm mode="create" /> :
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