#!/bin/bash

APP_NAME="opencdFree"
#下面的路径，修改为jar包当前路径
APP_HOME="/root/opencd"
JAR_NAME="opencdFree.jar"
CONFIG_FILE="$APP_HOME/application.yml"
PID_FILE="$APP_HOME/$APP_NAME.pid"
LOG_FILE="$APP_HOME/console.log"

JAVA_OPTS="-Xms128m -Xmx256m"

start() {
    if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
        echo "$APP_NAME 已经在运行中 (PID: $(cat $PID_FILE))"
        return
    fi

    echo "启动 $APP_NAME ..."

    nohup java $JAVA_OPTS \
        -jar "$APP_HOME/$JAR_NAME" \
        --config="$CONFIG_FILE" \
        > "$LOG_FILE" 2>&1 &

    echo $! > "$PID_FILE"
    echo "$APP_NAME 启动成功 (PID: $!)"
}

stop() {
    if [ ! -f "$PID_FILE" ]; then
        echo "$APP_NAME 未运行"
        return
    fi

    PID=$(cat "$PID_FILE")
    echo "停止 $APP_NAME (PID: $PID)..."

    kill -15 "$PID"

    for i in {1..10}; do
        if kill -0 "$PID" 2>/dev/null; then
            sleep 1
        else
            break
        fi
    done

    if kill -0 "$PID" 2>/dev/null; then
        echo "进程未正常退出，强制 kill"
        kill -9 "$PID"
    fi

    rm -f "$PID_FILE"
    echo "$APP_NAME 已停止"
}

restart() {
    stop
    sleep 2
    start
}

status() {
    if [ -f "$PID_FILE" ] && kill -0 $(cat "$PID_FILE") 2>/dev/null; then
        echo "$APP_NAME 正在运行 (PID: $(cat $PID_FILE))"
    else
        echo "$APP_NAME 未运行"
    fi
}

case "$1" in
    start)
        start
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        status
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status}"
        exit 1
        ;;
esac
