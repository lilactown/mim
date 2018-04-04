#!/bin/sh

VERSION='1.0.0'
CWD=$(pwd)
TRAMPOLINE_FILE=~/.mim/TRAMPOLINE
OUT_FILE=~/.mim/out
PID_FILE=~/.mim/pid

usage() {
    echo "mim version $VERSION"
    echo ""
    echo "Usage:"
    echo "- mim --version/-v : prints the version"
    echo ""
    echo "- mim --eval '(+ 1 1)' : evaluates a Clojure expression and prints the result"
    echo ""
    echo "- mim --start : starts the server without running any commands"
    echo ""
    echo "- mim --stop : shuts down a running server"
    echo ""
    echo "- mim --pid : prints the process ID of a running server"
    echo ""
    echo "- mim --clean : clean environment if mim quit unexpectedly"
    echo "" 
    echo "- Running a task inside a mim.edn servepfile:"
    echo "  mim <keyword1> <keyword2> ... <keywordN>"
    echo ""
    echo "  The keywords should correspond to a path in a mim.edn file that is"
    echo "  contained in the directory your shell is currently at."
    echo ""
    echo "  An example mim.edn:"
    echo "  {:build (task \"lein uberjar\")"
    echo "   :test (task \"lein test\")"
    echo "   :foo (println \"bar\")"
    echo "   :ls {:home (task \"ls -l\""
    echo "                    :cwd \"~\")}}"
    exit 0
}

version() {
    echo "mim version $VERSION"
    exit 0
}

ensure_started() {
    # pid file tells us whether the server is already started
    if [ ! -e "$PID_FILE" ]; then
        echo "Starting server..."
        nohup java -jar ~/.mim/mim.jar &> ~/.mim/log &
    fi

    # wait until server has started & created pid file
    while [ ! -e "$PID_FILE" ]; do
        sleep 1
    done
}

start() {
    ensure_started
    echo "Server started."
    EXIT_CODE=0
}

pid() {
    if [ -e ~/.mim/pid ]; then
        cat ~/.mim/pid
        EXIT_CODE=0
    else
        echo "No server running."
        EXIT_CODE=1
    fi
}

clean() {
    rm $OUT_FILE
    rm $TRAMPOLINE_FILE
    rm $PID_FILE
}

send_from_edn() {
    ensure_started
    echo "{:cwd \"$CWD\"\
           :command :from-edn\
           :args \"$@\"\
           :version \"$VERSION\"}" | nc localhost 1234 2>&1 | tee $OUT_FILE
}

send_stop() {
    echo "Stopping server..."
    echo "{:command :stop\
           :version \"$VERSION\"}" | nc localhost 1234 2>&1 | tee $OUT_FILE
}

send_form() {
    ensure_started
    echo "{:command :eval\
           :version \"$VERSION\"\
           :form ${@:2}}" | nc localhost 1234 2>&1 | tee $OUT_FILE
}

if [ -z "$1" ]; then
    usage
    exit 1
else
    COMMAND=$1
fi

EXIT_CODE=0


case $COMMAND in
    "--version"|"-v") version;;
    "--start") start;;
    "--stop") send_stop;;
    "--pid") pid;;
    "--eval") send_form $@;;
    "--clean") clean;;
    # assume it's a path in the mim.edn, send it to the server
    *) send_from_edn $@;;
esac

if [ -r "$OUT_FILE" ]; then
    # once a command completes, we expect it's last line to be
    # ':mim/exit <exit code>'
    OUT_FILE_TAIL=$(tail -1 $OUT_FILE)
    rm $OUT_FILE

    # awk '{print $1}' gets the first column in the output
    FINAL_SAY=$(echo $OUT_FILE_TAIL | awk '{print $1}')

    if [ "$FINAL_SAY" = ":mim/exit" ]; then
        # set the exit code to be the second column
        EXIT_CODE=$(echo $OUT_FILE_TAIL | awk '{print $2}')
    fi
fi

if [ "$EXIT_CODE" -gt 0 ]; then
    exit $EXIT_CODE
fi

if [ -r "$TRAMPOLINE_FILE" ]; then
    TRAMPOLINE="$(cat $TRAMPOLINE_FILE)"
    rm $TRAMPOLINE_FILE
    exec sh -c "exec $TRAMPOLINE"
fi
