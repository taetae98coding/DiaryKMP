#!/usr/bin/env bash
# 배포하지 않은 마이그레이션까지 적용한 상태에서 public 스키마의 PL/pgSQL 함수를 plpgsql_check로 검사한다.
# PL/pgSQL 본문은 함수를 만들 때가 아니라 처음 실행할 때 검사되므로, 이 검사가 없으면 모호한 이름 같은 오류가 배포 뒤 첫 호출에서야 드러난다.
# 대상 프로젝트에 트랜잭션을 열어 확장 설치, 미배포 마이그레이션 적용, 검사를 한 뒤 롤백하므로 남는 변경은 없다.
#
# 사용법: supabase/scripts/plpgsql-check.sh [project-ref]   기본값은 dev 프로젝트다.
set -euo pipefail

project_ref="${1:-ljsscxmqzsoulswmwsia}"
repository_root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$repository_root"

query_json() {
  supabase db query --linked --project-ref "$project_ref" "$@" -o json 2>/dev/null
}

applied_versions="$(
  query_json "select version from supabase_migrations.schema_migrations" |
    python3 -c 'import json, sys; print("\n".join(row["version"] for row in json.load(sys.stdin)["rows"]))'
)"

check_sql="$(mktemp)"
trap 'rm -f "$check_sql"' EXIT

{
  echo "begin;"
  echo "create extension if not exists plpgsql_check;"
  for migration in supabase/migrations/*.sql; do
    version="$(basename "$migration" | cut -d_ -f1)"
    if ! grep -qx "$version" <<< "$applied_versions"; then
      echo "-- $migration" >&2
      cat "$migration"
      echo
    fi
  done
  cat <<'SQL'
select coalesce(json_agg(problem), '[]'::json) as problems
from (
    select p.proname as function_name, c.lineno as line, c.sqlstate, c.level, c.message
    from pg_proc p
    join pg_namespace n on n.oid = p.pronamespace
    join pg_language l on l.oid = p.prolang
    cross join lateral plpgsql_check_function_tb(p.oid) c
    where n.nspname = 'public'
        and l.lanname = 'plpgsql'
        and p.prorettype <> 'trigger'::regtype
        and c.level in ('error', 'warning')
        -- 배열 변수로 테이블 이름을 넘기는 동적 SQL을 실제 테이블 이름으로 읽지 못하는 오탐이다.
        and not (p.proname = 'purge_deleted_entities' and c.sqlstate = '42P01')
) problem;
rollback;
SQL
} > "$check_sql"

echo "plpgsql_check: $project_ref" >&2

if ! result="$(query_json -f "$check_sql")"; then
  echo "검사 SQL을 실행하지 못했다. 미배포 마이그레이션이 트랜잭션 안에서 실패했을 수 있다." >&2
  supabase db query --linked --project-ref "$project_ref" -f "$check_sql" >&2 || true
  exit 1
fi

python3 - "$result" <<'PY'
import json
import sys

problems = json.loads(sys.argv[1])["rows"][-1]["problems"]
for problem in problems:
    print(f"{problem['level']} {problem['sqlstate']} {problem['function_name']}:{problem['line']} {problem['message']}")
if problems:
    sys.exit(1)
print("문제 없음")
PY
