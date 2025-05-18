package com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class TeamMemberId implements Serializable {

  @Column(name = "user_id")
  private Long userId;

  @Column(name = "team_id")
  private Long teamId;
}
