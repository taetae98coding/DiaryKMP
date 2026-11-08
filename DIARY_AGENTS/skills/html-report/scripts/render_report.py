#!/usr/bin/env python3
"""결정 항목 JSON을 HTML 결정 보고서로 렌더링한다.

사용법:
  python3 render_report.py <input.json> <output.html> [--open]
"""

import argparse
import base64
import json
import subprocess
import sys
from pathlib import Path

TEMPLATE = Path(__file__).resolve().parent.parent / "assets" / "template.html"
PLACEHOLDER = "/*__REPORT_DATA__*/null"
TYPES = {"radio", "checkbox", "text"}
FIGURE_KINDS = {"image", "mermaid", "svg"}
IMAGE_MIME = {
    ".png": "image/png",
    ".jpg": "image/jpeg",
    ".jpeg": "image/jpeg",
    ".gif": "image/gif",
    ".webp": "image/webp",
    ".svg": "image/svg+xml",
}
MAX_IMAGE_BYTES = 5 * 1024 * 1024


def validate_examples(value, where, errors):
    if value is None:
        return
    if not isinstance(value, list):
        errors.append(f"{where}.examples는 배열이어야 한다.")
        return
    for index, example in enumerate(value, 1):
        example_where = f"{where}.examples[{index}]"
        if not isinstance(example, dict):
            errors.append(f"{example_where}는 객체여야 한다.")
        elif not isinstance(example.get("content"), str) or not example["content"].strip():
            errors.append(f"{example_where}.content가 비어 있다.")


def validate_figures(value, where, errors):
    if value is None:
        return
    if not isinstance(value, list):
        errors.append(f"{where}.figures는 배열이어야 한다.")
        return
    for index, figure in enumerate(value, 1):
        figure_where = f"{where}.figures[{index}]"
        if not isinstance(figure, dict):
            errors.append(f"{figure_where}는 객체여야 한다.")
            continue
        kind = figure.get("kind")
        if kind not in FIGURE_KINDS:
            errors.append(f"{figure_where}.kind는 image, mermaid, svg 중 하나여야 한다.")
        elif kind == "image":
            if not isinstance(figure.get("src"), str) or not figure["src"].strip():
                errors.append(f"{figure_where}.src가 비어 있다.")
        elif not isinstance(figure.get("content"), str) or not figure["content"].strip():
            errors.append(f"{figure_where}.content가 비어 있다.")


def embed_images(data, base_dir, errors):
    """로컬 이미지 경로를 data URI로 바꿔 HTML 하나로 열리게 한다."""
    holders = [data] + data["items"] + [o for item in data["items"] for o in item.get("options", [])]
    for holder in holders:
        for figure in holder.get("figures") or []:
            if figure.get("kind") != "image":
                continue
            src = figure["src"]
            if src.startswith(("http://", "https://", "data:")):
                continue
            path = Path(src).expanduser()
            if not path.is_absolute():
                path = base_dir / path
            mime = IMAGE_MIME.get(path.suffix.lower())
            if mime is None:
                errors.append(f"이미지 '{src}'는 지원하지 않는 형식이다 ({', '.join(IMAGE_MIME)}).")
            elif not path.is_file():
                errors.append(f"이미지 '{src}'를 찾을 수 없다 ({path}).")
            elif path.stat().st_size > MAX_IMAGE_BYTES:
                errors.append(f"이미지 '{src}'가 5MB를 넘는다. 크기를 줄여서 쓴다.")
            else:
                encoded = base64.b64encode(path.read_bytes()).decode("ascii")
                figure["src"] = f"data:{mime};base64,{encoded}"


