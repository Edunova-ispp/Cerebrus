import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import NavbarMisCursos from "../../components/NavbarMisCursos/NavbarMisCursos";
import { apiFetch } from "../../utils/api";
import { getCurrentUserInfo } from "../../types/curso";
import "./Perfil.css";

interface UserData {
  id: number;
  nombre: string;
  primerApellido: string;
  segundoApellido?: string;
  nombreUsuario: string;
  correoElectronico: string;
}

export default function Perfil() {
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
  const navigate = useNavigate();
  const userInfo = getCurrentUserInfo() as Record<string, unknown> | null;
  const roles = (userInfo?.authorities as string[]) ?? [];
  const isOrganizacion = roles.some(r => r.toUpperCase().includes('ORGANIZACION'));
  const organizacionId = userInfo?.id as number | undefined;

  const [user, setUser] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ nombre: '', primerApellido: '', segundoApellido: '', nombreUsuario: '', correoElectronico: '' });
  const [saving, setSaving] = useState(false);
  const [msg, setMsg] = useState<{ type: 'ok' | 'err'; text: string } | null>(null);

  useEffect(() => {
    (async () => {
      try {
        const res = await apiFetch(`${apiBase}/api/usuarios/me`);
        const data: UserData = await res.json();
        setUser(data);
        setForm({
          nombre: data.nombre,
          primerApellido: data.primerApellido,
          segundoApellido: data.segundoApellido ?? '',
          nombreUsuario: data.nombreUsuario,
          correoElectronico: data.correoElectronico ?? '',
        });
      } catch {
        setMsg({ type: 'err', text: 'Error al cargar perfil' });
      } finally {
        setLoading(false);
      }
    })();
  }, [apiBase]);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("role");
    navigate("/");
  };

  const handleSave = async () => {
    if (!user) return;
    setSaving(true);
    setMsg(null);
    try {
      const params = new URLSearchParams();
      if (form.nombre) params.set('nombre', form.nombre);
      if (form.primerApellido) params.set('primerApellido', form.primerApellido);
      params.set('segundoApellido', form.segundoApellido);
      if (form.nombreUsuario) params.set('nombreUsuario', form.nombreUsuario);
      if (form.correoElectronico) params.set('correoElectronico', form.correoElectronico);

      const res = await apiFetch(
        `${apiBase}/api/usuarios/me?${params.toString()}`,
        { method: 'PUT' }
      );
      const updated: UserData = await res.json();
      setUser(updated);
      setEditing(false);
      setMsg({ type: 'ok', text: 'Perfil actualizado correctamente' });
      setTimeout(() => setMsg(null), 3000);
    } catch (err: unknown) {
      setMsg({ type: 'err', text: err instanceof Error ? err.message : 'Error al actualizar' });
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <NavbarMisCursos />
      <div className="perfil-page">
        <div className="perfil-card">
          <h1 className="perfil-heading">Mi Perfil</h1>

          {msg && (
            <div className={`perfil-msg perfil-msg--${msg.type}`}>{msg.text}</div>
          )}

          {loading ? (
            <p className="perfil-loading">Cargando…</p>
          ) : !user ? (
            <p className="perfil-loading">No se pudo cargar el perfil</p>
          ) : !editing ? (
            <div className="perfil-fields">
              <div className="perfil-field">
                <span className="perfil-label">Nombre</span>
                <span className="perfil-value">{user.nombre}</span>
              </div>
              <div className="perfil-field">
                <span className="perfil-label">Primer Apellido</span>
                <span className="perfil-value">{user.primerApellido}</span>
              </div>
              <div className="perfil-field">
                <span className="perfil-label">Segundo Apellido</span>
                <span className="perfil-value">{user.segundoApellido || '—'}</span>
              </div>
              <div className="perfil-field">
                <span className="perfil-label">Usuario</span>
                <span className="perfil-value">{user.nombreUsuario}</span>
              </div>
              <div className="perfil-field">
                <span className="perfil-label">Email</span>
                <span className="perfil-value">{user.correoElectronico || '—'}</span>
              </div>
              <div className="perfil-actions">
                {isOrganizacion && (
                  <button className="perfil-btn perfil-btn--edit" onClick={() => setEditing(true)}>
                    Editar perfil
                  </button>
                )}
                <button className="perfil-btn perfil-btn--logout" onClick={handleLogout}>
                  Cerrar sesión
                </button>
              </div>
            </div>
          ) : (
            <div className="perfil-form">
              <label className="perfil-form-label">
                Nombre
                <input value={form.nombre} onChange={e => setForm(f => ({ ...f, nombre: e.target.value }))} />
              </label>
              <label className="perfil-form-label">
                Primer Apellido
                <input value={form.primerApellido} onChange={e => setForm(f => ({ ...f, primerApellido: e.target.value }))} />
              </label>
              <label className="perfil-form-label">
                Segundo Apellido
                <input value={form.segundoApellido} onChange={e => setForm(f => ({ ...f, segundoApellido: e.target.value }))} />
              </label>
              <label className="perfil-form-label">
                Usuario
                <input value={form.nombreUsuario} onChange={e => setForm(f => ({ ...f, nombreUsuario: e.target.value }))} />
              </label>
              <label className="perfil-form-label">
                Email
                <input type="email" value={form.correoElectronico} onChange={e => setForm(f => ({ ...f, correoElectronico: e.target.value }))} />
              </label>
              <div className="perfil-actions">
                <button className="perfil-btn perfil-btn--save" onClick={handleSave} disabled={saving}>
                  {saving ? 'Guardando…' : 'Guardar'}
                </button>
                <button className="perfil-btn perfil-btn--cancel" onClick={() => { setEditing(false); setMsg(null); }}>
                  Cancelar
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
}
