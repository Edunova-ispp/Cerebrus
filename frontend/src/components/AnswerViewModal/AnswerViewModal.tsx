import './AnswerViewModal.css';

export interface AnswerItem {
  readonly question: string;
  readonly studentAnswer?: string;
  readonly correctAnswer?: string;
  readonly isCorrect?: boolean;
}

interface Props {
  readonly title: string;
  readonly answers: readonly AnswerItem[];
  readonly onClose: () => void;
  readonly mode?: 'student' | 'correct'; // student = solo respuesta del alumno, correct = solo correcta
}

export default function AnswerViewModal({
  title,
  answers,
  onClose,
  mode = 'student'
}: Props) {
  return (
    <div
      className="avm-overlay"
      onMouseDown={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="avm-modal">
        <div className="avm-header">
          <h3 className="avm-title">{title}</h3>
          <button className="avm-close" type="button" onClick={onClose}>
            ✕
          </button>
        </div>

        <div className="avm-content">
          {answers && answers.length > 0 ? (
            answers.map((answer, idx) => (
              <div key={idx} className="avm-answer-item">
                <p className="avm-question">
                  <strong>Pregunta {idx + 1}:</strong> {answer.question}
                </p>

                {mode === 'student' && answer.studentAnswer && (
                  <div className={`avm-answer ${answer.isCorrect ? 'correct' : 'incorrect'}`}>
                    <p className="avm-answer-label">Tu respuesta:</p>
                    <p className="avm-answer-text">{answer.studentAnswer}</p>
                  </div>
                )}

                {mode === 'correct' && answer.correctAnswer && (
                  <div className="avm-answer correct">
                    <p className="avm-answer-label">Respuesta correcta:</p>
                    <p className="avm-answer-text">{answer.correctAnswer}</p>
                  </div>
                )}

                {mode === 'student' && answer.isCorrect !== undefined && (
                  <p className={`avm-status ${answer.isCorrect ? 'correct' : 'incorrect'}`}>
                    {answer.isCorrect ? '✓ Correcta' : '✗ Incorrecta'}
                  </p>
                )}
              </div>
            ))
          ) : (
            <p className="avm-empty">No hay respuestas disponibles</p>
          )}
        </div>

        <div className="avm-footer">
          <button className="avm-btn" type="button" onClick={onClose}>
            Cerrar
          </button>
        </div>
      </div>
    </div>
  );
}
