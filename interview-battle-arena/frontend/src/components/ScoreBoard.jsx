export default function ScoreBoard({ opponent, myScore, opponentScore }) {
  return (
    <section className="card scoreboard" aria-label="Scores">
      <div className="score-side">
        <span className="score-name">Opponent: {opponent?.username ?? 'Waiting…'}</span>
        <span className="score-rating">Rating: {opponent?.rating ?? '-'}</span>
      </div>
      <div className="score-numbers">
        <div><span className="score-label">Your score</span><strong>{myScore}</strong></div>
        <div><span className="score-label">Opponent score</span><strong>{opponentScore}</strong></div>
      </div>
    </section>
  );
}