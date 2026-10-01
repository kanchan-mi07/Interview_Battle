import { useState } from 'react';
import { Link } from 'react-router-dom';
import QuestionCard from '../components/QuestionCard.jsx';
import Timer from '../components/Timer.jsx';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import { getPracticeQuestions, submitPractice } from '../services/questionService.js';
import { CATEGORIES, DIFFICULTIES, difficultyLabel } from '../constants.js';

const SECONDS_PER_QUESTION = 20;

export default function Practice() {
  // stage: setup | quiz | submitting | submit-failed | result
  const [stage, setStage] = useState('setup');
  const [settings, setSettings] = useState({ category: '', difficulty: '', count: 10 });
  const [questions, setQuestions] = useState([]);
  const [index, setIndex] = useState(0);
  const [answers, setAnswers] = useState([]);
  const [selected, setSelected] = useState(null);
  const [result, setResult] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const onSettingChange = (e) => setSettings({ ...settings, [e.target.name]: e.target.value });

  const start = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const qs = await getPracticeQuestions({
        category: settings.category || undefined,
        difficulty: settings.difficulty || undefined,
        count: Number(settings.count),
      });
      setQuestions(qs);
      setAnswers([]);
      setIndex(0);
      setSelected(null);
      setResult(null);
      setStage('quiz');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load questions.');
    } finally {
      setBusy(false);
    }
  };

  const finish = async (finalAnswers) => {
    setStage('submitting');
    try {
      setResult(await submitPractice(finalAnswers));
      setStage('result');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not submit your answers.');
      setStage('submit-failed');
    }
  };

  // choice = 'A'..'D', or null when the timer ran out
  const advance = (choice) => {
    const updated = [...answers, { questionId: questions[index].id, selectedOption: choice }];
    setAnswers(updated);
    setSelected(null);
    if (index + 1 < questions.length) {
      setIndex(index + 1);
    } else {
      finish(updated);
    }
  };

  if (stage === 'submitting') return <LoadingSpinner label="Checking your answers…" />;

  if (stage === 'submit-failed') {
    return (
      <main className="page">
        <p className="alert alert-error">{error}</p>
        <button className="btn btn-primary" onClick={() => finish(answers)}>Try again</button>
      </main>
    );
  }

  if (stage === 'quiz') {
    const isLast = index + 1 === questions.length;
    return (
      <main className="page page-narrow">
        <div className="quiz-top">
          <span className="question-count">{index} of {questions.length} answered</span>
          <Timer seconds={SECONDS_PER_QUESTION} resetKey={index} onExpire={() => advance(null)} />
        </div>
        <div className="progress">
          <div className="progress-bar" style={{ width: `${(index / questions.length) * 100}%` }} />
        </div>

        <QuestionCard
          question={questions[index]}
          number={index + 1}
          total={questions.length}
          selected={selected}
          onSelect={setSelected}
        />

        <button className="btn btn-primary quiz-submit" disabled={!selected} onClick={() => advance(selected)}>
          {isLast ? 'Finish' : 'Submit Answer'}
        </button>
      </main>
    );
  }

  if (stage === 'result') {
    return (
      <main className="page page-narrow">
        <h1 className="page-title">Practice complete</h1>
        <p className="page-sub">Practice does not change your rating.</p>

        <section className="stat-grid">
          <div className="card stat"><span className="stat-value">{result.score}</span><span className="stat-label">Score</span></div>
          <div className="card stat"><span className="stat-value">{result.correct}</span><span className="stat-label">Correct</span></div>
          <div className="card stat"><span className="stat-value">{result.wrong}</span><span className="stat-label">Wrong</span></div>
        </section>

        <h2 className="section-title">Explanations</h2>
        {result.results.map((r, i) => (
          <article key={r.questionId} className={`card review ${r.correct ? 'review-ok' : 'review-bad'}`}>
            <p className="review-q">{i + 1}. {r.questionText}</p>
            <p className="review-line">
              Your answer:{' '}
              {r.selectedOption ? `${r.selectedOption}. ${r.options[r.selectedOption]}` : 'No answer (time ran out)'}
            </p>
            {!r.correct && (
              <p className="review-line">Correct answer: {r.correctOption}. {r.options[r.correctOption]}</p>
            )}
            {r.explanation && <p className="review-expl">{r.explanation}</p>}
          </article>
        ))}

        <div className="action-row">
          <button className="btn btn-primary" onClick={() => setStage('setup')}>Practice again</button>
          <Link className="btn" to="/dashboard">Back to Dashboard</Link>
        </div>
      </main>
    );
  }

  // setup
  return (
    <main className="page page-narrow">
      <h1 className="page-title">Practice</h1>
      <p className="page-sub">Solo questions with explanations. No rating changes.</p>

      <form className="card setup-card" onSubmit={start}>
        {error && <p className="alert alert-error">{error}</p>}

        <label htmlFor="category">Category</label>
        <select id="category" name="category" value={settings.category} onChange={onSettingChange}>
          <option value="">All categories</option>
          {CATEGORIES.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
        </select>

        <label htmlFor="difficulty">Difficulty</label>
        <select id="difficulty" name="difficulty" value={settings.difficulty} onChange={onSettingChange}>
          <option value="">All difficulties</option>
          {DIFFICULTIES.map((d) => <option key={d} value={d}>{difficultyLabel(d)}</option>)}
        </select>

        <label htmlFor="count">Number of questions</label>
        <select id="count" name="count" value={settings.count} onChange={onSettingChange}>
          {[5, 10, 15, 20].map((n) => <option key={n} value={n}>{n}</option>)}
        </select>

        <button className="btn btn-primary" disabled={busy}>
          {busy ? 'Loading…' : 'Start practice'}
        </button>
      </form>
    </main>
  );
}