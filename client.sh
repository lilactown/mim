#!/bin/sh

VERSION='1.0.0'
CWD=$(pwd)
TRAMPOLINE_FILE=~/.mim/TRAMPOLINE


usage() {
    echo "mim version $VERSION"
    echo ""
    echo "Usage:"
    echo "mim <keyword1> <keyword2>"
    echo ""
    echo "The keywords should correspond to a path in a mim.edn file that is"
    echo "contained in the directory your shell is currently at."
    echo ""
    echo "An example mim.edn:"
    echo "{:build (task \"lein uberjar\")"
    echo " :test (task \"lein test\")"
    echo " :foo (println \"bar\")"
    echo " :ls {:home (task \"ls -l\""
    echo "                  :cwd \"~\")}}"
}


send_from_config() {
    echo "{:cwd \"$CWD\"\
           :command :from-edn\
           :args \"$@\"\
           :version \"$VERSION\"}" | nc localhost 1234    
}


if [ -z "$1" ]; then
    usage
    exit 1
fi

if [ "$1" = "--version" ]; then
    echo "mim version $VERSION"
    exit 0
fi

if [ "$1" = "stop" ]; then
    echo "{:command :stop\
           :version \"$VERSION\"}" | nc localhost 1234
else
    send_from_config $@
fi



if [ -r "$TRAMPOLINE_FILE" ]; then
    TRAMPOLINE="$(cat $TRAMPOLINE_FILE)"
    rm $TRAMPOLINE_FILE
    exec sh -c "exec $TRAMPOLINE"
fi
