import api from '@/api/axios.js';

export const fetchMyWorklogs = (url, searchRequest) => {
  return api.post(url, searchRequest);
};

export const searchWorklogs = params => {
  return api.post('/search/info', params);
};

export const generateSummary = request => {
  return api.post('/summary', request);
};
