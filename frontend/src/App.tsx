import { useEffect, useState, useMemo} from "react";
import "./App.css";

type TestEntity = {
  id: number;
  name: string;
};

function App() {
  const API_BASE = useMemo(() => (import.meta.env.VITE_API_URL ?? "").trim(), []);
  const [items, setItems] = useState<TestEntity[]>([]);
  const [name, setName] = useState<string>("");
  const [loadingInsert, setLoadingInsert] = useState<boolean>(false);
  const [error, setError] = useState<string>("");

  const loadHello = async () => {
    try {
      const res = await fetch(`${API_BASE}/api/hello`);
      if (!res.ok) throw new Error(`GET /api/hello -> ${res.status}`);

    } catch (e) {
      console.error(e);
    }
  };

  const loadAll = async () => {
    setError("");
    try {
      const res = await fetch(`${API_BASE}/api/test`);
      if (!res.ok) throw new Error(`GET /api/test -> ${res.status}`);
      const data = await res.json();
      setItems(Array.isArray(data) ? data : []);
    } catch (e: any) {
      console.error(e);
      setError(e?.message ?? "Error cargando datos");
    } finally {
    }
  };

  const insert = async () => {
    const trimmed = name.trim();
    if (!trimmed) {
      setError("Escribe un nombre antes de insertar.");
      return;
    }

    setError("");
    setLoadingInsert(true);
    try {
      // Si tu backend acepta query param "name"
      const res = await fetch(`${API_BASE}/api/test?name=${encodeURIComponent(trimmed)}`, {
        method: "POST",
      });
      if (!res.ok) throw new Error(`POST /api/test -> ${res.status}`);
      const newItem: TestEntity = await res.json();

      // Optimista: lo añadimos y limpiamos input
      setItems(prev => [...prev, newItem]);
      setName("");
    } catch (e: any) {
      console.error(e);
      setError(e?.message ?? "Error insertando");
    } finally {
      setLoadingInsert(false);
    }
  };

  useEffect(() => {
    loadHello();
    loadAll();
  }, []);

  return (
    <div
      style={{
        minHeight: "100vh",
        display: "grid",
        placeItems: "center",
        padding: 24,
      }}
    >
      <div
        style={{
          width: "min(900px, 100%)",
          background: "rgba(255,255,255,0.06)",
          border: "1px solid rgba(255,255,255,0.12)",
          borderRadius: 16,
          padding: 24,
          backdropFilter: "blur(6px)",
        }}
      >
        {/* Header */}
        <div style={{ textAlign: "center", marginBottom: 20 }}>
          <h1 style={{ margin: 0, fontSize: 56, letterSpacing: 2 }}>
            cerebrus
          </h1>
          <p style={{ marginTop: 8, opacity: 0.85, fontSize: 16 }}>
            Prueba de conexion frontend + backend + db
          </p>
        </div>

        {/* Controls */}
        <div
          style={{
            display: "grid",
            gap: 12,
            gridTemplateColumns: "1fr",
            marginBottom: 18,
          }}
        >
          <div style={{ display: "flex", gap: 10, flexWrap: "wrap" }}>
            <input
              value={name}
              onChange={e => setName(e.target.value)}
              placeholder="Nombre para insertar en BD"
              style={{
                flex: "1 1 320px",
                padding: "10px 12px",
                borderRadius: 10,
                border: "1px solid rgba(255,255,255,0.18)",
                background: "rgba(0,0,0,0.25)",
                color: "inherit",
                outline: "none",
              }}
              onKeyDown={e => {
                if (e.key === "Enter") insert();
              }}
              disabled={loadingInsert}
            />

            <button
              onClick={insert}
              disabled={loadingInsert}
              style={{
                padding: "10px 14px",
                borderRadius: 10,
                border: "1px solid rgba(255,255,255,0.18)",
                background: "rgba(255,255,255,0.12)",
                color: "inherit",
                cursor: loadingInsert ? "not-allowed" : "pointer",
                minWidth: 180,
              }}
            >
              {loadingInsert ? "Insertando..." : "Insertar en la BD"}
            </button>
          </div>

          {error && (
            <div
              style={{
                padding: 12,
                borderRadius: 10,
                border: "1px solid rgba(255,80,80,0.45)",
                background: "rgba(255,80,80,0.10)",
              }}
            >
              {error}
            </div>
          )}
        </div>

        {/* List */}
        <div
          style={{
            borderTop: "1px solid rgba(255,255,255,0.12)",
            paddingTop: 16,
          }}
        >
          <div
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "baseline",
              gap: 12,
              marginBottom: 10,
            }}
          >
            <h2 style={{ margin: 0, fontSize: 18, opacity: 0.9 }}>
              Datos en la base de datos
            </h2>
            <span style={{ opacity: 0.7 }}>
              Total: <strong>{items.length}</strong>
            </span>
          </div>

          {items.length === 0 ? (
            <div style={{ opacity: 0.75 }}>
              No hay datos todavía. Inserta uno arriba.
            </div>
          ) : (
            <ul style={{ margin: 0, paddingLeft: 18 }}>
              {items.map(i => (
                <li key={i.id}>
                  <strong>#{i.id}</strong> — {i.name}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}

export default App;