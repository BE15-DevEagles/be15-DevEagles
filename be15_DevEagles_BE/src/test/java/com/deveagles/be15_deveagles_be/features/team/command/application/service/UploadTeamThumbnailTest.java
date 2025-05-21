package com.deveagles.be15_deveagles_be.features.team.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.deveagles.be15_deveagles_be.features.team.command.application.service.impl.TeamCommandServiceImpl;
import com.deveagles.be15_deveagles_be.features.team.command.domain.aggregate.Team;
import com.deveagles.be15_deveagles_be.features.team.command.domain.exception.TeamBusinessException;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamMemberRepository;
import com.deveagles.be15_deveagles_be.features.team.command.domain.repository.TeamRepository;
import com.deveagles.be15_deveagles_be.features.user.command.repository.UserRepository;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class UploadTeamThumbnailTest {

  private TeamCommandServiceImpl teamCommandService;

  private TeamRepository teamRepository;
  private UserRepository userRepository;
  private TeamMemberRepository teamMemberRepository;
  private AmazonS3 amazonS3;

  private final String bucket = "test-bucket";

  @BeforeEach
  void setUp() throws Exception {
    teamRepository = mock(TeamRepository.class);
    userRepository = mock(UserRepository.class);
    teamMemberRepository = mock(TeamMemberRepository.class);
    amazonS3 = mock(AmazonS3.class);

    teamCommandService =
        new TeamCommandServiceImpl(teamRepository, userRepository, teamMemberRepository, amazonS3);

    // @Value 필드 리플렉션으로 주입
    Field bucketField = TeamCommandServiceImpl.class.getDeclaredField("bucket");
    bucketField.setAccessible(true);
    bucketField.set(teamCommandService, bucket);
  }

  @Test
  @DisplayName("성공: 팀장이 썸네일 업로드")
  void testUploadTeamThumbnail_success() throws IOException {
    // given
    Long userId = 1L;
    Long teamId = 100L;
    String filename = "logo.png";
    String expectedUrl = "https://s3.amazonaws.com/test-bucket/team/" + filename;

    Team team =
        Team.builder()
            .teamId(teamId)
            .userId(userId)
            .teamName("Dev Team")
            .introduction("test intro")
            .build();

    MockMultipartFile file =
        new MockMultipartFile("file", filename, "image/png", "dummy image".getBytes());

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));
    when(amazonS3.putObject(anyString(), anyString(), any(), any(ObjectMetadata.class)))
        .thenReturn(new PutObjectResult());
    when(amazonS3.getUrl(eq(bucket), anyString())).thenReturn(new URL(expectedUrl));

    // when
    String result = teamCommandService.uploadTeamThumbnail(userId, teamId, file);

    // then
    assertThat(result).isEqualTo(expectedUrl);
    assertThat(team.getUrl()).isEqualTo(expectedUrl);

    verify(amazonS3).putObject(anyString(), anyString(), any(), any(ObjectMetadata.class));
    verify(amazonS3).getUrl(eq(bucket), anyString());
  }

  @Test
  @DisplayName("실패: 팀이 존재하지 않음")
  void testUploadTeamThumbnail_teamNotFound() {
    // given
    Long userId = 1L;
    Long teamId = 999L;
    MockMultipartFile file =
        new MockMultipartFile("file", "logo.png", "image/png", "dummy".getBytes());

    when(teamRepository.findById(teamId)).thenReturn(Optional.empty());

    // then
    assertThrows(
        TeamBusinessException.class,
        () -> teamCommandService.uploadTeamThumbnail(userId, teamId, file));
  }

  @Test
  @DisplayName("실패: 팀장이 아님")
  void testUploadTeamThumbnail_notLeader() {
    // given
    Long userId = 2L;
    Long teamId = 100L;

    Team team =
        Team.builder()
            .teamId(teamId)
            .userId(1L) // 다른 사람
            .teamName("Other")
            .introduction("intro")
            .build();

    MockMultipartFile file =
        new MockMultipartFile("file", "logo.png", "image/png", "dummy".getBytes());

    when(teamRepository.findById(teamId)).thenReturn(Optional.of(team));

    // then
    assertThrows(
        TeamBusinessException.class,
        () -> teamCommandService.uploadTeamThumbnail(userId, teamId, file));
  }
}