def validate(data):
    errors = []
    if not isinstance(data, dict):
        return ["최상위 값은 객체여야 한다."]
    if not isinstance(data.get("title"), str) or not data["title"].strip():
        errors.append("title이 비어 있다.")
    validate_examples(data.get("examples"), "report", errors)
    validate_figures(data.get("figures"), "report", errors)
    items = data.get("items")
    if not isinstance(items, list) or not items:
        return errors + ["items가 비어 있다."]

    seen = set()
    for index, item in enumerate(items, 1):
        where = f"items[{index}]"
        if not isinstance(item, dict):
            errors.append(f"{where}는 객체여야 한다.")
            continue
        item_id = item.get("id")
        if not isinstance(item_id, str) or not item_id.strip():
            errors.append(f"{where}.id가 비어 있다.")
        elif item_id in seen:
            errors.append(f"{where}.id '{item_id}'가 중복된다.")
        else:
            seen.add(item_id)
        if not isinstance(item.get("title"), str) or not item["title"].strip():
            errors.append(f"{where}.title이 비어 있다.")
        validate_examples(item.get("examples"), where, errors)
        validate_figures(item.get("figures"), where, errors)
        kind = item.get("type")
        if kind not in TYPES:
            errors.append(f"{where}.type은 radio, checkbox, text 중 하나여야 한다.")
            continue
        if kind == "text":
            if "default" in item and not isinstance(item["default"], str):
                errors.append(f"{where}.default는 문자열이어야 한다.")
            continue

        options = item.get("options")
        if not isinstance(options, list) or len(options) < 2:
            errors.append(f"{where}.options는 선택지가 2개 이상이어야 한다.")
            continue
        option_ids = set()
        recommended = 0
        for option_index, option in enumerate(options, 1):
            option_where = f"{where}.options[{option_index}]"
            if not isinstance(option, dict):
                errors.append(f"{option_where}는 객체여야 한다.")
                continue
            option_id = option.get("id")
            if not isinstance(option_id, str) or not option_id.strip():
                errors.append(f"{option_where}.id가 비어 있다.")
            elif option_id in option_ids:
                errors.append(f"{option_where}.id '{option_id}'가 중복된다.")
            else:
                option_ids.add(option_id)
            if not isinstance(option.get("label"), str) or not option["label"].strip():
                errors.append(f"{option_where}.label이 비어 있다.")
            for key in ("pros", "cons"):
                value = option.get(key, [])
                if not isinstance(value, list) or not all(isinstance(v, str) for v in value):
                    errors.append(f"{option_where}.{key}는 문자열 배열이어야 한다.")
            validate_examples(option.get("examples"), option_where, errors)
            validate_figures(option.get("figures"), option_where, errors)
            if option.get("recommended") is True:
                recommended += 1
        if kind == "radio" and recommended > 1:
            errors.append(f"{where}는 radio라서 recommended가 최대 하나여야 한다.")
    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("input", type=Path)
    parser.add_argument("output", type=Path)
    parser.add_argument("--open", action="store_true", help="생성한 HTML을 기본 브라우저로 연다.")
    args = parser.parse_args()

    try:
        data = json.loads(args.input.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        print(f"입력 JSON을 읽지 못했다: {error}", file=sys.stderr)
        return 1

    errors = validate(data)
    if not errors:
        embed_images(data, args.input.resolve().parent, errors)
    if errors:
        print("입력 JSON 형식 오류:", file=sys.stderr)
        for error in errors:
            print(f"  - {error}", file=sys.stderr)
        return 1

    template = TEMPLATE.read_text(encoding="utf-8")
    if PLACEHOLDER not in template:
        print(f"템플릿에 데이터 자리({PLACEHOLDER})가 없다.", file=sys.stderr)
        return 1

    # <script> 안에 넣으므로 </script>, <!-- 로 해석될 수 있는 문자를 이스케이프한다.
    payload = (
        json.dumps(data, ensure_ascii=False)
        .replace("<", "\\u003c")
        .replace(">", "\\u003e")
        .replace("&", "\\u0026")
        .replace(" ", "\\u2028")
        .replace(" ", "\\u2029")
    )
    html = template.replace(PLACEHOLDER, payload).replace("__REPORT_TITLE__", escape_html(data["title"]))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(html, encoding="utf-8")
    print(args.output.resolve())

    if args.open:
        opener = "open" if sys.platform == "darwin" else "xdg-open"
        subprocess.run([opener, str(args.output.resolve())], check=False)
    return 0


def escape_html(text):
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace('"', "&quot;")


if __name__ == "__main__":
    sys.exit(main())
