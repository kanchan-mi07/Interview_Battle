import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { getBattle } from '../services/battleService.js';

const HEADLINE = { WIN: 'YOU WON', LOSS: 'YOU LOST', DRAW: 'DRAW' };

export default function BattleResult() {
  const { battleId } = useParams();
  const navigate = useNavigate();
  const { user, refreshUser } = useAuth();
  const [battle, setBattle] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getBattle(battleId)
      .then((b) => {
        if (b.status !== 'COMPLETED') navigate(`/battle/${battleId}`, { replace: true });
        else setBattle(b);
      })
      .catch((err) => setError(err.response?.data?.message || 'Could not load the result.'));
    refreshUser().catch(() => {}); // pick up the new rating for the navbar
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [battleId]);

  if (error) return <main className="page"><p className="alert alert-error">{error}</p></main>;
  if (!battle) return <LoadingSpinner label="Loading result…" />;

  const { outcome, ratingBefore, ratingAfter } = battle.result;

  return (
    <main className="page page-narrow">
      <h1 className="page-title">Battle completed</h1>

      <section className={`card result-card result-${outcome.toLowerCase()}`}>
        <p className="result-headline">{HEADLINE[outcome]}</p>

        <div className="result-players">
          <div><span className="score-label">You ({user.username})</span><strong>{battle.myScore}</strong></div>
          <div><span className="score-label">Opponent ({battle.opponent.username})</span><strong>{battle.opponentScore}</strong></div>
        </div>

        <p className="result-rating">Rating: {ratingBefore} → {ratingAfter}</p>
      </section>

      <div className="action-row">
        <Link className="btn btn-primary" to="/history">View History</Link>
        <Link className="btn" to="/dashboard">Back to Dashboard</Link>
      </div>
    </main>
  );
}