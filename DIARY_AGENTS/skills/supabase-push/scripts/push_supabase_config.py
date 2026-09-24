#!/usr/bin/env python3
"""Push Supabase config with flavor-scoped, process-only credentials."""

from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path
from typing import Dict, Iterable, Mapping, Sequence, Set


EXPECTED_ENV_NAMES = frozenset(
    {
        "SUPABASE_AUTH_EXTERNAL_GOOGLE_CLIENT_ID",
        "SUPABASE_AUTH_EXTERNAL_GOOGLE_CLIENT_SECRET",
        "SUPABASE_AUTH_EXTERNAL_APPLE_CLIENT_ID",
    }
)
EXPECTED_PROJECT_REFS = {
    "dev": "ljsscxmqzsoulswmwsia",
    "real": "zsippzmbsjvkskkfxijl",
}
IOS_CONFIG_PATHS = {
    "dev": Path("iosApp/Config/DevLocal.xcconfig"),
    "real": Path("iosApp/Config/RealLocal.xcconfig"),
}
ENV_REFERENCE_PATTERN = re.compile(r"\benv\(\s*([A-Z_][A-Z0-9_]*)\s*\)")
GOOGLE_CLIENT_ID_PATTERN = re.compile(
    r"^[A-Za-z0-9][A-Za-z0-9._-]*\.apps\.googleusercontent\.com$"
)
APPLE_CLIENT_ID_PATTERN = re.compile(r"^[A-Za-z0-9][A-Za-z0-9-]*(\.[A-Za-z0-9][A-Za-z0-9-]*)+$")
PROJECT_REF_PATTERN = re.compile(r"^[a-z0-9]{20}$")


class PreflightError(Exception):
    """Raised when a safe push cannot be prepared."""


def parse_args(argv: Sequence[str]) -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Push Diary Supabase config without persisting credentials."
    )
    parser.add_argument("--flavor", required=True, choices=("dev", "real"))
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="validate inputs and print a redacted summary without calling Supabase",
    )
    return parser.parse_args(argv)


def discover_repo_root() -> Path:
    result = subprocess.run(
        ("git", "rev-parse", "--show-toplevel"),
        cwd=Path.cwd(),
        stdout=subprocess.PIPE,
        stderr=subprocess.DEVNULL,
        text=True,
        check=False,
    )
    if result.returncode == 0:
        root = Path(result.stdout.strip()).resolve()
        if (root / "supabase/config.toml").is_file():
            return root

    script_path = Path(__file__).resolve()
    for candidate in script_path.parents:
        if (candidate / "supabase/config.toml").is_file():
            return candidate

    raise PreflightError("Supabase config가 있는 Diary 저장소 루트를 찾지 못했습니다.")


def read_text(path: Path) -> str:
    if not path.is_file():
        raise PreflightError(f"필수 파일이 없습니다: {path}")
    try:
        return path.read_text(encoding="utf-8")
    except (OSError, UnicodeError) as error:
        raise PreflightError(f"필수 파일을 읽지 못했습니다: {path}") from error


def parse_assignments(path: Path, comment_prefixes: Iterable[str]) -> Dict[str, str]:
    assignments: Dict[str, str] = {}
    prefixes = tuple(comment_prefixes)

    for line_number, raw_line in enumerate(read_text(path).splitlines(), start=1):
        line = raw_line.strip()
        if not line or line.startswith(prefixes) or "=" not in line:
            continue

        key, value = line.split("=", 1)
        key = key.strip()
        if not key:
            continue
        if key in assignments:
            raise PreflightError(f"중복 키가 있습니다: {path}:{line_number} ({key})")
        assignments[key] = value.strip()

    return assignments


def require_value(values: Mapping[str, str], key: str, path: Path) -> str:
    if key not in values:
        raise PreflightError(f"필수 키가 없습니다: {path} ({key})")

    value = values[key].strip()
    if not value:
        raise PreflightError(f"필수 키 값이 비어 있습니다: {path} ({key})")
    if any(ord(character) < 32 or ord(character) == 127 for character in value):
        raise PreflightError(f"필수 키 값에 제어 문자가 있습니다: {path} ({key})")
    return value


