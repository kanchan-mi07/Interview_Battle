import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { joinBattle } from '../services/battleService.js';

export default function JoinBattle() {
  const navigate = useNavigate();
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const battle = await joinBattle(code.trim().toUpperCase());
      navigate(`/battle/${battle.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Could not join the battle.');
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="page page-narrow">
      <h1 className="page-title">Join Battle</h1>
      <p className="page-sub">Enter the 6-character code your opponent shared.</p>

      <form className="card setup-card" onSubmit={onSubmit}>
        {error && <p className="alert alert-error">{error}</p>}
        <label htmlFor="code">Battle code</label>
        <input
          id="code" className="code-input" value={code} maxLength={6}
          onChange={(e) => setCode(e.target.value.toUpperCase())}
          autoComplete="off" required
        />
        <button className="btn btn-primary" disabled={busy || code.trim().length !== 6}>
          {busy ? 'Joining…' : 'Join battle'}
        </button>
      </form>
    </main>
  );
}