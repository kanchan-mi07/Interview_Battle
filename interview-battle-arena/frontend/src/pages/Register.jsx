import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register } from '../services/authService.js';

export default function Register() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const onSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await register(form);
      navigate('/login', { state: { registered: true } });
    } catch (err) {
      setError(err.response?.data?.message || 'Could not reach the server.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="auth-page">
      <form className="card auth-card" onSubmit={onSubmit}>
        <h1>Create your account</h1>
        {error && <p className="alert alert-error">{error}</p>}

        <label htmlFor="username">Username</label>
        <input id="username" name="username" value={form.username} onChange={onChange} required />

        <label htmlFor="email">Email</label>
        <input id="email" name="email" type="email" value={form.email} onChange={onChange} required />

        <label htmlFor="password">Password</label>
        <input id="password" name="password" type="password" minLength={6}
               value={form.password} onChange={onChange} required />

        <button className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Creating account…' : 'Register'}
        </button>
        <p className="auth-alt">Already registered? <Link to="/login">Log in</Link></p>
      </form>
    </main>
  );
}