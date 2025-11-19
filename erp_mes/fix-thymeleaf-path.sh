#!/bin/bash

TARGET_DIR="src/main/java"

echo "🔍 Thymeleaf view 경로 자동 수정 시작..."

# 백업
BACKUP_NAME="java_backup_$(date +%Y%m%d_%H%M%S).tar.gz"
tar -czf $BACKUP_NAME $TARGET_DIR
echo "📦 백업 생성: $BACKUP_NAME"

# 치환 수행
find $TARGET_DIR -type f -name "*.java" | while read file; do
    # return "/xxxx";
    sed -i 's/return\s*"\//return "/g' "$file"
done

echo "✅ 치환 완료!"
grep -R 'return "/' -n $TARGET_DIR || echo "👌 모든 return 경로 정상 처리됨."
