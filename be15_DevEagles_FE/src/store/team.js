import { defineStore } from 'pinia';
import api from '@/api/axios';

export const useTeamStore = defineStore('team', {
  state: () => ({
    currentTeam: null,
    teams: [],
    loading: false,
    error: null,
  }),

  getters: {
    currentTeamId: state => state.currentTeam?.id,
    teamMembers: state => state.currentTeam?.members || [],
    teamChannels: state => state.currentTeam?.channels || [],
  },

  actions: {
    // 팀 목록 로드
    async fetchTeams() {
      this.loading = true;
      try {
        // 실제 API 구현 전까지는 테스트 데이터 사용
        // const response = await api.get('/api/v1/teams');
        // this.teams = response.data.data;

        // 테스트 데이터
        this.teams = [
          {
            id: 'team1',
            name: 'DevEagles',
            thumbnail: null,
            description: '비욘드 SW캠프 3분기 DevEagles 팀',
          },
          {
            id: 'team2',
            name: '코드봉인',
            thumbnail: null,
            description: '코드봉인 프로젝트 팀',
          },
          {
            id: 'team3',
            name: '알파코더',
            thumbnail: null,
            description: '알파코더 스터디 그룹',
          },
        ];

        // 로컬 스토리지에서 마지막 선택 팀 확인
        const lastSelectedTeam = localStorage.getItem('lastSelectedTeam');
        const teamToSelect =
          lastSelectedTeam && this.teams.find(team => team.id === lastSelectedTeam)
            ? lastSelectedTeam
            : this.teams[0]?.id;

        // 팀이 있는데 currentTeam이 없으면 첫 번째 팀을 선택
        if (this.teams.length > 0 && !this.currentTeam && teamToSelect) {
          this.setCurrentTeam(teamToSelect);
        }
      } catch (err) {
        this.error = err.message;
        console.error('팀 목록 로드 실패:', err);
      } finally {
        this.loading = false;
      }
    },

    // 현재 팀 설정
    async setCurrentTeam(teamId) {
      if (this.currentTeamId === teamId) return;

      this.loading = true;
      try {
        // 실제 API 구현 전까지는 메모리에서 찾기
        // const response = await api.get(`/api/v1/teams/${teamId}`);
        // this.currentTeam = response.data.data;

        // 메모리에서 팀 찾기
        const team = this.teams.find(t => t.id === teamId);
        if (!team) {
          throw new Error('팀을 찾을 수 없습니다.');
        }

        // 테스트 데이터로 팀 멤버 추가
        const teamWithMembers = {
          ...team,
          members: [
            { id: 'user1', name: '김코딩', position: '팀장', thumbnail: null, isOnline: true },
            { id: 'user2', name: '이해커', position: '개발자', thumbnail: null, isOnline: false },
            { id: 'user3', name: '박알고', position: '디자이너', thumbnail: null, isOnline: true },
            { id: 'user4', name: '최데브', position: '기획자', thumbnail: null, isOnline: false },
          ],
        };

        this.currentTeam = teamWithMembers;

        // 브라우저 스토리지에 최근 선택 팀 저장
        localStorage.setItem('lastSelectedTeam', teamId);
      } catch (err) {
        this.error = err.message;
        console.error('팀 설정 실패:', err);
      } finally {
        this.loading = false;
      }
    },
  },
});
