# SettingDownload 테스트 케이스

기준 스펙: [SettingDownload 화면 스펙](../spec/client/setting-download.md)

설정 화면의 `다운로드` 항목을 선택해 SettingDownload로 이동하는 케이스는 [SettingHome 테스트 케이스](./setting-home.md)에서, 설정 목록과 함께 표시되는 상태의 시스템 뒤로가기 결과는 [Setting 목록·상세 배치 테스트 케이스](./setting-list-detail.md)에서 다룬다. 주소 후보와 프록시 상태가 정해지는 규칙은 [곡 다운로드 프록시 테스트 케이스](./music-download-proxy.md)에서 다루며, 이 문서는 정해진 상태를 화면이 어떻게 제공하는지만 다룬다.

## feature

### TC-SETTING-DOWNLOAD-FEATURE-001: 프록시가 제공 중이면 주소 후보를 모두 표시한다

- 근거: `feature > 프록시 주소 확인 (데스크톱 앱)`
- Given: 데스크톱 앱에서 프록시가 제공 중이고 주소 후보가 둘이다.
- When: 사용자가 SettingDownload 화면에 진입한다.
- Then: 두 주소 후보가 각각 전체 주소 그대로 표시되고, 주소를 입력하거나 바꾸는 수단은 표시되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-002: 닿을 주소가 없으면 그 사실을 알린다

- 근거: `feature > 프록시 주소 확인 (데스크톱 앱)`
- Given: 데스크톱 앱에서 프록시가 제공 중이고 주소 후보가 하나도 없다.
- When: 사용자가 SettingDownload 화면에 진입한다.
- Then: 다른 기기가 닿을 주소가 없음을 알리는 안내가 표시되고 어떤 주소도 표시되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-003: 프록시를 제공하지 못하면 시작하지 못했음을 알린다

- 근거: `feature > 프록시 주소 확인 (데스크톱 앱)`
- Given: 데스크톱 앱에서 프록시가 제공하지 못함 상태다.
- When: 사용자가 SettingDownload 화면에 진입한다.
- Then: 프록시를 시작하지 못했음과 앱을 다시 실행해야 함을 알리는 안내가 표시되고, 다시 시도하는 수단은 표시되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-004: 프록시 상태를 확인하기 전에는 본문을 제공하지 않는다

- 근거: `feature > 프록시 주소 확인 (데스크톱 앱)`, `feature > 프록시 주소 입력 (Android, iOS)`
- Given: 프록시 상태나 저장된 주소를 아직 확인하지 못한 상태다.
- When: 사용자가 SettingDownload 화면 본문을 확인한다.
- Then: 주소, 안내, 입력, 저장 동작이 모두 표시되지 않고 별도 로딩 또는 오류 상태도 제공되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-005: 저장된 주소가 입력된 상태로 시작한다

- 근거: `feature > 프록시 주소 입력 (Android, iOS)`
- Given: Android 또는 iOS에서 프록시 주소가 저장되어 있다.
- When: 사용자가 SettingDownload 화면에 진입한다.
- Then: 입력에 저장된 주소가 그대로 들어 있고, 저장 동작은 제공되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-006: 저장된 주소가 없으면 비어 있는 상태로 시작한다

- 근거: `feature > 프록시 주소 입력 (Android, iOS)`, `domain > 다운로드 프록시 설정`
- Given: Android 또는 iOS에서 프록시 주소를 한 번도 저장하지 않았다.
- When: 사용자가 SettingDownload 화면에 진입한다.
- Then: 입력이 비어 있고, 저장 동작은 제공되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-007: 편집 중인 주소가 저장된 주소와 다르면 저장 동작을 제공한다

- 근거: `feature > 설정 저장 (Android, iOS)`, `domain > 저장 가능 상태`
- Given: Android 또는 iOS에서 저장된 주소가 입력된 상태로 SettingDownload 화면이 표시되어 있다.
- When: 사용자가 입력을 저장된 주소와 다른 값으로 바꾼다.
- Then: 저장 동작이 제공된다.

### TC-SETTING-DOWNLOAD-FEATURE-008: 바꾼 값을 저장된 주소로 되돌리면 저장 동작을 다시 제공하지 않는다

- 근거: `feature > 설정 저장 (Android, iOS)`, `domain > 저장 가능 상태`
- Given: Android 또는 iOS에서 입력을 저장된 주소와 다른 값으로 바꿔 저장 동작이 제공되는 SettingDownload 화면이 표시되어 있다.
- When: 사용자가 입력을 저장된 주소와 같은 값으로 되돌린다.
- Then: 저장 동작이 제공되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-009: 저장하면 입력한 주소가 저장된다

