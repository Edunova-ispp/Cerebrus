import { useNavigate } from "react-router-dom";

export default function Placeholder() {
  const navigate = useNavigate();

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: "1.5rem",
        height: "100vh",
      }}
    >
      <h1 style={{ fontFamily: "'Pixelify Sans', sans-serif", fontSize: "2rem" }}>
        🚧 En construcción
      </h1>
      <button
        onClick={() => navigate(-1)}
        style={{
          fontFamily: "'Pixelify Sans', sans-serif",
          fontSize: "1rem",
          padding: "0.6rem 1.4rem",
          cursor: "pointer",
          borderRadius: "8px",
          border: "2px solid #333",
          background: "transparent",
        }}
      >
        ← Volver
      </button>
    </div>
  );
}
