/**
 * 채팅 시간 관련 유틸리티 함수들
 * 중복된 시간 포맷팅 로직을 중앙화
 */

// 한국 시간대 상수
const KOREA_TIMEZONE = 'Asia/Seoul';

/**
 * 백엔드 시간 오프셋 문제를 감지하고 보정
 * 백엔드에서 한국 시간을 UTC로 잘못 표시하는 경우 자동 보정
 */
function detectAndFixTimezone(timestamp) {
  const date = new Date(timestamp);
  const now = new Date();

  // 메시지 시간이 현재 시간보다 9시간 이상 미래인 경우 (백엔드 오프셋 문제 의심)
  const timeDiff = date.getTime() - now.getTime();
  const ninehours = 9 * 60 * 60 * 1000; // 9시간을 밀리초로

  if (timeDiff > ninehours - 30 * 60 * 1000 && timeDiff < ninehours + 30 * 60 * 1000) {
    // 9시간 차이(±30분 오차 허용)가 나면 백엔드에서 한국시간을 UTC로 잘못 표시한 것으로 판단
    const correctedTime = new Date(date.getTime() - ninehours);
    console.warn('[timeUtils] 백엔드 시간 오프셋 문제 감지 및 보정:', {
      원본: timestamp,
      보정전: date.toISOString(),
      보정후: correctedTime.toISOString(),
      시간차이: `${Math.round(timeDiff / (60 * 60 * 1000))}시간`,
    });
    return correctedTime;
  }

  return date;
}

export function formatMessageTime(timestamp) {
  if (!timestamp) return '';

  try {
    // 시간 오프셋 문제 보정
    const date = detectAndFixTimezone(timestamp);

    if (isNaN(date.getTime())) {
      console.warn('[timeUtils] 유효하지 않은 날짜:', timestamp);
      return '';
    }

    const now = new Date();

    // 디버깅용 로그 (일시적) - 문제 해결 후 제거 예정
    // console.log('[timeUtils] formatMessageTime 디버깅:', {
    //   원본타임스탬프: timestamp,
    //   보정된Date: date.toISOString(),
    //   현재시간: now.toISOString(),
    //   한국시간으로변환: date.toLocaleString('ko-KR', { timeZone: KOREA_TIMEZONE }),
    //   현재한국시간: now.toLocaleString('ko-KR', { timeZone: KOREA_TIMEZONE }),
    // });

    // 한국 시간대로 날짜 비교 - 더 정확한 방법 사용
    const messageKoreaDate = new Date(date.toLocaleString('en-CA', { timeZone: KOREA_TIMEZONE }));
    const todayKoreaDate = new Date(now.toLocaleString('en-CA', { timeZone: KOREA_TIMEZONE }));

    const isToday = messageKoreaDate.toDateString() === todayKoreaDate.toDateString();

    if (isToday) {
      return date.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
        timeZone: KOREA_TIMEZONE,
      });
    } else {
      const timeStr = date.toLocaleTimeString('ko-KR', {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
        timeZone: KOREA_TIMEZONE,
      });

      // 날짜 표시 - 한국 시간대 기준
      const koreaDate = new Date(date.toLocaleString('en-US', { timeZone: KOREA_TIMEZONE }));
      return `${koreaDate.getMonth() + 1}월 ${koreaDate.getDate()}일 ${timeStr}`;
    }
  } catch (error) {
    console.error('[timeUtils] 시간 포맷 오류:', error);
    return '';
  }
}

export function formatDate(timestamp) {
  if (!timestamp) return '';

  try {
    // 시간 오프셋 문제 보정
    const date = detectAndFixTimezone(timestamp);
    if (isNaN(date.getTime())) return '';

    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    // 한국 시간대로 날짜 비교 - 더 정확한 방법
    const messageKoreaDate = new Date(date.toLocaleString('en-CA', { timeZone: KOREA_TIMEZONE }));
    const todayKoreaDate = new Date(today.toLocaleString('en-CA', { timeZone: KOREA_TIMEZONE }));
    const yesterdayKoreaDate = new Date(
      yesterday.toLocaleString('en-CA', { timeZone: KOREA_TIMEZONE })
    );

    if (messageKoreaDate.toDateString() === todayKoreaDate.toDateString()) {
      return '오늘';
    }

    if (messageKoreaDate.toDateString() === yesterdayKoreaDate.toDateString()) {
      return '어제';
    }

    // 한국 시간대 기준으로 날짜 포맷
    const koreaDate = new Date(date.toLocaleString('en-US', { timeZone: KOREA_TIMEZONE }));
    return `${koreaDate.getFullYear()}.${(koreaDate.getMonth() + 1)
      .toString()
      .padStart(2, '0')}.${koreaDate.getDate().toString().padStart(2, '0')}`;
  } catch (error) {
    console.error('[timeUtils] 날짜 포맷 오류:', error);
    return '';
  }
}

export function formatLastMessageTime(timestamp) {
  if (!timestamp) return '';

  try {
    const now = new Date();
    const msgTime = new Date(timestamp);
    const diffMs = now - msgTime;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMins / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffMins < 1) return '방금';
    if (diffMins < 60) return `${diffMins}분 전`;
    if (diffHours < 24) return `${diffHours}시간 전`;
    if (diffDays < 7) return `${diffDays}일 전`;

    return msgTime.toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric',
      timeZone: KOREA_TIMEZONE,
    });
  } catch (error) {
    console.error('[timeUtils] 마지막 메시지 시간 포맷 오류:', error);
    return '';
  }
}

export function isValidTimestamp(timestamp) {
  if (!timestamp) return false;
  const date = new Date(timestamp);
  return !isNaN(date.getTime());
}

export function normalizeTimestamp(timestamp) {
  if (!timestamp) {
    // 한국 시간 기준으로 현재 시간 생성 - 더 정확한 방법
    const now = new Date();
    const koreaTime = new Date(now.toLocaleString('en-US', { timeZone: KOREA_TIMEZONE }));
    const result = koreaTime.toISOString();

    console.log('[timeUtils] 빈 타임스탬프를 한국 현재 시간으로 설정:', {
      original: timestamp,
      koreaTime: result,
      localTime: now.toISOString(),
    });

    return result;
  }

  const date = new Date(timestamp);
  if (isNaN(date.getTime())) {
    console.warn('[timeUtils] 유효하지 않은 timestamp 정규화:', timestamp);
    // 유효하지 않은 경우 한국 현재 시간으로 대체
    const now = new Date();
    const koreaTime = new Date(now.toLocaleString('en-US', { timeZone: KOREA_TIMEZONE }));
    return koreaTime.toISOString();
  }

  return date.toISOString();
}

export function compareTimestamps(a, b) {
  const timeA = new Date(a).getTime();
  const timeB = new Date(b).getTime();
  return timeA - timeB;
}