def validate_client_id(value: str, source_name: str) -> None:
    if not GOOGLE_CLIENT_ID_PATTERN.fullmatch(value):
        raise PreflightError(f"Google client ID 형식이 올바르지 않습니다: {source_name}")


def resolve_apple_client_ids(raw_value: str, source_name: str) -> str:
    values = [entry.strip() for entry in raw_value.split(",")]
    if any(not entry for entry in values):
        raise PreflightError(f"Apple client ID 목록에 빈 항목이 있습니다: {source_name}")
    for entry in values:
        if not APPLE_CLIENT_ID_PATTERN.fullmatch(entry):
            raise PreflightError(f"Apple client ID 형식이 올바르지 않습니다: {source_name}")

    return ",".join(deduplicate(values))


def deduplicate(values: Iterable[str]) -> Sequence[str]:
    result = []
    seen: Set[str] = set()
    for value in values:
        if value not in seen:
            seen.add(value)
            result.append(value)
    return result


def strip_toml_comment(line: str) -> str:
    quote = None
    escaped = False

    for index, character in enumerate(line):
        if quote == '"':
            if escaped:
                escaped = False
            elif character == "\\":
                escaped = True
            elif character == '"':
                quote = None
        elif quote == "'":
            if character == "'":
                quote = None
        elif character in ('"', "'"):
            quote = character
        elif character == "#":
            return line[:index]

    return line


def active_toml_lines(config_text: str) -> Iterable[str]:
    for raw_line in config_text.splitlines():
        line = strip_toml_comment(raw_line).strip()
        if line:
            yield line


def find_env_references(config_text: str) -> Set[str]:
    return {
        match.group(1)
        for line in active_toml_lines(config_text)
        for match in ENV_REFERENCE_PATTERN.finditer(line)
    }


def parse_toml_string(raw_value: str, path: Path, key: str) -> str:
    value = raw_value.strip()
    if len(value) < 2 or value[0] not in ('"', "'") or value[-1] != value[0]:
        raise PreflightError(f"문자열 설정 형식이 올바르지 않습니다: {path} ({key})")
    parsed = value[1:-1].strip()
    if not parsed:
        raise PreflightError(f"문자열 설정 값이 비어 있습니다: {path} ({key})")
    return parsed


def parse_project_refs(config_text: str, path: Path) -> Dict[str, str]:
    section = ""
    refs: Dict[str, str] = {}

    for line in active_toml_lines(config_text):
        if line.startswith("[") and line.endswith("]"):
            section = line[1:-1].strip()
            continue
        if "=" not in line:
            continue

        key, raw_value = line.split("=", 1)
        if key.strip() != "project_id":
            continue

        flavor = "dev" if not section else "real" if section == "remotes.real" else None
        if flavor is None:
            continue
        if flavor in refs:
            raise PreflightError(f"project_id가 중복되어 있습니다: {path} ({flavor})")
        refs[flavor] = parse_toml_string(raw_value, path, f"{flavor}.project_id")

    return refs


def resolve_project_ref(flavor: str, config_text: str, config_path: Path) -> str:
    refs = parse_project_refs(config_text, config_path)
    if flavor not in refs:
        raise PreflightError(f"flavor project_id가 없습니다: {config_path} ({flavor})")

    project_ref = refs[flavor]
    if not PROJECT_REF_PATTERN.fullmatch(project_ref):
        raise PreflightError(f"project_id 형식이 올바르지 않습니다: {config_path} ({flavor})")

    expected = EXPECTED_PROJECT_REFS[flavor]
    if project_ref != expected:
        raise PreflightError(
            f"project_id가 스킬의 안전 목록과 다릅니다: {config_path} ({flavor})"
        )
    return project_ref


