#!/usr/bin/dumb-init /bin/sh

set -m

/usr/local/bin/docker-entrypoint.sh server -dev &

/vault/init-dev.sh

fg %1
