#!/usr/bin/env python3
"""테스트 케이스 문서와 테스트 코드 사이의 TC ID 추적을 기계적으로 검사한다.

검사 항목:
  duplicate  같은 ID가 테스트 케이스 문서의 케이스 제목에 두 번 이상 나온다.
  orphan     테스트 코드가 참조하는 ID가 어느 테스트 케이스 문서에도 없다.
  missing    `작성하지 않는 이유`가 없는 케이스인데 그 ID를 참조하는 테스트가 없다.

`작성하지 않는 이유`가 있는 케이스도 자동화할 수 있는 부분은 그 ID로 테스트하므로, 이유가 있는데 테스트가 있는 것은 문제로 보지 않는다.

duplicate와 orphan은 저장소 전체를, missing은 인자로 준 slug만 검사한다. slug를 주지 않으면 모든 문서를 검사한다.

사용법: DIARY_AGENTS/scripts/check-testcase-trace.py [slug ...]
문제가 없으면 `문제 없음`을 출력하고 0으로, 있으면 항목별로 출력하고 1로 끝난다.

DIARY_AGENTS/scripts/check-testcase-trace.py --map <slug> [slug ...]
slug의 케이스마다 ID, `작성하지 않는 이유` 유무, 그 ID를 참조하는 테스트 파일을 한 줄씩 출력한다. 검토 세션에 넘겨 ID를 하나씩 찾지 않게 한다.
"""

import re
import sys
from collections import defaultdict
from pathlib import Path

ID_PATTERN = re.compile(r"TC-[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")
CASE_HEADING = re.compile(r"^#{2,6}\s+(" + ID_PATTERN.pattern + r")\b")
SKIP_REASON = re.compile(r"^-\s*작성하지 않는 이유\s*:")
TEST_SOURCE_DIR = re.compile(r"/src/[A-Za-z]*[Tt]est[A-Za-z]*/")

root = Path(__file__).resolve().parents[2]
testcase_dir = root / "docs" / "testcase"


def read_cases():
    """{slug: [(id, has_skip_reason, line)]}"""
    cases = defaultdict(list)
    for path in sorted(testcase_dir.glob("*.md")):
        if path.name == "README.md":
            continue
        current = None
        for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
            heading = CASE_HEADING.match(line)
            if heading:
                current = [heading.group(1), False, number]
                cases[path.stem].append(current)
            elif line.startswith("#"):
                current = None
            elif current is not None and SKIP_REASON.match(line):
                current[1] = True
    return cases


def read_test_references():
    """{id: [path]}"""
    references = defaultdict(set)
    for path in root.rglob("*.kt"):
        relative = path.relative_to(root).as_posix()
        if "/build/" in f"/{relative}" or not TEST_SOURCE_DIR.search(f"/{relative}"):
            continue
        for found in ID_PATTERN.findall(path.read_text(encoding="utf-8", errors="ignore")):
            references[found].add(relative)
    return references


def print_map(slugs):
    cases = read_cases()
    references = read_test_references()
    for slug in slugs:
        if slug not in cases:
            print(f"테스트 케이스 문서가 없는 slug: {slug}")
            return 1
        for case_id, has_skip_reason, line in cases[slug]:
            reason = " [작성하지 않는 이유]" if has_skip_reason else ""
            tests = ", ".join(sorted(references.get(case_id, ()))) or "-"
            print(f"docs/testcase/{slug}.md:{line} {case_id}{reason}: {tests}")
    return 0


def main(slugs):
    cases = read_cases()
    unknown = [slug for slug in slugs if slug not in cases]
    if unknown:
        print("테스트 케이스 문서가 없는 slug: " + ", ".join(unknown))
        return 1

    references = read_test_references()
    locations = defaultdict(list)
    for slug, entries in cases.items():
        for case_id, _, line in entries:
            locations[case_id].append(f"docs/testcase/{slug}.md:{line}")

    problems = []
    for case_id, places in sorted(locations.items()):
        if len(places) > 1:
            problems.append(f"duplicate {case_id}: {', '.join(places)}")
    for case_id, paths in sorted(references.items()):
        if case_id not in locations:
            problems.append(f"orphan {case_id}: {', '.join(sorted(paths))}")
    for slug in slugs or sorted(cases):
        for case_id, has_skip_reason, line in cases[slug]:
            place = f"docs/testcase/{slug}.md:{line}"
            if not has_skip_reason and case_id not in references:
                problems.append(f"missing {case_id}: {place}")

    if not problems:
        print("문제 없음")
        return 0
    print("\n".join(problems))
    return 1


if __name__ == "__main__":
    if sys.argv[1:2] == ["--map"]:
        sys.exit(print_map(sys.argv[2:]))
    sys.exit(main(sys.argv[1:]))