def resolve_environment(flavor: str, root: Path) -> Dict[str, str]:
    local_path = root / "local.properties"
    ios_path = root / IOS_CONFIG_PATHS[flavor]
    local_values = parse_assignments(local_path, ("#", "!"))
    ios_values = parse_assignments(ios_path, ("//", "#"))

    web_key = f"{flavor}.wasm.googleCredentialsClientId"
    android_key = f"{flavor}.android.googleCredentialsServerClientId"
    jvm_key = f"{flavor}.jvm.googleCredentialsClientId"
    secret_key = f"{flavor}.wasm.googleSecret"
    apple_client_ids_key = f"{flavor}.apple.clientIds"
    apple_web_client_id_key = f"{flavor}.apple.webClientId"

    web_client_id = require_value(local_values, web_key, local_path)
    android_client_id = require_value(local_values, android_key, local_path)
    jvm_client_id = require_value(local_values, jvm_key, local_path)
    ios_server_client_id = require_value(ios_values, "GID_SERVER_CLIENT_ID", ios_path)
    ios_client_id = require_value(ios_values, "GID_CLIENT_ID", ios_path)
    web_secret = require_value(local_values, secret_key, local_path)
    apple_client_ids = require_value(local_values, apple_client_ids_key, local_path)
    apple_web_client_id = require_value(local_values, apple_web_client_id_key, local_path)

    client_sources = {
        web_key: web_client_id,
        android_key: android_client_id,
        jvm_key: jvm_client_id,
        "GID_SERVER_CLIENT_ID": ios_server_client_id,
        "GID_CLIENT_ID": ios_client_id,
    }
    for source_name, value in client_sources.items():
        validate_client_id(value, source_name)

    if not web_client_id == android_client_id == ios_server_client_id:
        raise PreflightError(
            "웹 client ID가 flavor별 Android/WASM/iOS 설정에서 서로 다릅니다."
        )

    client_ids = ",".join(deduplicate((web_client_id, ios_client_id, jvm_client_id)))
    resolved = {
        "SUPABASE_AUTH_EXTERNAL_GOOGLE_CLIENT_ID": client_ids,
        "SUPABASE_AUTH_EXTERNAL_GOOGLE_CLIENT_SECRET": web_secret,
        "SUPABASE_AUTH_EXTERNAL_APPLE_CLIENT_ID": resolve_apple_client_ids(
            f"{apple_client_ids},{apple_web_client_id}",
            f"{apple_client_ids_key},{apple_web_client_id_key}",
        ),
    }
    for name, value in resolved.items():
        if not value.strip():
            raise PreflightError(f"생성된 환경변수 값이 비어 있습니다: {name}")
    return resolved


def main(argv: Sequence[str]) -> int:
    args = parse_args(argv)

    try:
        root = discover_repo_root()
        config_path = root / "supabase/config.toml"
        config_text = read_text(config_path)
        env_references = find_env_references(config_text)

        if env_references != EXPECTED_ENV_NAMES:
            missing = sorted(EXPECTED_ENV_NAMES - env_references)
            unsupported = sorted(env_references - EXPECTED_ENV_NAMES)
            details = []
            if missing:
                details.append("누락=" + ",".join(missing))
            if unsupported:
                details.append("미지원=" + ",".join(unsupported))
            raise PreflightError(
                "config.toml env(...) 참조가 예상과 다릅니다: " + "; ".join(details)
            )

        project_ref = resolve_project_ref(args.flavor, config_text, config_path)
        resolved_env = resolve_environment(args.flavor, root)
        for name in env_references:
            if name not in resolved_env or not resolved_env[name].strip():
                raise PreflightError(f"환경변수를 비어 있지 않게 구성하지 못했습니다: {name}")

        supabase = shutil.which("supabase")
        if supabase is None:
            raise PreflightError("supabase CLI를 PATH에서 찾지 못했습니다.")

        print(
            f"Supabase config push 준비 완료: flavor={args.flavor}, "
            f"project_ref={project_ref}, env_count={len(resolved_env)}"
        )
        if args.dry_run:
            print("dry-run: Supabase CLI를 호출하지 않았습니다.")
            return 0

        child_env = os.environ.copy()
        for name in EXPECTED_ENV_NAMES:
            child_env.pop(name, None)
        child_env.update(resolved_env)

        command = (
            supabase,
            "--workdir",
            str(root),
            "config",
            "push",
            "--project-ref",
            project_ref,
        )
        return subprocess.run(command, cwd=root, env=child_env, check=False).returncode
    except PreflightError as error:
        print(f"Supabase config push 중단: {error}", file=sys.stderr)
        return 2
    except OSError as error:
        print(f"Supabase config push 실행 실패: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main(sys.argv[1:]))
