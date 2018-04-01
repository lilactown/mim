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


send_payload() {
    echo "{:cwd \"$CWD\"\
           :mode :from-config
           :args \"$@\"\
           :version \"$VERSION\"}" | nc localhost 1234    
}


if [ -z "$1" ]; then
    usage
    exit 1
fi

if [ "$1" -eq "--version" ]; then
    echo "mim version $VERSION"
    exit 0
fi

send_payload $@

if [ -r "$TRAMPOLINE_FILE" ]; then
    TRAMPOLINE="$(cat $TRAMPOLINE_FILE)"
    rm $TRAMPOLINE_FILE
    exec sh -c "exec $TRAMPOLINE"
fi


exit $EXIT_CODE
