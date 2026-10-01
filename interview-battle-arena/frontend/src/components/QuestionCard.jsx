import { categoryLabel, difficultyLabel } from '../constants.js';

export default function QuestionCard({ question, number, total, selected, onSelect, disabled }) {
  return (
    <section className="card question-card">
      <div className="question-meta">
        <span className="question-count">Question {number} / {total}</span>
        <span className="badge">{categoryLabel(question.category)}</span>
        <span className={`badge badge-${question.difficulty.toLowerCase()}`}>
          {difficultyLabel(question.difficulty)}
        </span>
      </div>

      <h2 className="question-text">{question.questionText}</h2>

      <div className="option-list" role="radiogroup" aria-label="Answer options">
        {Object.entries(question.options).map(([key, text]) => (
          <button
            type="button"
            key={key}
            role="radio"
            aria-checked={selected === key}
            className={`option${selected === key ? ' option-selected' : ''}`}
            onClick={() => onSelect(key)}
            disabled={disabled}
          >
            <span className="option-key">{key}</span>
            <span>{text}</span>
          </button>
        ))}
      </div>
    </section>
  );
}