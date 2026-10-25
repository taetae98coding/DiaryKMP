#!/usr/bin/env python3
"""스토어에 같은 빌드가 이미 올라가 있는지 확인해 true/false를 출력한다.

python3 store_build_exists.py play <package> <versionCode>
    PLAY_SERVICE_ACCOUNT_JSON 환경 변수의 서비스 계정으로 Play에 올라간 번들의 versionCode를 조회한다.
python3 store_build_exists.py asc <appId> <version> <build>
    ASC_KEY_ID, ASC_ISSUER_ID, ASC_KEY_P8 환경 변수의 API 키로 App Store Connect 빌드를 조회한다.

러너에 추가 패키지를 설치하지 않도록 표준 라이브러리와 openssl만 쓴다.
"""

import base64
import json
import os
import subprocess
import sys
import tempfile
import time
import urllib.parse
import urllib.request


def b64url(data: bytes) -> str:
    return base64.urlsafe_b64encode(data).rstrip(b"=").decode()


def sign(message: bytes, private_key: str) -> bytes:
    with tempfile.NamedTemporaryFile("w", suffix=".pem", dir=os.environ.get("RUNNER_TEMP")) as key_file:
        key_file.write(private_key)
        key_file.flush()
        return subprocess.run(
            ["openssl", "dgst", "-sha256", "-sign", key_file.name],
            input=message,
            capture_output=True,
            check=True,
        ).stdout


def jwt(header: dict, claims: dict, private_key: str, ecdsa: bool) -> str:
    message = f"{b64url(json.dumps(header).encode())}.{b64url(json.dumps(claims).encode())}".encode()
    signature = sign(message, private_key)
    if ecdsa:
        signature = der_to_raw_ecdsa(signature)
    return f"{message.decode()}.{b64url(signature)}"


# openssl은 ECDSA 서명을 DER(SEQUENCE { r, s })로 내지만 JWT ES256은 32바이트 r과 s를 이어 붙인 값을 요구한다.
def der_to_raw_ecdsa(der: bytes) -> bytes:
    index = 2 if der[1] < 0x80 else 2 + (der[1] & 0x7F)
    parts = []
    for _ in range(2):
        length = der[index + 1]
        value = der[index + 2 : index + 2 + length]
        parts.append(value.lstrip(b"\x00").rjust(32, b"\x00"))
        index += 2 + length
    return b"".join(parts)


def request(method: str, url: str, token: str | None = None, data: bytes | None = None, form: bool = False) -> dict:
    headers = {}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    if data is not None:
        headers["Content-Type"] = "application/x-www-form-urlencoded" if form else "application/json"
    with urllib.request.urlopen(urllib.request.Request(url, data=data, headers=headers, method=method)) as response:
        body = response.read()
    return json.loads(body) if body else {}


def play_exists(package: str, version_code: int) -> bool:
    account = json.loads(os.environ["PLAY_SERVICE_ACCOUNT_JSON"])
    now = int(time.time())
    assertion = jwt(
        {"alg": "RS256", "typ": "JWT"},
        {
            "iss": account["client_email"],
            "scope": "https://www.googleapis.com/auth/androidpublisher",
            "aud": account["token_uri"],
            "iat": now,
            "exp": now + 600,
        },
        account["private_key"],
        ecdsa=False,
    )
    token = request(
        "POST",
        account["token_uri"],
        data=urllib.parse.urlencode(
            {"grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer", "assertion": assertion}
        ).encode(),
        form=True,
    )["access_token"]

    # 번들 목록은 edit 안에서만 읽을 수 있으므로 임시 edit을 열고 커밋하지 않고 지운다.
    base = f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{package}/edits"
    edit_id = request("POST", base, token, data=b"{}")["id"]
    try:
        bundles = request("GET", f"{base}/{edit_id}/bundles", token).get("bundles", [])
    finally:
        request("DELETE", f"{base}/{edit_id}", token)
    return any(bundle.get("versionCode") == version_code for bundle in bundles)


def asc_exists(app_id: str, version: str, build: str) -> bool:
    now = int(time.time())
    token = jwt(
        {"alg": "ES256", "kid": os.environ["ASC_KEY_ID"], "typ": "JWT"},
        {"iss": os.environ["ASC_ISSUER_ID"], "iat": now, "exp": now + 600, "aud": "appstoreconnect-v1"},
        os.environ["ASC_KEY_P8"],
        ecdsa=True,
    )
    query = urllib.parse.urlencode(
        {
            "filter[app]": app_id,
            "filter[version]": build,
            "filter[preReleaseVersion.version]": version,
            "limit": "1",
        }
    )
    return bool(request("GET", f"https://api.appstoreconnect.apple.com/v1/builds?{query}", token)["data"])


def main() -> None:
    store, *args = sys.argv[1:]
    if store == "play":
        exists = play_exists(args[0], int(args[1]))
    elif store == "asc":
        exists = asc_exists(args[0], args[1], args[2])
    else:
        sys.exit(f"알 수 없는 스토어: {store}")
    print("true" if exists else "false")


if __name__ == "__main__":
    main()
