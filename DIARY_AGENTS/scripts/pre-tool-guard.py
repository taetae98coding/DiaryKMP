#!/usr/bin/env python3
"""Claude Code와 Codex가 함께 쓰는 PreToolUse hook. 문서로만 지키던 금지 사항을 도구 호출 전에 막는다.

- 로컬 전용 파일(DIARY_AGENTS/local-files.md 목록)의 내용을 읽거나 출력하지 않는다.
- 다른 worktree와 공유하는 stash 스택을 건드리는 인자 없는 stash 명령과 강제 push를 쓰지 않는다.
- verify-wave를 통과하지 않은 작업 트리를 커밋하지 않는다(DIARY_AGENTS/scripts/verify-stamp.sh check).

막을 때는 두 도구가 모두 이해하는 `permissionDecision: deny` JSON을 stdout에 쓰고 0으로 끝난다. 사용자가 검증 없는 커밋을 명시적으로 요청했으면
명령 앞에 `DIARY_VERIFY_BYPASS=1`을 붙여 커밋 확인만 건너뛴다.
"""

import json
import re
import shlex
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parents[2]

# 파일 내용을 화면에 내보낼 수 있는 명령. cp·mv·ls처럼 내용을 출력하지 않는 명령은 허용한다.
OUTPUT_COMMANDS = {
    "cat", "head", "tail", "less", "more", "bat", "grep", "rg", "egrep", "sed", "awk", "cut",
    "xxd", "hexdump", "od", "strings", "base64", "nl", "sort", "uniq", "diff", "cmp", "plutil",
    "python", "python3", "jq", "tr", "source", ".", "keytool",
}
# 목록의 파일을 읽어도 내용을 출력하지 않는 저장소 스크립트.
ALLOWED_SCRIPTS = ("copy-local-files.sh", "push_supabase_config.py")


def block(reason: str) -> None:
    decision = {"hookEventName": "PreToolUse", "permissionDecision": "deny", "permissionDecisionReason": reason}
    print(json.dumps({"hookSpecificOutput": decision}, ensure_ascii=False))
    sys.exit(0)


def shell_command(tool_input: dict) -> str:
    """Claude Code는 문자열, Codex는 `["bash", "-lc", "..."]` 같은 배열로 셸 명령을 넘길 수 있다."""
    command = tool_input.get("command", "")
    if isinstance(command, list):
        if len(command) >= 3 and command[-2] in ("-c", "-lc"):
            return command[-1]
        return shlex.join(command)
    return command


def local_files() -> list[str]:
    text = (root / "DIARY_AGENTS" / "local-files.md").read_text(encoding="utf-8")
    return re.findall(r"^\| `([^`]+)` \|", text, flags=re.MULTILINE)


def split_segments(command: str) -> list[list[str]]:
    segments = []
    for part in re.split(r"\|\||&&|[;|\n]", command):
        try:
            tokens = shlex.split(part)
        except ValueError:
            tokens = part.split()
        while tokens and re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*=.*", tokens[0]):
            tokens = tokens[1:]
        if tokens:
            segments.append(tokens)
    return segments


def check_read(file_path: str) -> None:
    try:
        relative = Path(file_path).resolve().relative_to(root).as_posix()
    except ValueError:
        return
    if relative in local_files():
        block(f"{relative}은 로컬 전용 파일이라 내용을 읽지 않는다(DIARY_AGENTS/local-files.md).")


def check_local_file_output(command: str, segments: list[list[str]]) -> None:
    if any(script in command for script in ALLOWED_SCRIPTS):
        return
    paths = local_files()
    for tokens in segments:
        if Path(tokens[0]).name not in OUTPUT_COMMANDS:
            continue
        for path in paths:
            if any(token.endswith(path) or token.endswith(Path(path).name) and "/" not in token for token in tokens[1:]):
                block(f"{path}은 로컬 전용 파일이라 내용을 출력하지 않는다(DIARY_AGENTS/local-files.md).")


def check_git(segments: list[list[str]]) -> None:
    for tokens in segments:
        if tokens[0] != "git":
            continue
        args = [token for token in tokens[1:]]
        # -C <경로>, -c <설정> 같은 전역 옵션을 건너뛴다.
        while args and args[0].startswith("-"):
            args = args[2:] if args[0] in ("-C", "-c") else args[1:]
        if not args:
            continue
        subcommand, rest = args[0], args[1:]
        if subcommand == "stash":
            action = rest[0] if rest else "push"
            if action in ("pop", "clear"):
                block("stash 스택은 다른 worktree와 공유한다. pop·clear 대신 `git stash apply <sha>` 후 태그로 찾은 항목만 drop한다.")
            if action in ("push", "save") and "-m" not in rest and "--message" not in rest:
                block("stash 스택은 다른 worktree와 공유한다. `git stash push -u -m \"<고유 태그>\"`로 넣고 SHA를 기록한다.")
            if action == "drop" and len(rest) < 2:
                block("stash 스택은 다른 worktree와 공유한다. 인자 없는 drop 대신 태그로 다시 찾은 `stash@{n}`을 지정한다.")
        if subcommand == "push":
            if any(token in ("-f", "--force", "--force-with-lease") or token.startswith("--force") or token.startswith("+") for token in rest):
                block("강제 push는 하지 않는다(merge-wave).")


def check_commit(command: str, segments: list[list[str]]) -> None:
    if "DIARY_VERIFY_BYPASS=1" in command:
        return
    for tokens in segments:
        if tokens[0] == "git" and "commit" in tokens[1:]:
            result = subprocess.run(
                [str(root / "DIARY_AGENTS" / "scripts" / "verify-stamp.sh"), "check"],
                capture_output=True,
                text=True,
            )
            if result.returncode != 0:
                block(result.stdout.strip() or result.stderr.strip())
            return


def main() -> None:
    payload = json.load(sys.stdin)
    tool = payload.get("tool_name")
    tool_input = payload.get("tool_input") or {}
    if tool == "Read":
        check_read(tool_input.get("file_path", ""))
    elif tool in ("Bash", "shell", "local_shell", "exec_command"):
        command = shell_command(tool_input)
        segments = split_segments(command)
        check_local_file_output(command, segments)
        check_git(segments)
        check_commit(command, segments)


if __name__ == "__main__":
    main()
