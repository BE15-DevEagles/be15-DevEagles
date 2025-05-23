import api from '@/api/axios'; // 토큰 및 리프레시 처리 포함된 axios 인스턴스 사용

export const createTeam = async ({ teamName, description }) => {
  const response = await api.post('/teams', {
    teamName,
    introduction: description, // 백엔드 요청 필드에 맞춤
  });

  return response.data.data; // ApiResponse에서 data만 반환
};
