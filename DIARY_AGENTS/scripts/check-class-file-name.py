#!/usr/bin/env python3
"""컴파일한 class 파일 이름이 Linux 파일 이름 한도(255바이트)를 넘지 않는지 검사한다.

Kotlin은 백틱 테스트 이름을 class 파일 이름에 그대로 쓴다. macOS는 파일 이름 길이를 글자 수로 세서 통과하지만,
CI의 Linux(ext4)는 UTF-8 바이트로 세므로 한 글자가 3바이트인 한글 이름이 길면 그 테스트 모듈의 컴파일이 실패한다.
어떤 람다와 inline 호출이 별도 class를 만드는지는 소스만으로 정확히 알 수 없으므로, 컴파일 결과를 직접 검사한다.

Gradle 테스트를 실행한 뒤 실행한다. 각 모듈의 build/classes 아래만 본다.

사용법: DIARY_AGENTS/scripts/check-class-file-name.py
문제가 없으면 `문제 없음`을 출력하고 0으로, 있으면 파일마다 바이트 수와 경로를 출력하고 1로 끝난다.
"""

import os
import sys
from pathlib import Path

LIMIT = 255
SKIPPED_DIRS = {".git", ".gradle", ".idea", "node_modules", "kotlin-js-store", "iosApp"}

root = Path(__file__).resolve().parents[2]


def class_dirs():
    for current, dirs, _ in os.walk(root):
        if os.path.basename(current) == "build":
            classes = os.path.join(current, "classes")
            if os.path.isdir(classes):
                yield classes
            dirs[:] = []
            continue
        dirs[:] = [name for name in dirs if name not in SKIPPED_DIRS]


def main():
    problems = []
    for classes in class_dirs():
        for current, _, files in os.walk(classes):
            for name in files:
                size = len(name.encode("utf-8"))
                if size > LIMIT:
                    path = Path(current, name).relative_to(root).as_posix()
                    problems.append((size, path))

    if not problems:
        print("문제 없음")
        return 0
    for size, path in sorted(problems, reverse=True):
        print(f"{size}바이트 {path}")
    return 1


if __name__ == "__main__":
    sys.exit(main())
