#!/usr/bin/env bash
#
# 테스트 빠른 실행기 — 긴 ":chapterNN-...:test --tests \"study...\"" 대신 짧게.
#
#   ./t 1                  chapter01 전체 테스트
#   ./t 1 MyArrayList      이름에 'MyArrayList' 포함된 테스트만
#   ./t 1 add_하면         메서드명 부분 일치 (한글 OK)
#   ./t 11 Atomic -w       파일이 바뀔 때마다 자동 재실행 (continuous)
#
# 챕터 번호는 1 / 01 둘 다 됨. 테스트 이름은 부분 문자열(앞뒤 * 자동).
set -euo pipefail

if [ $# -eq 0 ]; then
  sed -n '3,9p' "$0" | sed 's/^# \{0,1\}//'
  exit 0
fi

num="$1"; shift
pad=$(printf '%02d' "$num" 2>/dev/null) || pad="$num"
proj=$(ls -d ./*/chapter"${pad}"-*/ 2>/dev/null | head -1 | xargs -I{} basename {} || true)
if [ -z "$proj" ]; then
  echo "chapter '$num' 를 찾을 수 없습니다. (예: ./t 1, ./t 11)" >&2
  exit 1
fi

watch=""
pattern=""
for a in "$@"; do
  case "$a" in
    -w|--watch) watch="-t" ;;
    *)          pattern="$a" ;;
  esac
done

args=(":${proj}:test")
[ -n "$pattern" ] && args+=(--tests "*${pattern}*")
[ -n "$watch" ] && args+=("$watch")

echo "▶ ./gradlew ${args[*]}"
exec ./gradlew "${args[@]}"
