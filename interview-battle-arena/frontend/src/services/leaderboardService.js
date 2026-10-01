import api from './api.js';

export const getLeaderboard = (page = 0, size = 10) =>
  api.get('/leaderboard', { params: { page, size } }).then((r) => r.data);