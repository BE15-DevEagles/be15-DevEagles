package com.deveagles.be15_deveagles_be.features.team.command.application.service.impl;

import com.deveagles.be15_deveagles_be.features.team.command.application.dto.request.CreateTeamRequest;
import com.deveagles.be15_deveagles_be.features.team.command.application.dto.response.CreateTeamResponse;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.TeamCommandService;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMemberId;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamErrorCode;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import com.deveagles.be15_deveagles_be.features.user.command.domain.aggregate.User;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamCommandServiceImpl implements TeamCommandService {

  private final TeamRepository teamRepository;
  private final UserRepository userRepository;
  private final TeamMemberRepository teamMemberRepository;

  @Override
  @Transactional
  public CreateTeamResponse createTeam(Long userId, CreateTeamRequest request) {
    // 1. 팀 이름 중복 검증
    if (teamRepository.existsByTeamName(request.getTeamName())) {
      throw new TeamBusinessException(TeamErrorCode.TEAM_NAME_DUPLICATION);
    }

    // 2. 팀 생성
    Team team =
        Team.builder()
            .userId(userId)
            .teamName(request.getTeamName())
            .introduction(request.getIntroduction())
            .build();

    Team saved = teamRepository.save(team);

    // 3. 팀장을 팀원으로 등록
    User teamLeader =
        userRepository
            .findById(userId)
            .orElseThrow(
                () ->
                    new TeamBusinessException(
                        TeamErrorCode.USER_NOT_FOUND)); // 보통 존재한다고 가정되지만 안정성 위해 체크

    TeamMember teamMember =
        TeamMember.builder()
            .id(new TeamMemberId(userId, saved.getTeamId()))
            .user(teamLeader)
            .team(saved)
            .build();

    teamMemberRepository.save(teamMember);

    // 4. 응답 반환
    return CreateTeamResponse.builder()
        .teamId(saved.getTeamId())
        .teamName(saved.getTeamName())
        .introduction(saved.getIntroduction())
        .build();
  }

  @Override
  @Transactional
  public void inviteTeamMember(Long inviterId, Long teamId, String email) {
    // 1. 팀 존재 여부 확인
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));

    // 2. 초대한 사람이 팀장인지 검증
    if (!team.getUserId().equals(inviterId)) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_LEADER);
    }

    // 3. 이메일로 유저 조회
    User invitee =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.USER_NOT_FOUND));

    // 4. 이미 팀원인지 확인
    if (teamMemberRepository.existsByTeamTeamIdAndUserUserId(teamId, invitee.getUserId())) {
      throw new TeamBusinessException(TeamErrorCode.ALREADY_TEAM_MEMBER);
    }

    // 5. 팀원 등록
    TeamMember newMember =
        TeamMember.builder()
            .id(new TeamMemberId(invitee.getUserId(), teamId))
            .user(invitee)
            .team(team)
            .build();

    teamMemberRepository.save(newMember);
  }

  @Override
  @Transactional
  public void fireTeamMember(Long userId, Long teamId, String email) {
    // 1. 팀 조회
    Team team =
        teamRepository
            .findById(teamId)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.TEAM_NOT_FOUND));

    // 2. 팀장 권한 확인
    if (!team.getUserId().equals(userId)) {
      throw new TeamBusinessException(TeamErrorCode.NOT_TEAM_LEADER);
    }

    // 3. 추방 대상 유저 조회
    User target =
        userRepository
            .findUserByEmail(email)
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.USER_NOT_FOUND));

    // 3-1. 팀장이 본인을 추방하려는 경우 예외
    if (target.getUserId().equals(userId)) {
      throw new TeamBusinessException(TeamErrorCode.CANNOT_FIRE_SELF);
    }

    // 4. 팀원 엔티티 조회
    TeamMember teamMember =
        teamMemberRepository
            .findByTeamTeamIdAndUserUserId(teamId, target.getUserId())
            .orElseThrow(() -> new TeamBusinessException(TeamErrorCode.NOT_TEAM_MEMBER));

    // 5. 이미 삭제된 경우 체크
    if (teamMember.isDeleted()) {
      throw new TeamBusinessException(TeamErrorCode.ALREADY_DELETED_MEMBER);
    }

    // 6. soft delete 처리
    teamMember.softDelete();
  }
}
