import { useEffect, useState } from 'react';
import LoadingSpinner from '../components/LoadingSpinner.jsx';
import { getLeaderboard } from '../services/leaderboardService.js';

const PAGE_SIZE = 10;

export default function Leaderboard() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    setError('');
    getLeaderboard(page, PAGE_SIZE)
      .then(setData)
      .catch((err) => setError(err.response?.data?.message || 'Could not load the leaderboard.'));
  }, [page]);

  if (error) return <main className="page"><p className="alert alert-error">{error}</p></main>;
  if (!data) return <LoadingSpinner label="Loading leaderboard…" />;

  return (
    <main className="page">
      <h1 className="page-title">Leaderboard</h1>
      <p className="page-sub">{data.totalPlayers} players, ranked by rating.</p>

      <div className="table-wrap">
        <table className="table">
          <thead>
            <tr>
              <th className="num">Rank</th>
              <th>Username</th>
              <th className="num">Rating</th>
              <th className="num">Wins</th>
              <th className="num">Losses</th>
              <th className="num">Battles</th>
            </tr>
          </thead>
          <tbody>
            {data.entries.map((e) => (
              <tr key={e.rank} className={e.you ? 'row-you' : ''}>
                <td className="num">{e.rank}</td>
                <td>{e.username}{e.you && <span className="badge you-badge">You</span>}</td>
                <td className="num">{e.rating}</td>
                <td className="num">{e.wins}</td>
                <td className="num">{e.losses}</td>
                <td className="num">{e.totalBattles}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {data.totalPages > 1 && (
        <nav className="pager" aria-label="Leaderboard pages">
          <button className="btn btn-small" disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <span>Page {data.page + 1} of {data.totalPages}</span>
          <button className="btn btn-small" disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
        </nav>
      )}
    </main>
  );
}