package com.deveagles.be15_deveagles_be.features.worklog.command.application.service;

import com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception.WorklogBusinessException;
import com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception.WorklogErrorCode;

public class GeneratorBuilder {
  private static final int MAX_LENGTH = 40;

  public static String ContentsPrompt(String workContent, String note) {
    validateContents(workContent, note);
    String prefixPrompt = buildPrefixPrompt();
    String originPrompt = buildOriginPrompt(workContent.trim(), note.trim());
    String guide =
        String.join(
            "\n", "[제목 예시] 로그인 기능 개발 및 예외 처리 개선을 통한 인증 시스템 안정화", "위 예시와 유사하게 한 문장 제목을 작성하세요.");

    String prompt = prefixPrompt + "\n\n" + originPrompt + "\n\n" + guide;

    return prompt;
  }

  private static void validateContents(String workContent, String note) {
    if (workContent == null
        || workContent.trim().isEmpty()
        || note == null
        || note.trim().isEmpty()) {
      throw new WorklogBusinessException(WorklogErrorCode.INVALID_WORKLOG_INPUT);
    }
  }

  private static String buildOriginPrompt(String workContent, String note) {
    return String.format("[업무 내용]\n%s\n\n[특이사항]\n%s", workContent, note);
  }

  private static String buildPrefixPrompt() {
    return String.format(
        "당신은 업무일지에 대학 요약을 30년동안 한 전문가입니다.\n"
            + "다음 4가지 기준을 바탕으로 내용을 요약해주세요\n"
            + "1. 업무 내용과 특이사항을 바탕으로 요약해주세요\n"
            + "2. 최대 %d자 이내로 간결하게 작성해주세요\n"
            + "3. 제목 형태로 자연스럽게 작성해주세요\n"
            + "4. 불필요한 수식어 제외",
        MAX_LENGTH);
  }
}
