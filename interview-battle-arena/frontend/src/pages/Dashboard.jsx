import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Dashboard() {
  const { user, refreshUser } = useAuth();

  // Reload stats every time the dashboard opens (they change after battles)
  useEffect(() => {
    refreshUser().catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const stats = [
    { label: 'Rating', value: user.rating },
    { label: 'Wins', value: user.wins },
    { label: 'Losses', value: user.losses },
    { label: 'Total battles', value: user.totalBattles },
  ];

  return (
    <main className="page">
      <h1 className="page-title">Welcome, {user.username}</h1>
      <p className="page-sub">Pick a mode and start answering.</p>

      <section className="stat-grid" aria-label="Your stats">
        {stats.map((s) => (
          <div className="card stat" key={s.label}>
            <span className="stat-value">{s.value}</span>
            <span className="stat-label">{s.label}</span>
          </div>
        ))}
      </section>

      <section className="action-grid" aria-label="Actions">
        <Link className="btn btn-primary" to="/create-battle">Create Battle</Link>
        <Link className="btn" to="/join-battle">Join Battle</Link>
        <Link className="btn" to="/practice">Practice</Link>
        <Link className="btn" to="/leaderboard">Leaderboard</Link>
        <Link className="btn" to="/history">Battle History</Link>
        <Link className="btn" to="/profile">Profile</Link>
      </section>
    </main>
  );
}