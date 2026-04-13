import { type FormEvent, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './GestionUsuarios.css';

type Rol = 'MAESTRO' | 'ALUMNO';

type FormState = {
  nombre: string;
  primerApellido: string;
  segundoApellido: string;
  email: string;
  username: string;
  password: string;
  rol: Rol;
};

export default function CreacionUsuario() {
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
  const navigate = useNavigate();

  const userInfo = useMemo(() => getCurrentUserInfo() as Record<string, unknown> | null, []);

  const [form, setForm] = useState<FormState>({
    nombre: '',
    primerApellido: '',
    segundoApellido: '',
    email: '',
    username: '',
    password: '',
    rol: 'ALUMNO',
  });

  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // ── Auth guard (igual que Gestión de Usuarios) ──
  useEffect(() => {
    if (!userInfo) {
      navigate('/');
      return;
    }

    const rol = (userInfo.authorities as string[])?.[0] ?? '';
    if (!rol.toUpperCase().includes('ORGANIZACION')) navigate('/');
  }, [navigate, userInfo]);

  const validate = (): string[] => {
    const errors: string[] = [];

    if (!form.nombre.trim()) errors.push('El nombre es obligatorio');
    if (!form.primerApellido.trim()) errors.push('El primer apellido es obligatorio');
    if (!form.username.trim()) errors.push('El username es obligatorio');
    if (!form.password.trim()) errors.push('La contraseña es obligatoria');

    const emailTrimmed = form.email.trim();
    if (emailTrimmed && !emailTrimmed.includes('@')) errors.push('El email debe ser válido');

    return errors;
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    const errors = validate();
    if (errors.length > 0) {
      setErrorMsg(errors.join('. '));
      return;
    }

    setSubmitting(true);
    try {
      const payload = {
        nombre: form.nombre.trim(),
        primerApellido: form.primerApellido.trim(),
        segundoApellido: form.segundoApellido.trim() || undefined,
        username: form.username.trim(),
        email: form.email.trim() || undefined,
        password: form.password,
        rol: form.rol,
      };

      const res = await apiFetch(`${apiBase}/api/organizaciones/usuarios`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });

      // backend devuelve texto, pero tras crear redirigimos a Gestión de Usuarios
      await res.text();
      navigate('/gestion-usuarios', { state: { toast: 'Usuario creado con éxito' } });
    } catch (err: unknown) {
      setErrorMsg(err instanceof Error ? err.message : 'Error al crear el usuario');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="gu-page">
      <NavbarMisCursos />

      <main className="gu-main">
        <div className="gu-wrapper">
          <form className="gu-edit-form gu-create-form" onSubmit={handleSubmit}>
            <div className="gu-create-header">
              <button
                type="button"
                className="gu-btn gu-btn--primary gu-back-btn"
                onClick={() => navigate(-1)}
                aria-label="Volver"
              >
                ←
              </button>
              <h1 className="gu-title">Crear Usuario</h1>
            </div>
            {errorMsg && <div className="gu-toast gu-toast--err">{errorMsg}</div>}

            <label className="gu-form-label">
              Nombre
              <input
                value={form.nombre}
                onChange={e => setForm(f => ({ ...f, nombre: e.target.value }))}
                autoComplete="given-name"
              />
            </label>

            <label className="gu-form-label">
              Primer apellido
              <input
                value={form.primerApellido}
                onChange={e => setForm(f => ({ ...f, primerApellido: e.target.value }))}
                autoComplete="family-name"
              />
            </label>

            <label className="gu-form-label">
              Segundo apellido (opcional)
              <input
                value={form.segundoApellido}
                onChange={e => setForm(f => ({ ...f, segundoApellido: e.target.value }))}
                autoComplete="family-name"
              />
            </label>

            <label className="gu-form-label">
              Correo electrónico (opcional)
              <input
                type="email"
                value={form.email}
                onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                autoComplete="email"
              />
            </label>

            <label className="gu-form-label">
              Username
              <input
                value={form.username}
                onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
                autoComplete="username"
              />
            </label>

            <label className="gu-form-label">
              Contraseña
              <input
                type="password"
                value={form.password}
                onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                autoComplete="new-password"
              />
            </label>

            <div className="gu-field gu-role-field">
              <span className="gu-field-label">TIPO DE USUARIO</span>
              <div className="gu-role-buttons">
                <button
                  type="button"
                  className={`gu-filter-btn gu-role-btn--maestro${form.rol === 'MAESTRO' ? ' gu-role-btn--selected' : ''}`}
                  onClick={() => setForm(f => ({ ...f, rol: 'MAESTRO' }))}
                >
                  Profesor
                </button>
                <button
                  type="button"
                  className={`gu-filter-btn gu-role-btn--alumno${form.rol === 'ALUMNO' ? ' gu-role-btn--selected' : ''}`}
                  onClick={() => setForm(f => ({ ...f, rol: 'ALUMNO' }))}
                >
                  Alumno
                </button>
              </div>
            </div>

            <div className="gu-modal-actions">
              <button className="gu-filter-btn" type="submit" disabled={submitting}>
                {submitting ? 'Creando…' : 'Crear usuario'}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
