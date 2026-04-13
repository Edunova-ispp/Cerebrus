import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { apiFetch } from '../../utils/api';
import { getCurrentUserInfo } from '../../types/curso';
import './GestionUsuarios.css';

interface UsuarioRow {
  id: number;
  nombre: string;
  primerApellido: string;
  segundoApellido?: string;
  nombreUsuario: string;
  correoElectronico: string;
  rol: 'MAESTRO' | 'ALUMNO';
}

interface UsuarioDetalle {
  id: number;
  nombre: string;
  primerApellido: string;
  segundoApellido?: string;
  nombreUsuario: string;
  correoElectronico: string;
}

interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

type FiltroRol = 'TODOS' | 'MAESTRO' | 'ALUMNO';

export default function GestionUsuarios() {
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
  const navigate = useNavigate();
  const userInfo = getCurrentUserInfo() as Record<string, unknown> | null;
  const organizacionId = userInfo?.id as number | undefined;

  // ── State ──
  const [usuarios, setUsuarios] = useState<UsuarioRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filtro, setFiltro] = useState<FiltroRol>('TODOS');
  const [busqueda, setBusqueda] = useState('');
  const [pageMaestros, setPageMaestros] = useState(0);
  const [pageAlumnos, setPageAlumnos] = useState(0);
  const [totalMaestros, setTotalMaestros] = useState(0);
  const [totalAlumnos, setTotalAlumnos] = useState(0);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // ── Detail / Edit modal ──
  const [selectedUser, setSelectedUser] = useState<UsuarioDetalle | null>(null);
  const [selectedRol, setSelectedRol] = useState<'MAESTRO' | 'ALUMNO'>('ALUMNO');
  const [editing, setEditing] = useState(false);
  const [editForm, setEditForm] = useState({ nombre: '', primerApellido: '', segundoApellido: '', nombreUsuario: '', correoElectronico: '' });
  const [saving, setSaving] = useState(false);
  const [modalError, setModalError] = useState<string | null>(null);

  // ── Create modal ──
  const [showCreate, setShowCreate] = useState(false);
  const [createForm, setCreateForm] = useState({ nombre: '', primerApellido: '', segundoApellido: '', email: '', username: '', password: '', rol: 'ALUMNO' as 'MAESTRO' | 'ALUMNO' });
  const [creating, setCreating] = useState(false);
  const [createError, setCreateError] = useState<string | null>(null);

  // ── Delete confirm ──
  const [deleteConfirm, setDeleteConfirm] = useState<{ id: number; nombre: string } | null>(null);
  const [deleting, setDeleting] = useState(false);

  const PAGE_SIZE = 10;

  // ── Auth guard ──
  useEffect(() => {
    if (!userInfo) { navigate('/'); return; }
    const rol = (userInfo.authorities as string[])?.[0] ?? '';
    if (!rol.toUpperCase().includes('ORGANIZACION')) navigate('/');
  }, []);

  // ── Fetch users ──
  const fetchUsers = useCallback(async () => {
    if (!organizacionId) return;
    setLoading(true);
    setError(null);
    try {
      const results: UsuarioRow[] = [];

      if (filtro !== 'ALUMNO') {
        const res = await apiFetch(`${apiBase}/api/organizaciones/${organizacionId}/maestros?page=${pageMaestros}&size=${PAGE_SIZE}`);
        const data: PageResponse<UsuarioDetalle> = await res.json();
        setTotalMaestros(data.totalElements);
        data.content.forEach(u => results.push({ ...u, rol: 'MAESTRO' }));
      } else {
        setTotalMaestros(0);
      }

      if (filtro !== 'MAESTRO') {
        const res = await apiFetch(`${apiBase}/api/organizaciones/${organizacionId}/alumnos?page=${pageAlumnos}&size=${PAGE_SIZE}`);
        const data: PageResponse<UsuarioDetalle> = await res.json();
        setTotalAlumnos(data.totalElements);
        data.content.forEach(u => results.push({ ...u, rol: 'ALUMNO' }));
      } else {
        setTotalAlumnos(0);
      }

      setUsuarios(results);
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error al cargar usuarios');
    } finally {
      setLoading(false);
    }
  }, [organizacionId, apiBase, filtro, pageMaestros, pageAlumnos]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  // ── Filter by search text ──
  const filtered = usuarios.filter(u => {
    if (!busqueda.trim()) return true;
    const q = busqueda.toLowerCase();
    return (
      u.nombre.toLowerCase().includes(q) ||
      u.primerApellido.toLowerCase().includes(q) ||
      (u.segundoApellido ?? '').toLowerCase().includes(q) ||
      u.nombreUsuario.toLowerCase().includes(q) ||
      (u.correoElectronico ?? '').toLowerCase().includes(q)
    );
  });

  // ── Open detail ──
  const openDetail = async (userId: number, rol: 'MAESTRO' | 'ALUMNO') => {
    if (!organizacionId) return;
    setModalError(null);
    setEditing(false);
    try {
      const res = await apiFetch(`${apiBase}/api/organizaciones/${organizacionId}/usuarios/${userId}`);
      const data: UsuarioDetalle = await res.json();
      setSelectedUser(data);
      setSelectedRol(rol);
      setEditForm({
        nombre: data.nombre,
        primerApellido: data.primerApellido,
        segundoApellido: data.segundoApellido ?? '',
        nombreUsuario: data.nombreUsuario,
        correoElectronico: data.correoElectronico ?? '',
      });
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error al cargar detalle');
    }
  };

  // ── Save edit ──
  const handleSave = async () => {
    if (!organizacionId || !selectedUser) return;
    setSaving(true);
    setModalError(null);
    try {
      const params = new URLSearchParams();
      if (editForm.nombre) params.set('nombre', editForm.nombre);
      if (editForm.primerApellido) params.set('primerApellido', editForm.primerApellido);
      params.set('segundoApellido', editForm.segundoApellido);
      if (editForm.nombreUsuario) params.set('nombreUsuario', editForm.nombreUsuario);
      if (editForm.correoElectronico) params.set('correoElectronico', editForm.correoElectronico);

      const res = await apiFetch(
        `${apiBase}/api/organizaciones/${organizacionId}/usuarios/${selectedUser.id}/actualizar?${params.toString()}`
      );
      const updated: UsuarioDetalle = await res.json();
      setSelectedUser(updated);
      setEditing(false);
      setSuccessMsg('Usuario actualizado correctamente');
      setTimeout(() => setSuccessMsg(null), 3000);
      fetchUsers();
    } catch (err: unknown) {
      setModalError(err instanceof Error ? err.message : 'Error al actualizar');
    } finally {
      setSaving(false);
    }
  };

  // ── Delete ──
  const handleDelete = async () => {
    if (!organizacionId || !deleteConfirm) return;
    setDeleting(true);
    try {
      await apiFetch(
        `${apiBase}/api/organizaciones/${organizacionId}/usuarios/${deleteConfirm.id}/eliminar`,
        { method: 'DELETE' }
      );
      setDeleteConfirm(null);
      setSelectedUser(null);
      setSuccessMsg('Usuario eliminado correctamente');
      setTimeout(() => setSuccessMsg(null), 3000);
      fetchUsers();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Error al eliminar');
    } finally {
      setDeleting(false);
    }
  };

  // ── Create user ──
  const openCreateModal = () => {
    setCreateForm({ nombre: '', primerApellido: '', segundoApellido: '', email: '', username: '', password: '', rol: 'ALUMNO' });
    setCreateError(null);
    setShowCreate(true);
  };

  const handleCreate = async () => {
    setCreateError(null);
    const errors: string[] = [];
    if (!createForm.nombre.trim()) errors.push('El nombre es obligatorio');
    if (!createForm.primerApellido.trim()) errors.push('El primer apellido es obligatorio');
    if (!createForm.username.trim()) errors.push('El username es obligatorio');
    if (!createForm.password.trim()) errors.push('La contraseña es obligatoria');
    const emailTrimmed = createForm.email.trim();
    if (emailTrimmed && !emailTrimmed.includes('@')) errors.push('El email debe ser válido');
    if (errors.length > 0) { setCreateError(errors.join('. ')); return; }

    setCreating(true);
    try {
      const payload = {
        nombre: createForm.nombre.trim(),
        primerApellido: createForm.primerApellido.trim(),
        segundoApellido: createForm.segundoApellido.trim() || undefined,
        username: createForm.username.trim(),
        email: createForm.email.trim() || undefined,
        password: createForm.password,
        rol: createForm.rol,
      };
      await apiFetch(`${apiBase}/api/organizaciones/usuarios`, {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      setShowCreate(false);
      setSuccessMsg('Usuario creado con éxito');
      setTimeout(() => setSuccessMsg(null), 3000);
      fetchUsers();
    } catch (err: unknown) {
      setCreateError(err instanceof Error ? err.message : 'Error al crear el usuario');
    } finally {
      setCreating(false);
    }
  };

  const totalPagesMaestros = Math.ceil(totalMaestros / PAGE_SIZE);
  const totalPagesAlumnos = Math.ceil(totalAlumnos / PAGE_SIZE);

  return (
    <div className="gu-page">
      <NavbarMisCursos />

      <main className="gu-main">
        <div className="gu-wrapper">
          <h1 className="gu-title">Gestión de Usuarios</h1>

          {/* Success toast */}
          {successMsg && <div className="gu-toast gu-toast--ok">{successMsg}</div>}
          {error && <div className="gu-toast gu-toast--err">{error}</div>}

          {/* Create and import users */}
          <div>
            <button
              className="gu-filter-btn gu-create-user-btn"
              onClick={() => navigate('/gestion-usuarios/crear-usuario')}
            >
              Crear usuario
            </button>
            <button
              className="gu-filter-btn gu-create-user-btn"
              onClick={() => navigate('/gestion-usuarios/importar-usuarios')}
            >
              Importar usuarios
            </button>
          </div>

          {/* Toolbar */}
          <div className="gu-toolbar">
            <input
              className="gu-search"
              type="text"
              placeholder="Buscar por nombre, usuario o email…"
              value={busqueda}
              onChange={e => setBusqueda(e.target.value)}
            />
            <div className="gu-filters">
              {(['TODOS', 'MAESTRO', 'ALUMNO'] as FiltroRol[]).map(f => (
                <button
                  key={f}
                  className={`gu-filter-btn${filtro === f ? ' gu-filter-btn--active' : ''}`}
                  onClick={() => { setFiltro(f); setPageMaestros(0); setPageAlumnos(0); }}
                >
                  {f === 'TODOS' ? 'Todos' : f === 'MAESTRO' ? 'Profesores' : 'Alumnos'}
                </button>
              ))}
            </div>
          </div>

          {/* Table */}
          {loading ? (
            <div className="gu-loading">Cargando usuarios…</div>
          ) : filtered.length === 0 ? (
            <div className="gu-empty">No se encontraron usuarios</div>
          ) : (
            <div className="gu-table-wrap">
              <table className="gu-table">
                <thead>
                  <tr>
                    <th>Nombre completo</th>
                    <th>Usuario</th>
                    <th>Email</th>
                    <th>Rol</th>
                    <th>Acciones</th>
                  </tr>
                </thead>
                <tbody>
                  {filtered.map(u => (
                    <tr key={`${u.rol}-${u.id}`}>
                      <td>{u.nombre} {u.primerApellido} {u.segundoApellido ?? ''}</td>
                      <td>{u.nombreUsuario}</td>
                      <td>{u.correoElectronico || '—'}</td>
                      <td>
                        <span className={`gu-badge gu-badge--${u.rol.toLowerCase()}`}>
                          {u.rol === 'MAESTRO' ? 'Profesor' : 'Alumno'}
                        </span>
                      </td>
                      <td className="gu-actions-cell">
                        <button className="gu-btn gu-btn--detail" onClick={() => openDetail(u.id, u.rol)}>
                          Ver
                        </button>
                        <button
                          className="gu-btn gu-btn--delete"
                          onClick={() => setDeleteConfirm({ id: u.id, nombre: `${u.nombre} ${u.primerApellido}` })}
                        >
                          Eliminar
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination */}
          <div className="gu-pagination">
            {filtro !== 'ALUMNO' && totalPagesMaestros > 1 && (
              <div className="gu-page-group">
                <span className="gu-page-label">Profesores</span>
                <button disabled={pageMaestros === 0} onClick={() => setPageMaestros(p => p - 1)}>←</button>
                <span>{pageMaestros + 1} / {totalPagesMaestros}</span>
                <button disabled={pageMaestros >= totalPagesMaestros - 1} onClick={() => setPageMaestros(p => p + 1)}>→</button>
              </div>
            )}
            {filtro !== 'MAESTRO' && totalPagesAlumnos > 1 && (
              <div className="gu-page-group">
                <span className="gu-page-label">Alumnos</span>
                <button disabled={pageAlumnos === 0} onClick={() => setPageAlumnos(p => p - 1)}>←</button>
                <span>{pageAlumnos + 1} / {totalPagesAlumnos}</span>
                <button disabled={pageAlumnos >= totalPagesAlumnos - 1} onClick={() => setPageAlumnos(p => p + 1)}>→</button>
              </div>
            )}
          </div>
        </div>
      </main>

      {/* ── Detail / Edit Modal ── */}
      {selectedUser && (
        <div className="gu-overlay" onClick={() => setSelectedUser(null)}>
          <div className="gu-modal" onClick={e => e.stopPropagation()}>
            <button className="gu-modal-close" onClick={() => setSelectedUser(null)}>✕</button>
            <h2 className="gu-modal-title">{editing ? 'Editar Usuario' : 'Detalle de Usuario'}</h2>
            <span className={`gu-badge gu-badge--${selectedRol.toLowerCase()}`}>
              {selectedRol === 'MAESTRO' ? 'Profesor' : 'Alumno'}
            </span>

            {modalError && <div className="gu-toast gu-toast--err" style={{ margin: '12px 0' }}>{modalError}</div>}

            {!editing ? (
              <div className="gu-detail-fields">
                <div className="gu-field"><span className="gu-field-label">Nombre</span><span>{selectedUser.nombre}</span></div>
                <div className="gu-field"><span className="gu-field-label">Primer Apellido</span><span>{selectedUser.primerApellido}</span></div>
                <div className="gu-field"><span className="gu-field-label">Segundo Apellido</span><span>{selectedUser.segundoApellido || '—'}</span></div>
                <div className="gu-field"><span className="gu-field-label">Usuario</span><span>{selectedUser.nombreUsuario}</span></div>
                <div className="gu-field"><span className="gu-field-label">Email</span><span>{selectedUser.correoElectronico || '—'}</span></div>
                <div className="gu-modal-actions">
                  <button className="gu-btn gu-btn--primary" onClick={() => setEditing(true)}>Editar</button>
                  <button className="gu-btn gu-btn--delete" onClick={() => setDeleteConfirm({ id: selectedUser.id, nombre: `${selectedUser.nombre} ${selectedUser.primerApellido}` })}>Eliminar</button>
                </div>
              </div>
            ) : (
              <div className="gu-edit-form">
                <label className="gu-form-label">
                  Nombre
                  <input value={editForm.nombre} onChange={e => setEditForm(f => ({ ...f, nombre: e.target.value }))} />
                </label>
                <label className="gu-form-label">
                  Primer Apellido
                  <input value={editForm.primerApellido} onChange={e => setEditForm(f => ({ ...f, primerApellido: e.target.value }))} />
                </label>
                <label className="gu-form-label">
                  Segundo Apellido
                  <input value={editForm.segundoApellido} onChange={e => setEditForm(f => ({ ...f, segundoApellido: e.target.value }))} />
                </label>
                <label className="gu-form-label">
                  Usuario
                  <input value={editForm.nombreUsuario} onChange={e => setEditForm(f => ({ ...f, nombreUsuario: e.target.value }))} />
                </label>
                <label className="gu-form-label">
                  Email
                  <input type="email" value={editForm.correoElectronico} onChange={e => setEditForm(f => ({ ...f, correoElectronico: e.target.value }))} />
                </label>
                <div className="gu-modal-actions">
                  <button className="gu-btn gu-btn--primary" onClick={handleSave} disabled={saving}>
                    {saving ? 'Guardando…' : 'Guardar'}
                  </button>
                  <button className="gu-btn gu-btn--secondary" onClick={() => { setEditing(false); setModalError(null); }}>Cancelar</button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* ── Delete Confirmation ── */}
      {deleteConfirm && (
        <div className="gu-overlay" onClick={() => setDeleteConfirm(null)}>
          <div className="gu-modal gu-modal--small" onClick={e => e.stopPropagation()}>
            <h2 className="gu-modal-title">Confirmar eliminación</h2>
            <p className="gu-confirm-text">
              ¿Estás seguro de que deseas eliminar a <strong>{deleteConfirm.nombre}</strong>? Esta acción no se puede deshacer.
            </p>
            <div className="gu-modal-actions">
              <button className="gu-btn gu-btn--delete" onClick={handleDelete} disabled={deleting}>
                {deleting ? 'Eliminando…' : 'Eliminar'}
              </button>
              <button className="gu-btn gu-btn--secondary" onClick={() => setDeleteConfirm(null)}>Cancelar</button>
            </div>
          </div>
        </div>
      )}

      {/* ── Create User Modal ── */}
      {showCreate && (
        <div className="gu-overlay" onClick={() => setShowCreate(false)}>
          <div className="gu-modal" onClick={e => e.stopPropagation()}>
            <button className="gu-modal-close" onClick={() => setShowCreate(false)}>✕</button>
            <h2 className="gu-modal-title">Crear Usuario</h2>

            {createError && <div className="gu-toast gu-toast--err" style={{ margin: '12px 0' }}>{createError}</div>}

            <div className="gu-edit-form">
              <label className="gu-form-label">
                Nombre
                <input value={createForm.nombre} onChange={e => setCreateForm(f => ({ ...f, nombre: e.target.value }))} autoComplete="given-name" />
              </label>
              <label className="gu-form-label">
                Primer Apellido
                <input value={createForm.primerApellido} onChange={e => setCreateForm(f => ({ ...f, primerApellido: e.target.value }))} autoComplete="family-name" />
              </label>
              <label className="gu-form-label">
                Segundo Apellido (opcional)
                <input value={createForm.segundoApellido} onChange={e => setCreateForm(f => ({ ...f, segundoApellido: e.target.value }))} autoComplete="family-name" />
              </label>
              <label className="gu-form-label">
                Email (opcional)
                <input type="email" value={createForm.email} onChange={e => setCreateForm(f => ({ ...f, email: e.target.value }))} autoComplete="email" />
              </label>
              <label className="gu-form-label">
                Username
                <input value={createForm.username} onChange={e => setCreateForm(f => ({ ...f, username: e.target.value }))} autoComplete="username" />
              </label>
              <label className="gu-form-label">
                Contraseña
                <input type="password" value={createForm.password} onChange={e => setCreateForm(f => ({ ...f, password: e.target.value }))} autoComplete="new-password" />
              </label>
              <div className="gu-field gu-role-field">
                <span className="gu-field-label">TIPO DE USUARIO</span>
                <div className="gu-role-buttons">
                  <button
                    type="button"
                    className={`gu-filter-btn gu-role-btn--maestro${createForm.rol === 'MAESTRO' ? ' gu-role-btn--selected' : ''}`}
                    onClick={() => setCreateForm(f => ({ ...f, rol: 'MAESTRO' }))}
                  >
                    Profesor
                  </button>
                  <button
                    type="button"
                    className={`gu-filter-btn gu-role-btn--alumno${createForm.rol === 'ALUMNO' ? ' gu-role-btn--selected' : ''}`}
                    onClick={() => setCreateForm(f => ({ ...f, rol: 'ALUMNO' }))}
                  >
                    Alumno
                  </button>
                </div>
              </div>
              <div className="gu-modal-actions">
                <button className="gu-btn gu-btn--primary" onClick={handleCreate} disabled={creating}>
                  {creating ? 'Creando…' : 'Crear usuario'}
                </button>
                <button className="gu-btn gu-btn--secondary" onClick={() => setShowCreate(false)}>Cancelar</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
