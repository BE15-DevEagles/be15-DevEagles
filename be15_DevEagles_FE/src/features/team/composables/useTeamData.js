import { ref } from 'vue';

export function useTeamData() {
  // 팀원 데이터
  const teamMembers = ref([
    {
      id: 1,
      name: '김경록',
      isOnline: true,
      userThumbnail: null,
      workLog: ['오늘 할 일 완료', '새로운 기능 구현', '회의 참석'],
    },
    {
      id: 2,
      name: '박준석',
      isOnline: true,
      userThumbnail: null,
      workLog: ['API 개발', '데이터베이스 최적화', '버그 수정'],
    },
    {
      id: 3,
      name: '류현진',
      isOnline: false,
      userThumbnail: null,
      workLog: ['UI 디자인', '아이콘 제작', '프로토타입 테스트'],
    },
    {
      id: 4,
      name: '코디 폰스',
      isOnline: true,
      userThumbnail: null,
      workLog: ['일정 관리', '팀 미팅', '요구사항 분석'],
    },
    {
      id: 5,
      name: '이지수',
      isOnline: true,
      userThumbnail: null,
      workLog: ['서버 인프라 구축', '보안 테스트', 'DB 마이그레이션'],
    },
    {
      id: 6,
      name: '장성호',
      isOnline: false,
      userThumbnail: null,
      workLog: ['테스트 케이스 작성', '버그 리포트', '성능 테스트'],
    },
    {
      id: 7,
      name: '최민지',
      isOnline: true,
      userThumbnail: null,
      workLog: ['데이터 수집', '분석 리포트 작성', '대시보드 구현'],
    },
  ]);

  // 일지보기 기능
  function viewWorkLog(memberId) {
    const member = teamMembers.value.find(m => m.id === memberId);
    if (!member) return null;
    return member.workLog;
  }

  return {
    teamMembers,
    viewWorkLog,
  };
}
