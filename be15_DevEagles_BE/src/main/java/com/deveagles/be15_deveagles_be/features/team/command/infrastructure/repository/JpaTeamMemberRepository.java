package com.deveagles.be15_deveagles_be.features.team.command.infrastructure.repository;

import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMember;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.TeamMemberId;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTeamMemberRepository
    extends JpaRepository<TeamMember, TeamMemberId>, TeamMemberRepository {}
