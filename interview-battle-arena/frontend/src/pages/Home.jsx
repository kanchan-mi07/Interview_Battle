import { useEffect, useState } from 'react';
import api from '../services/api.js';
import { Link } from 'react-router-dom'

export default function Home() {
  const [status, setStatus] = useState('checking');

  useEffect(() => {
    api
      .get('/health')
      .then((res) => setStatus(res.data.status === 'UP' ? 'up' : 'down'))
      .catch(() => setStatus('down'));
  }, []);

  const label = {
    checking: 'Checking backend…',
    up: 'Backend connected on port 8080',
    down: 'Backend unreachable. Is Spring Boot running?',
  }[status];

  return (
    <main className="home">
      <h1 className="home-title">Interview Battle Arena</h1>
      <p className="home-sub">
        Challenge a friend to a timed Java, Spring Boot, SQL and DSA quiz. Highest score wins.
      </p>
      <p className={`status status-${status}`}>
        <span className="dot" aria-hidden="true" />
        {label}
      </p>
      <div className="home-actions">
              <Link className="btn btn-primary" to="/register">Get started</Link>
              <Link className="btn" to="/login">Log in</Link>
            </div>
    </main>
  );
}
