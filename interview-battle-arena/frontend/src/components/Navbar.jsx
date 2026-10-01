import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="navbar">
      <div className="navbar-inner">
        <Link to="/dashboard" className="navbar-brand">Interview Battle Arena</Link>

        <nav className="navbar-links" aria-label="Main">
          <NavLink to="/dashboard">Dashboard</NavLink>
          <NavLink to="/practice">Practice</NavLink>
          <NavLink to="/leaderboard">Leaderboard</NavLink>
          <NavLink to="/history">History</NavLink>
          <NavLink to="/profile">Profile</NavLink>
        </nav>

        <div className="navbar-user">
          <span className="badge">{user.rating}</span>
          <span className="navbar-name">{user.username}</span>
          <button className="btn btn-small" onClick={handleLogout}>Log out</button>
        </div>
      </div>
    </header>
  );
}