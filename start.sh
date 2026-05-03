#!/bin/bash
# ============================================
# AutoCodeWorkspace 启动脚本
# ============================================

set -e

# ---------- 环境变量 ----------
export DEEPSEEK_API_KEY="sk-your-deepseek-api-key"
export FEISHU_APP_ID=""
export FEISHU_APP_SECRET=""
export SERVERCHAN_SENDKEY=""
export NOTIFYME_UUID=""
export ROCOM_API_KEY=""
export IMGBB_API_KEY=""

# ---------- JVM 参数 ----------
JAVA_OPTS="-Xms256m -Xmx512m \
  -Dfile.encoding=UTF-8 \
  -Djava.awt.headless=true"

# ---------- JAR 路径 ----------
JAR_FILE="./trading-application-1.0.0-SNAPSHOT.jar"
LOG_FILE="app.log"
PID_FILE="app.pid"

cd "$(dirname "$0")"

# ---------- 检查 JAR 是否存在 ----------
if [ ! -f "$JAR_FILE" ]; then
    echo "❌ 找不到 JAR 文件: $JAR_FILE"
    echo "   请先执行: mvn package -DskipTests"
    exit 1
fi

# ---------- 检查是否已运行 ----------
if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    if ps -p "$PID" > /dev/null 2>&1; then
        echo "⚠️  应用已在运行 (PID: $PID)"
        exit 1
    else
        rm -f "$PID_FILE"
    fi
fi

# ---------- 启动 ----------
echo "🚀 正在启动应用..."
nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

sleep 2

if ps -p "$(cat $PID_FILE)" > /dev/null 2>&1; then
    echo "✅ 应用启动成功 (PID: $(cat $PID_FILE))"
    echo "   日志文件: $LOG_FILE"
    echo "   查看日志: tail -f $LOG_FILE"
else
    echo "❌ 应用启动失败，请查看日志: $LOG_FILE"
    rm -f "$PID_FILE"
    exit 1
fi
