# 카메라 권한 요청 스펙

이 문서는 앱이 사용자에게 시스템 카메라 권한을 요청하는 규칙을 다룬다. 모든 권한이 공유하는 요청 기준, 요청 결과와 허용 여부 조회는 [권한 요청 공통 스펙](./permission.md)이 소유하고, 이 문서는 카메라 권한에만 적용되는 부분을 다룬다.

카메라로 무엇을 하는지는 [QrScan 화면 스펙](./qr-scan.md)이 소유한다.

권한 요청은 시스템이 표시하므로 앱이 제공하는 화면이나 조작 수단이 없고, 저장하거나 동기화하는 데이터도 없다. 따라서 `data` 영역은 생략한다.

## feature

### QR 스캔 시작 시 카메라 권한 요청

사용자가 [QrAdd 화면](./qr-add.md)에서 QR 스캔 시작을 선택하면 카메라 권한이 아직 허용되어 있지 않은 경우 시스템 카메라 권한 요청을 시작한다. 요청을 사용자에게 실제로 표시할지는 시스템이 정하며, 그 기준은 [권한 요청 공통 스펙](./permission.md)의 요청 기준을 따른다.

요청에 응답한 뒤 사용자가 보는 결과는 [QrAdd 화면 스펙](./qr-add.md)의 `QR 스캔 시작`이 정한다. 이는 [권한 요청 공통 스펙](./permission.md)의 권한 요청 응답에 대한 예외다.

## domain

### 요청 지점

카메라 권한 요청 조건은 화면이 시작될 때가 아니라 사용자가 QR 스캔 시작을 선택할 때마다 확인한다. 사용자가 카메라를 쓰겠다고 선택한 순간에 묻는 것이 요청의 이유를 가장 분명하게 알려 주기 때문이다.

QR 스캔 시작 밖에서는 카메라 권한을 요청하지 않는다. QrScan 화면에 들어와 있는 동안에도 요청하지 않는다.

### 요청 제공 범위

카메라 권한 요청은 Android와 iOS에서 제공한다. 웹과 데스크톱 앱에서는 요청하지 않는다. 이는 [QrAdd 화면 스펙](./qr-add.md)의 `QR 스캔 제공 범위`에 맞춘 것이며, 그 밖의 기준은 [권한 요청 공통 스펙](./permission.md)의 요청 제공 범위를 따른다.

### 요청 결과의 사용

`이미 허용`과 `허용`을 구분해 쓰지 않는다. 두 결과 모두 카메라를 쓸 수 있는 것으로 보고, `거부`만 카메라를 쓸 수 없는 것으로 본다.

요청 결과를 사용자 설정으로 저장하지 않는다.

## 참고

- [Android 카메라 권한](https://developer.android.com/media/camera/camerax/architecture#permissions)
- [Apple Requesting authorization to capture and save media](https://developer.apple.com/documentation/avfoundation/requesting-authorization-to-capture-and-save-media)