- 근거: `feature > 설정 저장 (Android, iOS)`, `data > 저장 시점과 유지`
- Given: Android 또는 iOS에서 입력을 새 주소로 바꾼 SettingDownload 화면이 표시되어 있고, 저장이 성공하도록 설정되어 있다.
- When: 사용자가 저장을 실행한다.
- Then: 새 주소가 그대로 저장된다.

### TC-SETTING-DOWNLOAD-FEATURE-010: 저장하는 동안 진행 상태를 표시한다

- 근거: `feature > 설정 저장 (Android, iOS)`
- Given: Android 또는 iOS에서 입력을 새 주소로 바꾼 SettingDownload 화면이 표시되어 있고, 저장이 끝나지 않도록 설정되어 있다.
- When: 사용자가 저장을 실행한다.
- Then: 저장이 진행 중임이 표시된다.

### TC-SETTING-DOWNLOAD-FEATURE-011: 저장 중에는 같은 저장 요청을 처리하지 않는다

- 근거: `feature > 설정 저장 (Android, iOS)`
- Given: Android 또는 iOS에서 저장을 실행해 아직 끝나지 않은 SettingDownload 화면이 표시되어 있다.
- When: 사용자가 저장을 한 번 더 실행한다.
- Then: 저장은 한 번만 수행된다.

### TC-SETTING-DOWNLOAD-FEATURE-012: 저장에 성공하면 입력을 유지하고 성공을 알린다

- 근거: `feature > 설정 저장 (Android, iOS)`, `domain > 저장 가능 상태`
- Given: Android 또는 iOS에서 입력을 새 주소로 바꾼 SettingDownload 화면이 표시되어 있고, 저장이 성공하도록 설정되어 있다.
- When: 사용자가 저장을 실행한다.
- Then: 저장되었다는 피드백이 표시되고, 입력에 새 주소가 그대로 남으며, 저장 동작은 더 제공되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-013: 저장에 실패하면 입력을 유지하고 실패를 알린다

- 근거: `feature > 설정 저장 (Android, iOS)`, `domain > 저장 가능 상태`
- Given: Android 또는 iOS에서 입력을 새 주소로 바꾼 SettingDownload 화면이 표시되어 있고, 저장이 실패하도록 설정되어 있다.
- When: 사용자가 저장을 실행한다.
- Then: 저장하지 못했다는 피드백이 표시되고, 입력에 새 주소가 그대로 남으며, 저장 동작이 계속 제공된다.

### TC-SETTING-DOWNLOAD-FEATURE-014: 단독 화면에서 뒤로가면 설정 목록으로 돌아간다

- 근거: `feature > 뒤로가기`
- Given: SettingDownload 화면이 단독으로 표시되어 있다.
- When: 사용자가 뒤로가기 동작을 실행한다.
- Then: 이전 화면으로 돌아가는 동작이 한 번 실행된다.

### TC-SETTING-DOWNLOAD-FEATURE-015: 화면이 재생성되어도 입력 중이던 주소를 유지한다

- 근거: `feature > 입력 상태 유지 (Android, iOS)`, `domain > 유지와 초기화 기준`
- Given: Android 또는 iOS에서 입력을 새 주소로 바꾼 SettingDownload 화면이 표시되어 있다.
- When: 화면이 재생성된다.
- Then: 입력에 바꾼 주소가 그대로 남아 있다.

### TC-SETTING-DOWNLOAD-FEATURE-016: 저장하지 않고 떠나면 저장된 주소가 바뀌지 않는다

- 근거: `feature > 뒤로가기`
- Given: Android 또는 iOS에서 입력을 새 주소로 바꾼 SettingDownload 화면이 표시되어 있다.
- When: 사용자가 저장하지 않고 뒤로가기 동작을 실행한다.
- Then: 확인을 요구하지 않고 곧바로 화면을 떠나며, 저장은 수행되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-017: 입력은 저장된 주소를 처음 확인했을 때 한 번만 채운다

- 근거: `feature > 프록시 주소 입력 (Android, iOS)`
- Given: Android 또는 iOS에서 저장된 주소가 입력된 상태로 화면이 표시되었고, 사용자가 입력을 다른 값으로 바꿨다.
- When: 화면을 보는 동안 저장된 주소가 또 다른 값으로 바뀐다.
- Then: 입력에는 사용자가 바꾼 값이 그대로 남는다.

### TC-SETTING-DOWNLOAD-FEATURE-018: 저장된 주소를 읽지 못하면 입력과 저장 기능을 제공하지 않는다

- 근거: `feature > 프록시 주소 입력 (Android, iOS)`, `data > 저장 시점과 유지`
- Given: Android 또는 iOS에서 저장된 주소 읽기가 실패하도록 제어되어 있다.
- When: 사용자가 SettingDownload 화면 본문을 확인한다.
- Then: 비어 있는 입력으로 시작하지 않고, 입력과 저장 동작이 표시되지 않으며 별도 로딩 또는 오류 상태도 제공되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-019: 화면을 떠났다가 다시 들어오면 저장된 주소로 시작한다

