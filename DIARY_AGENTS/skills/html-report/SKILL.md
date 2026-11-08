---
name: html-report
description: Diary KMP 프로젝트에서 사용자에게 여러 입력이나 trade-off 결정을 한꺼번에 받아야 할 때 사용한다. 결정 항목마다 RadioButton·CheckBox·여러 줄 TextField 입력, 선택지별 장단점과 예시·이미지·Mermaid 다이어그램, 기본값으로 미리 고른 추천안, 항목별 Skip을 갖춘 HTML 보고서를 만들어 브라우저로 열고, 사용자가 프롬프트 복사 버튼으로 답변을 클립보드에 복사해 채팅으로 전달하게 한다.
---

# HTML Report

## 사용 시점

사용자 결정이 필요한 항목이 여러 개이거나, 선택지마다 장단점을 비교해야 하는 trade-off가 있을 때 쓴다. 언제 멈추고 결정을 받을지는 [DIARY_AGENTS.md](../../../DIARY_AGENTS.md)의 `사용자 확인`과 각 스킬이 정하고, 이 스킬은 결정을 받는 형식만 소유한다.

결정 항목이 하나이고 선택지가 짧으면 [confirm.md](../../confirm.md) 형식의 채팅 보고로 충분하다.

## 절차

1. 결정 항목을 정리해 JSON 파일로 쓴다. 형식은 아래 `입력 JSON`을 따른다.
2. 렌더러로 HTML을 만든다. 파일은 세션 임시 디렉터리(Claude Code는 scratchpad, 없으면 `/tmp`)에 둔다.

   ```shell
   python3 DIARY_AGENTS/skills/html-report/scripts/render_report.py <input.json> <output.html> --open
   ```

3. 채팅에는 HTML 파일의 절대 경로와 항목 요약(항목 수, 각 항목 제목과 추천안)만 짧게 남기고, 사용자가 복사한 프롬프트를 붙여 넣을 때까지 해당 결정에 의존하는 작업을 진행하지 않는다.
4. 붙여 넣은 프롬프트를 받으면 선택한 방향만 반영한다. `건너뜀` 항목은 추정으로 메우지 않고 이번 작업 범위에서 제외하며, 그 항목에 막히는 작업이 있으면 무엇이 보류됐는지 보고한다.

## 항목 작성 기준

- 항목마다 왜 결정이 필요한지 `context`에 근거(문서 이름, 코드 위치, 충돌 내용)와 함께 적는다.
- 선택지마다 `pros`와 `cons`를 적는다. 한쪽만 채우지 않고, 실제 차이가 드러나도록 구체적으로 쓴다.
- 추천안은 `radio`·`checkbox` 항목에서 선택지의 `recommended: true`로, `text` 항목에서 `default`로 지정한다. 추천 근거는 `recommendReason`에 적는다. 추천안이 기본값으로 미리 선택된다.
- 결정할 근거가 부족해 추천할 수 없으면 추천안을 비워 두고 `context`에 그 이유를 적는다. 이 경우 사용자가 직접 고르거나 Skip해야 복사할 수 있다.
- 선택지를 하나만 고르는 결정은 `radio`, 여러 개를 함께 고를 수 있으면 `checkbox`, 정해진 선택지가 없는 입력(문구, 이름, 수치, 자유 의견)은 `text`를 쓴다.
- `radio`·`checkbox` 항목에는 기본으로 메모 입력란이 붙는다. 메모가 의미 없으면 `note: false`로 끈다.

### 예시와 그림

선택에 따라 결과가 어떻게 달라지는지 보여 줄 수 있으면 글로만 설명하지 않고 예시나 그림을 붙인다. 보고서 전체, 항목, 선택지 어디에나 붙일 수 있고, 선택지마다 같은 형식으로 붙여 나란히 비교되게 한다.

- `examples`: 화면 문구, API 시그니처·호출 코드, 데이터 형식, 프롬프트 출력처럼 텍스트로 보이는 차이. `language`에 `kotlin`, `json`, `text` 등을 적는다.
- `figures`의 `mermaid`: 화면 흐름, 상태 전이, 모듈 의존, 시퀀스, 데이터 흐름. Mermaid는 열 때 CDN에서 불러오고, 불러오지 못하면 원본 문법을 그대로 보여 준다.
- `figures`의 `svg`: Mermaid로 그리기 어려운 레이아웃 와이어프레임이나 간단한 도식. 안전을 위해 이미지로 그리므로 스크립트와 외부 리소스는 동작하지 않는다.
- `figures`의 `image`: 실제 화면 캡처, 디자인 문서의 이미지처럼 이미 있는 파일이나 이번에 직접 캡처한 파일. 로컬 경로는 렌더러가 HTML에 내장한다(5MB 이하, png·jpg·gif·webp·svg).
- 존재하지 않는 화면을 실제 캡처처럼 보이게 만들지 않는다. 가상의 화면은 `svg` 와이어프레임이나 `mermaid`로 그리고 `caption`에 예시임을 밝힌다.
- 그림은 클릭하면 크게 볼 수 있고, 선택지 안의 그림을 클릭해도 선택은 바뀌지 않는다.

