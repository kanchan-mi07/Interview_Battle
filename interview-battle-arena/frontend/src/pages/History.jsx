import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import { getHistory } from '../services/battleService.js';

const RESULT_LABEL = { WIN: 'Win', LOSS: 'Loss', DRAW: 'Draw' };

const formatChange = (n) => (n > 0 ? `+${n}` : `${n}`);

export default function History() {
  const [items, setItems] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getHistory()
      .then(setItems)
      .catch((err) => setError(err.response?.data?.message || 'Could not load your history.'));
  }, []);

  if (error) return <main className="page"><p className="alert alert-error">{error}</p></main>;
  if (!items) return <LoadingSpinner label="Loading history…" />;

  return (
    <main className="page">
      <h1 className="page-title">Battle History</h1>
      <p className="page-sub">Your last {items.length > 0 ? items.length : ''} completed battles.</p>

      {items.length === 0 ? (
        <section className="card empty">
          <p>You haven't completed a battle yet.</p>
          <Link className="btn btn-primary" to="/create-battle">Create a battle</Link>
        </section>
      ) : (
        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Opponent</th>
                <th className="num">Your Score</th>
                <th className="num">Opponent Score</th>
                <th>Result</th>
                <th className="num">Rating</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {items.map((h) => (
                <tr key={h.battleId}>
                  <td>{h.opponentUsername}</td>
                  <td className="num">{h.myScore}</td>
                  <td className="num">{h.opponentScore}</td>
                  <td><span className={`badge badge-result-${h.result.toLowerCase()}`}>{RESULT_LABEL[h.result]}</span></td>
                  <td className="num">{formatChange(h.ratingChange)}</td>
                  <td>{new Date(h.completedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </main>
  );
}