- 근거: `feature > 입력 상태 유지 (Android, iOS)`, `domain > 유지와 초기화 기준`
- Given: Android 또는 iOS에서 저장된 주소가 입력된 상태로 SettingDownload 화면이 표시되었고, 사용자가 입력을 저장하지 않은 다른 값으로 바꿨다.
- When: 사용자가 저장하지 않고 화면을 떠났다가 다시 들어온다.
- Then: 입력에 사용자가 바꾼 값이 아니라 저장된 주소가 들어 있고, 저장 동작은 제공되지 않는다.

### TC-SETTING-DOWNLOAD-FEATURE-020: 데스크톱 앱에서는 화면에 다시 들어와도 주소 후보를 새로 확인하지 않는다

- 근거: `feature > 프록시 주소 확인 (데스크톱 앱)`
- Given: 데스크톱 앱에서 프록시가 제공 중이고, 사용자가 SettingDownload 화면에서 주소 후보를 확인한 뒤 화면을 떠났다.
- When: 사용자가 SettingDownload 화면에 다시 들어온다.
- Then: 앞서 확인한 주소 후보가 그대로 표시되고, 프록시 상태를 새로 정하지 않는다.

## domain

### TC-SETTING-DOWNLOAD-DOMAIN-001: 주소 비교는 넣은 그대로 본다

- 근거: `domain > 저장 가능 상태`
- Given: Android 또는 iOS에서 저장된 주소가 입력된 상태로 SettingDownload 화면이 표시되어 있다.
- When: 사용자가 입력을 테스트 데이터의 값으로 바꾼다.
- Then: 저장 동작이 제공된다.
- 테스트 데이터:

| 값 |
| --- |
| 저장된 주소 끝에 공백 하나를 더한 값 |
| 저장된 주소의 영문자 하나를 대문자로 바꾼 값 |

### TC-SETTING-DOWNLOAD-DOMAIN-002: 비어 있는 주소도 저장할 수 있다

- 근거: `domain > 저장 성립 조건`
- Given: Android 또는 iOS에서 저장된 주소가 입력된 상태로 SettingDownload 화면이 표시되어 있고, 저장이 성공하도록 설정되어 있다.
- When: 사용자가 입력을 모두 지우고 저장을 실행한다.
- Then: 비어 있는 주소가 저장된다.

### TC-SETTING-DOWNLOAD-DOMAIN-003: 저장 시점에 주소의 형태나 연결 가능 여부를 확인하지 않는다

- 근거: `domain > 저장 성립 조건`
- Given: Android 또는 iOS에서 SettingDownload 화면이 표시되어 있고, 저장이 성공하도록 설정되어 있다.
- When: 사용자가 프록시 주소의 형태가 아닌 값을 입력하고 저장을 실행한다.
- Then: 그 값이 그대로 저장되고, 어떤 연결 확인도 요청되지 않는다.

### TC-SETTING-DOWNLOAD-DOMAIN-004: 저장하는 동안에도 저장된 주소와 같아지면 저장 동작을 제공하지 않는다

- 근거: `domain > 저장 가능 상태`
- Given: Android 또는 iOS에서 입력을 저장된 주소와 다른 값으로 바꾸고 저장을 실행해 저장이 진행 중이다.
- When: 저장이 끝나기 전에 저장된 주소가 입력한 값과 같아진다.
- Then: 저장 동작과 진행 상태가 모두 사라진다.

### TC-SETTING-DOWNLOAD-DOMAIN-005: 저장하는 동안 사용자가 입력을 저장된 주소로 되돌리면 저장 동작과 진행 상태가 사라진다

- 근거: `domain > 저장 가능 상태`
- Given: Android 또는 iOS에서 입력을 저장된 주소와 다른 값으로 바꾸고 저장을 실행해 저장이 진행 중이다.
- When: 저장이 끝나기 전에 사용자가 입력을 저장된 주소와 같은 값으로 되돌린다.
- Then: 저장 동작과 진행 상태가 모두 사라진다.

## data

### TC-SETTING-DOWNLOAD-DATA-001: 저장한 주소를 다시 읽으면 같은 값이다

- 근거: `data > 저장 시점과 유지`
- Given: 프록시 주소를 저장할 수 있는 저장 영역이 비어 있다.
- When: 한 주소를 저장한 뒤 저장된 주소를 읽는다.
- Then: 저장한 주소와 같은 값을 읽는다.

### TC-SETTING-DOWNLOAD-DATA-002: 저장된 값이 없으면 비어 있는 주소로 읽는다

- 근거: `data > 저장 시점과 유지`
- Given: 프록시 주소를 한 번도 저장하지 않았다.
- When: 저장된 주소를 읽는다.
- Then: 비어 있는 주소를 읽는다.
