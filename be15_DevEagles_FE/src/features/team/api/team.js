import api from '@/api/axios'; // 토큰 및 리프레시 처리 포함된 axios 인스턴스 사용

// 1. 팀 생성
export const createTeam = async ({ teamName, description }) => {
  const response = await api.post('/teams', {
    teamName,
    introduction: description, // 백엔드 요청 필드에 맞춤
  });

  return response.data.data; // ApiResponse에서 data만 반환
};

// 2. 팀원 목록 조회
export const getTeamMembers = async teamId => {
  const response = await api.get(`/teams/${teamId}/members`);
  return response.data.data; // ApiResponse 래핑 구조에 맞춤
};

// 3. 팀원 초대
export const inviteTeamMember = async (teamId, email) => {
  const res = await api.post(`/team/members/${teamId}/invite`, { email });
  return res.data.data; // "팀원 초대가 완료되었습니다."
};
