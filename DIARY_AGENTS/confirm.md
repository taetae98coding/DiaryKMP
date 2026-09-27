# 충돌·공백 보고

작업 도중 사용자 결정이 필요한 충돌이나 공백을 발견했을 때 쓰는 보고 형식이다. 언제 멈춰야 하는지는 [DIARY_AGENTS.md](../DIARY_AGENTS.md)의 `사용자 확인`과 각 스킬 문서가 정하고, 이 문서는 형식만 소유한다.

추정으로 메우거나 양쪽을 절충하지 않고 다음 형식으로 확인한 뒤, 사용자가 선택한 방향만 반영한다.

```markdown
<충돌 | 공백>이 있습니다.

1. 요약
   - <근거 A>: ...
   - <근거 B>: ...
   - 왜 문제인지: ...

선택이 필요합니다.
- 선택지 1: ...
- 선택지 2: ...
```

`근거` 자리에는 비교한 대상을 그대로 적는다. 문서를 다루는 wave는 문서 이름을, `rebase-wave`는 브랜치 이름을 쓴다.

이 형식은 보고에만 쓰고 산출 문서나 커밋 메시지에 남기지 않는다.

## 이 문서를 참조하는 문서

- [DIARY_AGENTS.md](../DIARY_AGENTS.md)
- [wave.md](wave.md)
- `skills/spec-wave`, `skills/design-wave`, `skills/testcase-wave`, `skills/testcode-wave`
- `skills/rebase-wave`, `skills/verify-wave`
