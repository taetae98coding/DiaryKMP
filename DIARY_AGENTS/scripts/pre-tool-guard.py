#!/usr/bin/env python3
"""Claude Code와 Codex가 함께 쓰는 PreToolUse hook. 문서로만 지키던 금지 사항을 도구 호출 전에 막는다.

- 로컬 전용 파일(DIARY_AGENTS/local-files.md 목록)의 내용을 읽거나(Read), 검색하거나(Grep), 출력 명령에 넘기지 않는다.
- 다른 worktree와 공유하는 stash 스택을 건드리는 stash 명령(pop·clear, 메시지 없는 push·save, 대상 없는 drop)과 강제 push를 쓰지 않는다.
- verify-wave를 통과하지 않은 작업 트리를 커밋하지 않는다(DIARY_AGENTS/scripts/verify-stamp.sh check).

막을 때는 두 도구가 모두 이해하는 `permissionDecision: deny` JSON을 stdout에 쓰고 0으로 끝난다. 사용자가 검증 없는 커밋을 명시적으로 요청했으면
명령 앞에 `DIARY_VERIFY_BYPASS=1`을 붙여 커밋 확인만 건너뛴다.
"""

import fnmatch
import json
import re
import shlex
import subprocess
import sys
from pathlib import Path
from typing import Optional

root = Path(__file__).resolve().parents[2]

# 파일 내용을 화면에 내보낼 수 있는 명령. cp·mv·ls처럼 내용을 출력하지 않는 명령은 허용한다.
OUTPUT_COMMANDS = {
    "cat", "head", "tail", "less", "more", "bat", "grep", "rg", "egrep", "sed", "awk", "cut",
    "xxd", "hexdump", "od", "strings", "base64", "nl", "sort", "uniq", "diff", "cmp", "plutil",
    "python", "python3", "jq", "tr", "source", ".", "keytool",
}
# 목록의 파일을 읽어도 내용을 출력하지 않는 저장소 스크립트.
ALLOWED_SCRIPTS = ("copy-local-files.sh", "push_supabase_config.py")
# 명령을 나누는 셸 연산자. 리다이렉션(<, >)은 연산자로 읽되 명령을 나누지 않는다.
SEPARATOR_CHARS = ";|&()\n"
# 값을 다음 토큰으로 받는 git 전역 옵션. 하위 명령을 찾을 때 값까지 건너뛴다.
GIT_GLOBAL_VALUE_OPTIONS = ("-C", "-c", "--git-dir", "--work-tree", "--namespace", "--config-env", "--exec-path", "--super-prefix")


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


def strip_assignments(tokens: list[str]) -> list[str]:
    while tokens and re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*=.*", tokens[0]):
        tokens = tokens[1:]
    return tokens


def split_segments(command: str) -> list[list[str]]:
    """명령을 `&&`, `||`, `;`, `|`, 줄바꿈, 괄호에서 나눠 단순 명령별 토큰으로 돌려준다.

    따옴표 안의 연산자에서는 나누지 않는다(`echo 'a; git commit'`은 한 명령이다). 따옴표가 맞지 않아 읽지 못하면
    따옴표를 보지 않고 나눈다. 이때는 더 잘게 나뉘어 막는 쪽으로 기운다.
    """
    lexer = shlex.shlex(command, posix=True, punctuation_chars=SEPARATOR_CHARS + "<>")
    lexer.whitespace = " \t\r"
    lexer.whitespace_split = True
    lexer.commenters = ""
    segments: list[list[str]] = [[]]
    try:
        for token in lexer:
            if token and all(char in SEPARATOR_CHARS for char in token):
                segments.append([])
            else:
                segments[-1].append(token)
    except ValueError:
        segments = []
        for part in re.split(r"\|\||&&|[;|\n()]", command):
            try:
                segments.append(shlex.split(part))
            except ValueError:
                segments.append(part.split())
    return [tokens for tokens in map(strip_assignments, segments) if tokens]


def git_subcommand(tokens: list[str]) -> Optional[tuple[str, list[str]]]:
    """`git` 명령이면 전역 옵션(`-C <경로>`, `-c <설정>` 등)을 건너뛴 하위 명령과 그 뒤의 인자를 돌려준다."""
    if Path(tokens[0]).name != "git":
        return None
    args = tokens[1:]
    while args and args[0].startswith("-"):
        args = args[2:] if args[0] in GIT_GLOBAL_VALUE_OPTIONS else args[1:]
    if not args:
        return None
    return args[0], args[1:]


def has_stash_message(arguments: list[str]) -> bool:
    """`-m <태그>`, `-um <태그>`처럼 묶은 짧은 옵션, `--message <태그>`, `--message=<태그>`를 메시지로 본다."""
    return any(re.match(r"-[A-Za-z]*m", token) or token.startswith("--message") for token in arguments)


def check_read(file_path: str) -> None:
    try:
        relative = Path(file_path).resolve().relative_to(root).as_posix()
    except ValueError:
        return
    if relative in local_files():
        block(f"{relative}은 로컬 전용 파일이라 내용을 읽지 않는다(DIARY_AGENTS/local-files.md).")


def check_grep(tool_input: dict) -> None:
    """Grep이 목록의 파일을 직접 가리키면 막는다. 디렉터리 검색은 ripgrep이 Git ignored 파일을 건너뛰므로 두지만,
    `glob`은 ignore 규칙보다 앞서므로 목록의 파일 이름을 그대로 적은 glob도 막는다."""
    if tool_input.get("path"):
        check_read(tool_input["path"])
    glob = tool_input.get("glob") or ""
    name = glob.rsplit("/", 1)[-1]
    if not name or any(char in name for char in "*?[{"):
        return
    for path in local_files():
        if Path(path).name == name and (fnmatch.fnmatch(path, glob) or "/" not in glob):
            block(f"{path}은 로컬 전용 파일이라 내용을 검색하지 않는다(DIARY_AGENTS/local-files.md).")


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
        parsed = git_subcommand(tokens)
        if parsed is None:
            continue
        subcommand, rest = parsed
        if subcommand == "stash":
            # `git stash -u`처럼 하위 명령 없이 옵션만 붙이면 git은 push로 실행한다.
            action = rest[0] if rest and not rest[0].startswith("-") else "push"
            if action in ("pop", "clear"):
                block("stash 스택은 다른 worktree와 공유한다. pop·clear 대신 `git stash apply <sha>` 후 태그로 찾은 항목만 drop한다.")
            if action in ("push", "save") and not has_stash_message(rest):
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
        # 하위 명령이 commit일 때만 본다. `git log --grep commit`처럼 인자에 commit이 들어간 명령은 커밋이 아니다.
        parsed = git_subcommand(tokens)
        if parsed is not None and parsed[0] == "commit":
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
    elif tool == "Grep":
        check_grep(tool_input)
    elif tool in ("Bash", "shell", "local_shell", "exec_command"):
        command = shell_command(tool_input)
        segments = split_segments(command)
        check_local_file_output(command, segments)
        check_git(segments)
        check_commit(command, segments)


if __name__ == "__main__":
    main()
