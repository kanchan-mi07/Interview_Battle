import api from './api.js';

export const getMe = () => api.get('/users/me').then((r) => r.data);