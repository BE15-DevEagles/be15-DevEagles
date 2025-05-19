export default {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      [
        'feat', // 새로운 기능
        'fix', // 버그 수정
        'hotfix', // 긴급 버그 수정
        'docs', // 문서 변경
        'style', // 코드 스타일 변경 (포맷팅, 세미콜론 누락 등)
        'refactor', // 코드 리팩토링
        'test', // 테스트 코드 추가/수정
        'chore', // 빌드 프로세스나 도구 변경, 종속성 업데이트
        'perf', // 성능 개선
        'ci', // CI 설정 변경
        'build', // 빌드 시스템이나 외부 종속성 변경
        'revert', // 이전 커밋으로 되돌리기
      ],
    ],
    'type-case': [2, 'always', 'lower'],
    'subject-case': [2, 'always', 'sentence-case'],
    'subject-empty': [2, 'never'],
    'subject-full-stop': [2, 'never', '.'],
    'body-leading-blank': [2, 'always'],
    'body-max-line-length': [2, 'always', 100],
    'footer-leading-blank': [2, 'always'],
  },
};
