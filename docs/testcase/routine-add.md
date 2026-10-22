# RoutineAdd 테스트 케이스

기준 스펙: [RoutineAdd 화면 스펙](../spec/client/routine-add.md)

RoutineAdd로 진입하는 케이스는 [RoutineHome 테스트 케이스](./routine-home.md)에서, 제목 입력과 설명 입력 컴포넌트 자체의 케이스는 [설명 입력 컴포넌트 테스트 케이스](./description-input.md)에서, 목록과 함께 표시할 때의 케이스는 [Routine 목록·상세 배치 테스트 케이스](./routine-list-detail.md)에서 다룬다.

제목 입력이 한 줄 분량을 받는 것은 여러 화면이 함께 쓰는 [제목 입력 테스트 케이스](./title-input.md)가 다룬다.

## feature

### TC-edbcdc9b-03b0-4a1e-99d2-edba55ad2074: 화면에 처음 진입하면 제목과 설명이 비어 있다

- 근거: `feature > 진입과 입력`
- Given: 사용자가 루틴 추가를 선택했다.
- When: RoutineAdd 화면이 처음 표시된다.
- Then: 제목과 설명이 모두 비어 있다.

### TC-6a87a917-4e61-4b0e-a4a1-df18f580ae6c: 화면에 처음 진입하면 제목 입력에 초점이 있다

- 근거: `feature > 진입과 입력`
- Given: 사용자가 루틴 추가를 선택했다.
- When: RoutineAdd 화면이 처음 표시된다.
- Then: 입력 초점이 제목 입력에 있다.

### TC-c184ab41-d30d-4136-8434-2a5d1b8fd176: 입력한 제목과 설명을 화면에 유지한다

- 근거: `feature > 진입과 입력`
- Given: RoutineAdd 화면이 표시되어 있다.
- When: 사용자가 제목과 설명을 입력한다.
- Then: 입력한 제목과 설명이 각 입력에 그대로 표시된다.

### TC-8a81e75d-a18f-4b36-8f91-81c9c9ef26bc: 추가를 실행해도 입력 내용과 화면 상태가 바뀌지 않는다

- 근거: `feature > 루틴 추가`
- Given: 제목과 설명을 입력한 RoutineAdd 화면이 표시되어 있다.
- When: 사용자가 추가를 실행한다.
- Then: 제목과 설명이 그대로 남고 성공·실패 안내가 표시되지 않으며 다른 화면으로 이동하지 않는다.

### TC-1a4c0e5b-a267-4f3a-98fe-4a6ea261e3c4: 화면이 재생성되어도 입력 중이던 내용을 유지한다

- 근거: `feature > 작성 상태 유지`
- Given: 제목과 설명을 입력한 RoutineAdd 화면이 표시되어 있다.
- When: 화면이 재생성된다.
- Then: 입력 중이던 제목과 설명이 그대로 표시된다.

### TC-84d2562f-324f-4d75-ad50-6c3362f8c240: 메모리 정리 뒤 복원해도 작성 중이던 내용을 모두 복원한다

- 근거: `feature > 작성 상태 유지`
- Given: RoutineAdd 화면에서 제목과 설명을 입력했다.
- When: 시스템이 앱을 메모리에서 정리한 뒤 화면을 복원한다.
- Then: 입력한 제목과 설명이 그대로 표시된다.

### TC-b910633a-ad71-4a5e-828a-c292a0f41cbc: 앱을 다시 실행해 진입하면 새 입력 상태로 시작한다

- 근거: `feature > 작성 상태 유지`
- Given: 이전 실행에서 RoutineAdd 화면에 제목과 설명을 입력한 뒤 앱을 종료했다.
- When: 앱을 다시 실행해 RoutineAdd 화면에 진입한다.
- Then: 제목과 설명이 모두 비어 있다.

### TC-f731c7b3-d20c-4718-ae02-b9287ed7d17a: 뒤로가면 이전 화면으로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: RoutineAdd 화면이 단독으로 표시되어 있다.
- When: 사용자가 뒤로가기를 실행한다.
- Then: RoutineAdd 화면에 진입하기 전 화면으로 돌아간다.
