import { useEffect, useRef, useState } from 'react';

// Counts down from `seconds`. Changing `resetKey` restarts it. Calls onExpire once at 0.
export default function Timer({ seconds = 20, resetKey, onExpire }) {
  const [left, setLeft] = useState(seconds);
  const expireRef = useRef(onExpire);
  expireRef.current = onExpire; // always call the latest callback

  useEffect(() => {
    setLeft(seconds);
    const id = setInterval(() => setLeft((prev) => (prev > 0 ? prev - 1 : 0)), 1000);
    return () => clearInterval(id);
  }, [resetKey, seconds]);

  useEffect(() => {
    if (left === 0) expireRef.current?.();
  }, [left]);

  return (
    <div className={`timer${left <= 5 ? ' timer-low' : ''}`} role="timer" aria-live="off">
      <span>Time: {left} seconds</span>
      <div className="timer-track">
        <div className="timer-fill" style={{ width: `${(left / seconds) * 100}%` }} />
      </div>
    </div>
  );
}