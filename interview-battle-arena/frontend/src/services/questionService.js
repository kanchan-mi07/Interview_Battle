import api from './api.js';

export const getPracticeQuestions = (params) =>
  api.get('/questions', { params }).then((r) => r.data);

export const getQuestion = (id) => api.get(`/questions/${id}`).then((r) => r.data);

export const submitPractice = (answers) =>
  api.post('/questions/practice/submit', { answers }).then((r) => r.data);