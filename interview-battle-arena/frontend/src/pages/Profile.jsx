import { useAuth } from '../context/AuthContext.jsx';

export default function Profile() {
  const { user } = useAuth();

  const rows = [
    ['Username', user.username],
    ['Email', user.email],
    ['Rating', user.rating],
    ['Wins', user.wins],
    ['Losses', user.losses],
    ['Total battles', user.totalBattles],
    ['Win rate', `${user.winRate}%`],
  ];

  return (
    <main className="page">
      <h1 className="page-title">Profile</h1>
      <p className="page-sub">Your rating and stats are updated by the server after each battle.</p>

      <section className="card profile-card">
        <dl className="profile-list">
          {rows.map(([label, value]) => (
            <div className="profile-row" key={label}>
              <dt>{label}</dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>
      </section>
    </main>
  );
}