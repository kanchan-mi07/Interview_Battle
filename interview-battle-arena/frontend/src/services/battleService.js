import api from './api.js';

export const createBattle = () => api.post('/battles').then((r) => r.data);
export const joinBattle = (battleCode) => api.post('/battles/join', { battleCode }).then((r) => r.data);
export const getBattle = (id) => api.get(`/battles/${id}`).then((r) => r.data);
export const submitAnswer = (id, payload) => api.post(`/battles/${id}/answer`, payload).then((r) => r.data);
export const finishBattle = (id) => api.post(`/battles/${id}/finish`).then((r) => r.data);
export const getHistory = () => api.get('/battles/history').then((r) => r.data);