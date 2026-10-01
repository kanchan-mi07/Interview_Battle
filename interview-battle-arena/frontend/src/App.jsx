import { Routes, Route, Navigate } from 'react-router-dom';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Register from './pages/Register.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Profile from './pages/Profile.jsx';

import ProtectedRoute from './components/ProtectedRoute.jsx';
import Practice from './pages/Practice.jsx';
import CreateBattle from './pages/CreateBattle.jsx';
import JoinBattle from './pages/JoinBattle.jsx';
import Battle from './pages/Battle.jsx';
import BattleResult from './pages/BattleResult.jsx';
import History from './pages/History.jsx';
import Leaderboard from './pages/Leaderboard.jsx';

const protect = (element) => <ProtectedRoute>{element}</ProtectedRoute>;

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route path="/dashboard" element={protect(<Dashboard />)} />
      <Route path="/profile" element={protect(<Profile />)} />

      {/* Placeholders, replaced in later phases */}
      <Route path="/practice" element={protect(<Practice />)} />
           <Route path="/create-battle" element={protect(<CreateBattle />)} />
           <Route path="/join-battle" element={protect(<JoinBattle />)} />
           <Route path="/battle/:battleId" element={protect(<Battle />)} />
           <Route path="/battle/:battleId/result" element={protect(<BattleResult />)} />
       <Route path="/leaderboard" element={protect(<Leaderboard />)} />
           <Route path="/history" element={protect(<History />)} />

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}