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

// 4. 팀원 추방
export const fireTeamMember = async (teamId, email) => {
  const res = await api.post(`/team/members/${teamId}/fire`, { email });
  return res.data.data; // "팀원 추방이 완료되었습니다."
};

// 5. 팀장 권한 양도
export const transferTeamLeader = async (teamId, email) => {
  const res = await api.patch(`/team/members/${teamId}/transfer`, { email });
  return res.data.data; // "팀장 권한이 성공적으로 양도되었습니다."
};

// 6. 팀 탈퇴
export const withdrawTeam = async teamId => {
  const res = await api.post(`/team/members/withdraw`, { teamId });
  return res.data.data; // "팀 탈퇴가 완료되었습니다."
};

// 7. 팀 삭제
export const deleteTeam = async teamId => {
  const res = await api.delete(`/teams/${teamId}`);
  return res.data.data; // "팀 삭제가 완료되었습니다."
};

// 8. 팀 썸네일 변경
export const updateTeamThumbnail = async (teamId, file) => {
  const formData = new FormData();
  formData.append('file', file);

  const res = await api.post(`/teams/teams/${teamId}/thumbnail`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });

  return res.data.data; // 썸네일 URL 혹은 성공 메시지
};

// 9. 팀 상세 조회
export const getTeamDetail = async teamId => {
  const res = await api.get(`/teams/teams/${teamId}`);
  return res.data.data;
};
