import axios from '@/api/axios';

// 타임캡슐 생성 API
export function createTimecapsule({ timecapsuleContent, openDate, teamId }) {
  return axios.post('/api/v1/timecapsules', {
    timecapsuleContent,
    openDate,
    teamId,
  });
}
