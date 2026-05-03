#!/bin/bash
# ============================================
# AutoCodeWorkspace 停止脚本
# ============================================

cd "$(dirname "$0")"
PID_FILE="app.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "⚠️  未找到 PID 文件，应用可能未运行"
    exit 0
fi

PID=$(cat "$PID_FILE")

if ! ps -p "$PID" > /dev/null 2>&1; then
    echo "⚠️  进程 $PID 不存在，清理 PID 文件"
    rm -f "$PID_FILE"
    exit 0
fi

echo "🛑 正在停止应用 (PID: $PID)..."
kill "$PID"

# 等待最多 30 秒
for i in $(seq 1 30); do
    if ! ps -p "$PID" > /dev/null 2>&1; then
        echo "✅ 应用已停止"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

echo "⚠️  进程未响应，强制终止..."
kill -9 "$PID"
rm -f "$PID_FILE"
echo "✅ 应用已强制停止"
