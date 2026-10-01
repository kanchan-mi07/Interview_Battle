import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createBattle, getBattle } from '../services/battleService.js';

export default function CreateBattle() {
  const navigate = useNavigate();
  const [battle, setBattle] = useState(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [copied, setCopied] = useState(false);

  const create = async () => {
    setError('');
    setBusy(true);
    try {
      setBattle(await createBattle());
    } catch (err) {
      setError(err.response?.data?.message || 'Could not create the battle.');
    } finally {
      setBusy(false);
    }
  };

  // Wait for the opponent: check every 2 seconds, go to the battle once someone joins
  useEffect(() => {
    if (!battle) return;
    const id = setInterval(async () => {
      try {
        const latest = await getBattle(battle.id);
        if (latest.status !== 'WAITING') navigate(`/battle/${battle.id}`);
      } catch {
        /* ignore and retry on the next tick */
      }
    }, 2000);
    return () => clearInterval(id);
  }, [battle, navigate]);

  const copy = async () => {
    try {
      await navigator.clipboard.writeText(battle.battleCode);
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    } catch {
      /* clipboard can be blocked; the code is still visible */
    }
  };

  return (
    <main className="page page-narrow">
      <h1 className="page-title">Create Battle</h1>
      <p className="page-sub">10 random questions, 20 seconds each. Highest score wins.</p>

      {error && <p className="alert alert-error">{error}</p>}

      {!battle ? (
        <button className="btn btn-primary" onClick={create} disabled={busy}>
          {busy ? 'Creating…' : 'Create battle'}
        </button>
      ) : (
        <section className="card code-card">
          <p className="code-label">Your Battle Code</p>
          <p className="battle-code">{battle.battleCode}</p>
          <button className="btn btn-small" onClick={copy}>{copied ? 'Copied' : 'Copy code'}</button>
          <p className="code-hint">Share this code with another player. The battle starts when they join.</p>
        </section>
      )}
    </main>
  );
}