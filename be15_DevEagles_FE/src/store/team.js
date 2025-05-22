import { defineStore } from 'pinia';
import api from '@/api/axios';

export const useTeamStore = defineStore('team', {
  state: () => ({
    currentTeam: null,
    teams: [],
    teamMembers: [],
    loading: false,
    error: null,
  }),

  getters: {
    currentTeamId: state => state.currentTeam?.teamId,
    teamChannels: state => state.currentTeam?.channels || [],
  },

  actions: {
    // 팀 목록 로드
    async fetchTeams() {
      this.loading = true;
      try {
        const response = await api.get('teams/my');
        this.teams = response.data.data;

        const lastSelectedTeam = localStorage.getItem('lastSelectedTeam');
        const teamToSelect =
          lastSelectedTeam && this.teams.find(team => team.teamId === Number(lastSelectedTeam))
            ? Number(lastSelectedTeam)
            : this.teams[0]?.teamId;

        if (this.teams.length > 0 && teamToSelect) {
          await this.setCurrentTeam(teamToSelect); // ✅ 항상 set 호출
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
      this.loading = true;
      try {
        // 팀 상세 정보 조회
        const teamRes = await api.get(`teams/teams/${teamId}`);
        this.currentTeam = teamRes.data.data;

        // 팀 멤버 목록 조회
        const memberRes = await api.get(`teams/${teamId}/members`);
        this.teamMembers = Array.isArray(memberRes.data.data) ? memberRes.data.data : [];

        console.log('📦 멤버 API 응답:', memberRes.data.data);

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
