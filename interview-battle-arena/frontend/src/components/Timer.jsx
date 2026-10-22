import { useEffect, useRef, useState } from 'react';

// Counts down from `seconds` using a real deadline (so a throttled background tab cannot
// make it drift). It restarts only when `resetKey` changes and calls onExpire once at 0.
// `total` is the full length of a question and is only used for the progress bar.
export default function Timer({ seconds = 20, total = seconds, resetKey, onExpire }) {
  const [left, setLeft] = useState(seconds);
  const secondsRef = useRef(seconds);
  const expireRef = useRef(onExpire);
  secondsRef.current = seconds;
  expireRef.current = onExpire; // always call the latest callback (it sees the latest selection)

  useEffect(() => {
    const endsAt = Date.now() + secondsRef.current * 1000;
    let fired = false;
    const tick = () => {
      const remaining = Math.max(0, Math.ceil((endsAt - Date.now()) / 1000));
      setLeft(remaining);
      if (remaining === 0 && !fired) {
        fired = true;
        clearInterval(id);
        expireRef.current?.();
      }
    };
    setLeft(secondsRef.current);
    const id = setInterval(tick, 250);
    tick();
    return () => clearInterval(id);
  }, [resetKey]);

  return (
    <div className={`timer${left <= 5 ? ' timer-low' : ''}`} role="timer" aria-live="off">
      <span>Time: {left} seconds</span>
      <div className="timer-track">
        <div className="timer-fill" style={{ width: `${Math.min(100, (left / total) * 100)}%` }} />
      </div>
    </div>
  );
}