## 입력 JSON

```json
{
  "title": "디데이 위젯 스펙 결정",
  "summary": "스펙 작성 중 확인이 필요한 항목입니다.",
  "items": [
    {
      "id": "Q1",
      "title": "지난 디데이 표시",
      "type": "radio",
      "context": "docs/spec/dday.md는 미래 날짜만 정의하고 지난 날짜 표시는 비어 있다.",
      "options": [
        {
          "id": "plus",
          "label": "D+N으로 표시",
          "description": "지난 날짜도 계속 보여 준다.",
          "pros": ["기념일 경과를 볼 수 있다"],
          "cons": ["목록이 계속 길어진다"],
          "recommended": true,
          "examples": [{ "title": "목록 표시", "language": "text", "content": "결혼기념일  D-12\n입사일      D+340" }]
        },
        {
          "id": "hide",
          "label": "숨김",
          "pros": ["목록이 짧게 유지된다"],
          "cons": ["지난 기록을 볼 방법이 없다"],
          "examples": [{ "title": "목록 표시", "language": "text", "content": "결혼기념일  D-12" }]
        }
      ],
      "figures": [
        { "kind": "mermaid", "content": "flowchart LR\n  A[디데이 날짜] --> B{오늘 이후?}\n  B -- 예 --> C[D-N]\n  B -- 아니오 --> D[Q1 결정]", "caption": "표시 분기" },
        { "kind": "image", "src": "screenshots/dday-list.png", "caption": "현재 목록 화면" }
      ],
      "recommendReason": "기존 캘린더 화면도 지난 일정을 숨기지 않는다."
    },
    {
      "id": "Q2",
      "title": "빈 상태 문구",
      "type": "text",
      "context": "디자인 문서에 빈 상태 문구가 없다.",
      "default": "아직 디데이가 없어요",
      "placeholder": "문구를 입력하세요"
    }
  ]
}
```

| 필드 | 필수 | 설명 |
| --- | --- | --- |
| `title`, `summary` | `title`만 | 보고서 제목과 머리말 |
| `items[].id` | O | 프롬프트에 남는 안정적인 식별자. 보고서 안에서 중복되지 않는다. |
| `items[].type` | O | `radio`, `checkbox`, `text` |
| `items[].title`, `context` | `title`만 | 항목 제목과 결정이 필요한 이유 |
| `items[].options[]` | `radio`·`checkbox` | `id`, `label` 필수, `description`, `pros`, `cons`, `recommended` 선택 |
| `items[].recommendReason` | | 추천 근거 |
| `items[].default`, `placeholder` | | `text` 항목의 추천 기본값과 안내 문구 |
| `items[].note` | | `radio`·`checkbox` 메모 입력란 여부. 기본 `true` |
| `examples[]` | | 보고서·항목·선택지에 붙이는 텍스트 예시. `content` 필수, `title`, `language` 선택 |
| `figures[]` | | 보고서·항목·선택지에 붙이는 그림. `kind`(`image`, `mermaid`, `svg`) 필수, `image`는 `src`(로컬 경로는 JSON 파일 기준 상대 경로 또는 절대 경로, URL), 나머지는 `content` 필수, `caption` 선택 |

`radio` 항목에는 `recommended: true`가 최대 하나다. 렌더러가 형식을 검사하고 어긋나면 HTML을 만들지 않고 이유를 출력한다.

## 보고서 동작

- 항목마다 `건너뛰기`를 켜면 입력이 비활성화되고 프롬프트에 `건너뜀`으로 남는다.
- 화면 하단에 고정된 바가 결정·건너뜀·미응답 수와 `프롬프트 복사` 버튼을 보여 준다. 맨 아래에는 같은 버튼과 프롬프트 미리보기가 있다.
- 미응답 항목(선택 없음, 빈 텍스트)이 있으면 복사하지 않고 첫 미응답 항목으로 이동해 강조한다.
- 복사는 Clipboard API를 쓰고, 실패하면 `execCommand('copy')`로 대체한다. 둘 다 실패하면 미리보기 textarea를 선택해 사용자가 직접 복사하게 한다.
- 복사되는 프롬프트 형식은 다음과 같다.

  ```text
  [디데이 위젯 스펙 결정] 결정 사항을 전달합니다.

  1. Q1 지난 디데이 표시
     - 선택: D+N으로 표시 (추천안)
     - 메모: 목록 정렬은 가까운 순으로
  2. Q2 빈 상태 문구
     - 입력:
       아직 디데이가 없어요
  3. Q3 위젯 크기
     - 건너뜀
  ```
