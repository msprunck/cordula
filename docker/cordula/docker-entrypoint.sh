#!/bin/sh

set -e

# first arg is `-f` or `--some-option`
if [ "${1:0:1}" = '-' ]; then
    set -- /start.sh "$@"
fi

if [ "$1" = 'cordula' ]; then
    shift
    set -- /start.sh "$@"
fi

exec "$@"
