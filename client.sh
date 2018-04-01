#!/bin/sh

VERSION='1.0.0'
CWD=$(pwd)
TRAMPOLINE_FILE=~/.mim/TRAMPOLINE

usage() {
    echo "mim version $VERSION"
    echo ""
    echo "Usage:"
    echo "- mim version/--version/-v : prints the version"
    echo ""
    echo "- mim eval '(+ 1 1)' : evaluates a Clojure expression and prints the result"
    echo ""
    echo "- mim server : starts the server without running any commands"
    echo ""
    echo "- mim stop : shuts down a running server"
    echo ""
    echo "- mim pid : prints the process ID of a running server"
    echo ""
    echo "- Running a task inside a mim.edn file:"
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
}


ensure_started() {
    # pid file tells us whether the server is already started
    if [ ! -e ~/.mim/pid ]; then
        echo "Starting server..."
        nohup java -jar ~/.mim/mim.jar &> ~/.mim/log &
    fi

    # wait until server has started & created pid file
    while [ ! -e ~/.mim/pid ]; do
        sleep 1
    done
}

start() {
    ensure_started
    echo "Server started"
}

pid() {
    if [ -e ~/.mim/pid ]; then
        cat ~/.mim/pid
    else
        echo "No server running."
    fi
}

send_from_edn() {
    ensure_started
    echo "{:cwd \"$CWD\"\
           :command :from-edn\
           :args \"$@\"\
           :version \"$VERSION\"}" | nc localhost 1234 
}

send_stop() {
    echo "{:command :stop\
           :version \"$VERSION\"}" | nc localhost 1234
}

send_form() {
    ensure_started
    echo "{:command :eval\
           :version \"$VERSION\"\
           :form ${@:2}}" | nc localhost 1234
}

if [ -z "$1" ]; then
    usage
    exit 1
else
    COMMAND=$1
fi

EXIT_CODE=0


# TODO: Capture exit code somehow
case $COMMAND in
    "version"|"--version"|"-v") echo "mim version $VERSION";;
    "server") start;;
    "stop") send_stop;;
    "pid") pid;;
    "eval") send_form $@;;
    # assume it's a path in the mim.edn, send it to the server
    *) send_from_edn $@;;
esac

if [ "$EXIT_CODE" -gt 0 ]; then
    exit $EXIT_CODE;
fi

if [ -r "$TRAMPOLINE_FILE" ]; then
    TRAMPOLINE="$(cat $TRAMPOLINE_FILE)"
    rm $TRAMPOLINE_FILE
    exec sh -c "exec $TRAMPOLINE"
fi
