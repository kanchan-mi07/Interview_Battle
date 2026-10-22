import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import QuestionCard from '../components/QuestionCard.jsx';
import Timer from '../components/Timer.jsx';
import ScoreBoard from '../components/ScoreBoard.jsx';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import { finishBattle, getBattle, submitAnswer } from '../services/battleService.js';

export default function Battle() {
  const { battleId } = useParams();
  const navigate = useNavigate();

  const [battle, setBattle] = useState(null);
  const [selected, setSelected] = useState(null);
  const [feedback, setFeedback] = useState('');
  const [error, setError] = useState('');
  const [startAt, setStartAt] = useState(null); // when the pre-battle countdown ends
  const [, setTick] = useState(0);
  const submitting = useRef(false);
  const finishCalled = useRef(false);
  const seq = useRef(0); // bumped on every submit so stale poll responses can be discarded

  const load = useCallback(async () => {
    try {
      setBattle(await getBattle(battleId));
      setError('');
    } catch (err) {
      setError(err.response?.data?.message || 'Could not load the battle.');
    }
  }, [battleId]);

  useEffect(() => { load(); }, [load]);

  // Completed -> result page
  useEffect(() => {
    if (battle?.status === 'COMPLETED') navigate(`/battle/${battleId}/result`, { replace: true });
  }, [battle, battleId, navigate]);

  // Pre-battle countdown (server tells us how many seconds are left)
  useEffect(() => {
    setStartAt(battle && battle.startsInSeconds > 0 ? Date.now() + battle.startsInSeconds * 1000 : null);
  }, [battle]);

  useEffect(() => {
    if (!startAt) return;
    const id = setInterval(() => {
      if (Date.now() >= startAt) {
        clearInterval(id);
        setStartAt(null);
        load();
      } else {
        setTick((n) => n + 1);
      }
    }, 250);
    return () => clearInterval(id);
  }, [startAt, load]);

  // Short feedback message after each answer
  useEffect(() => {
    if (!feedback) return;
    const id = setTimeout(() => setFeedback(''), 1500);
    return () => clearTimeout(id);
  }, [feedback]);

  const allAnswered = battle && battle.answeredCount === battle.totalQuestions;

  // I finished: tell the server once
  useEffect(() => {
    if (!allAnswered || battle.status === 'COMPLETED' || finishCalled.current) return;
    finishCalled.current = true;
    finishBattle(battleId).then(setBattle).catch(() => {});
  }, [allAnswered, battle, battleId]);

  // Clear a half-picked option whenever the question changes (e.g. the server expired it)
  const currentQuestionId = battle?.currentQuestion?.id;
  useEffect(() => { setSelected(null); }, [currentQuestionId]);

  // Poll for the opponent's progress/score while the battle is live (not during the countdown)
  const poll = useCallback(async () => {
    if (submitting.current) return;
    const mine = seq.current;
    try {
      const latest = await getBattle(battleId);
      if (!submitting.current && seq.current === mine) setBattle(latest);
    } catch {
      /* ignore and retry on the next tick */
    }
  }, [battleId]);

  const polling = battle && battle.status !== 'COMPLETED' && !(battle.startsInSeconds > 0);
  useEffect(() => {
    if (!polling) return;
    const id = setInterval(poll, 2500);
    return () => clearInterval(id);
  }, [polling, poll]);

  // choice = 'A'..'D', or null when the timer ran out with nothing selected
  const submit = async (choice) => {
    if (submitting.current || !battle?.currentQuestion) return;
    submitting.current = true;
    seq.current += 1;
    setSelected(null);
    try {
      const res = await submitAnswer(battleId, {
        questionId: battle.currentQuestion.id,
        selectedOption: choice,
      });
      setFeedback(choice === null ? 'Time is up' : res.correct ? 'Correct +100' : 'Incorrect');
      setBattle(res.state);
    } catch (err) {
      setFeedback(err.response?.data?.message || 'Could not submit your answer.');
      await load(); // resync with the server
    } finally {
      seq.current += 1;
      submitting.current = false;
    }
  };

  const countdown = startAt ? Math.ceil((startAt - Date.now()) / 1000) : 0;

  if (error && !battle) return <main className="page"><p className="alert alert-error">{error}</p></main>;
  if (!battle) return <LoadingSpinner label="Loading battle…" />;

  if (battle.status === 'WAITING') {
    return (
      <main className="page page-narrow">
        <section className="card code-card">
          <p className="code-label">Waiting for an opponent</p>
          <p className="battle-code">{battle.battleCode}</p>
        </section>
      </main>
    );
  }

  if (countdown > 0) {
    return (
      <main className="page page-narrow">
        <ScoreBoard opponent={battle.opponent} myScore={0} opponentScore={0} />
        <section className="card countdown-card">
          <p>Battle starts in</p>
          <p className="countdown">{countdown}</p>
        </section>
      </main>
    );
  }

  if (allAnswered) {
    return (
      <main className="page page-narrow">
        <ScoreBoard opponent={battle.opponent} myScore={battle.myScore} opponentScore={battle.opponentScore} />
        <LoadingSpinner
          label={`You finished. Waiting for ${battle.opponent.username} (${battle.opponentAnsweredCount} / ${battle.totalQuestions} answered)…`}
        />
      </main>
    );
  }

  if (!battle.currentQuestion) return <LoadingSpinner />;

  return (
    <main className="page page-narrow">
      <ScoreBoard opponent={battle.opponent} myScore={battle.myScore} opponentScore={battle.opponentScore} />

      <div className="quiz-top">
        <span className="feedback" role="status">{feedback}</span>
        <Timer
          seconds={battle.secondsRemaining}
          total={battle.questionTimeSeconds}
          resetKey={battle.currentQuestion.id}
          onExpire={() => submit(selected)}
        />
      </div>
      <div className="progress">
        <div className="progress-bar" style={{ width: `${(battle.answeredCount / battle.totalQuestions) * 100}%` }} />
      </div>

      <QuestionCard
        question={battle.currentQuestion}
        number={battle.currentQuestionNumber}
        total={battle.totalQuestions}
        selected={selected}
        onSelect={setSelected}
      />

      <button className="btn btn-primary quiz-submit" disabled={!selected} onClick={() => submit(selected)}>
        Submit Answer
      </button>
    </main>
  );
}