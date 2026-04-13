import { type FormEvent, useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import NavbarMisCursos from '../../components/NavbarMisCursos/NavbarMisCursos';
import { getCurrentUserInfo } from '../../types/curso';
import './GestionUsuarios.css';

type ImportResult = {
  okMessage: string | null;
  rawErrorMessage: string | null;
  parsedErrors: string[];
};

export default function ImportarUsuarios() {
  const apiBase = (import.meta.env.VITE_API_URL ?? '').trim().replace(/\/$/, '');
  const navigate = useNavigate();

  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const userInfo = useMemo(() => getCurrentUserInfo() as Record<string, unknown> | null, []);

  const [submitting, setSubmitting] = useState(false);

  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [result, setResult] = useState<ImportResult>({
    okMessage: null,
    rawErrorMessage: null,
    parsedErrors: [],
  });

  // ── Auth guard (igual que Gestión de Usuarios) ──
  useEffect(() => {
    if (!userInfo) {
      navigate('/');
      return;
    }

    const rol = (userInfo.authorities as string[])?.[0] ?? '';
    if (!rol.toUpperCase().includes('ORGANIZACION')) navigate('/');
  }, [navigate, userInfo]);

  const parseBackendErrors = (rawMessage: string): string[] => {
    // Controller devuelve texto plano; si contiene lista separada por comas, la extraemos.
    // Ejemplo: "... Los errores son:: <err1>, <err2>, ..."
    const marker = 'Los errores son::';
    const idx = rawMessage.indexOf(marker);
    const tail = idx >= 0 ? rawMessage.slice(idx + marker.length) : rawMessage;
    const parts = tail
      .split(',')
      .map((p) => p.trim())
      .filter(Boolean);

    // Si no parece una lista (p.ej. solo un mensaje), no “inventamos” errores.
    if (parts.length <= 1) return [];
    return parts;
  };

  const validateFile = (file: File | null): string | null => {
    if (!file) return 'Selecciona un archivo .csv o .xlsx.';
    const name = file.name.toLowerCase();
    if (!(name.endsWith('.csv') || name.endsWith('.xlsx'))) {
      return 'Formato no soportado. Solo se permiten archivos .csv o .xlsx.';
    }
    return null;
  };

  const clearSelectedFile = () => {
    setSelectedFile(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setResult({ okMessage: null, rawErrorMessage: null, parsedErrors: [] });

    const fileError = validateFile(selectedFile);
    if (fileError) {
      setResult({ okMessage: null, rawErrorMessage: fileError, parsedErrors: [] });
      clearSelectedFile();
      return;
    }

    setSubmitting(true);
    try {
      const formData = new FormData();
      formData.append('archivo', selectedFile as File);

      const token = localStorage.getItem('token');
      const res = await fetch(`${apiBase}/api/organizaciones/importar-usuarios`, {
        method: 'POST',
        body: formData,
        headers: {
          ...(token ? { Authorization: `Bearer ${token}` } : {}),
        },
      });

      const message = (await res.text()).trim();

      if (!res.ok) {
        setResult({
          okMessage: null,
          rawErrorMessage: message || 'No se pudo importar el archivo.',
          parsedErrors: parseBackendErrors(message),
        });
        clearSelectedFile();
        return;
      }

      setResult({ okMessage: message || 'Archivo importado correctamente.', rawErrorMessage: null, parsedErrors: [] });
    } catch (err: unknown) {
      setResult({
        okMessage: null,
        rawErrorMessage: err instanceof Error ? err.message : 'Error al importar el archivo.',
        parsedErrors: [],
      });
      clearSelectedFile();
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
              <h1 className="gu-title">Importar usuarios</h1>
            </div>

            {result.okMessage && <div className="gu-toast gu-toast--ok">{result.okMessage}</div>}
            {result.rawErrorMessage && <div className="gu-toast gu-toast--err">{result.rawErrorMessage}</div>}

            <div className="gu-field">
              <span className="gu-field-label">FORMATO DEL ARCHIVO</span>
              <p className="gu-info-text">
                Para importar archivos es posible subir un archivo <strong>.csv</strong> o un <strong>.xlsx (Excel)</strong> con un <strong>máximo de 600 filas, asegúrese de que el archivo no excede este número de filas o de lo contrario no se importará ningún usuario</strong>.
                <br />
                La primera fila debe ser el encabezado con estas 7 columnas (en este orden):
                <br />
                <strong>Nombre, Primer Apellido, Segundo Apellido, Correo Electronico, Nombre de Usuario, Contrasena, Rol</strong>
                <br />
                En la columna <strong>Rol</strong> usa únicamente <strong>MAESTRO</strong> o <strong>ALUMNO</strong>.
              </p>

              <div className="gu-modal-actions">
                <a className="gu-filter-btn gu-create-user-btn" href="/usuarios_plantilla.csv" download>
                  Descargar plantilla CSV
                </a>
                <a className="gu-filter-btn gu-create-user-btn" href="/usuarios_plantilla.xlsx" download>
                  Descargar plantilla Excel
                </a>
              </div>
            </div>

            {result.parsedErrors.length > 0 && (
              <div className="gu-field">
                <span className="gu-field-label">ERRORES DETECTADOS</span>
                <div className="gu-confirm-text">
                  {result.parsedErrors.map((e) => (
                    <div key={e}>{e}</div>
                  ))}
                </div>
              </div>
            )}

            <label className="gu-form-label">
              Archivo (.csv o .xlsx)
              <input
                ref={fileInputRef}
                type="file"
                accept=".csv,.xlsx,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                onChange={(e) => {
                  const f = e.currentTarget.files?.[0] ?? null;
                  setSelectedFile(f);
                  setResult({ okMessage: null, rawErrorMessage: null, parsedErrors: [] });
                }}
              />
            </label>

            <div className="gu-modal-actions">
              <button className="gu-filter-btn" type="submit" disabled={submitting || !selectedFile}>
                {submitting ? 'Importando…' : 'Importar'}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
