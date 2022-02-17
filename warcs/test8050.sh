#!/bin/bash
echo "into warcprox shell script"
source /app/env/bin/activate
warcprox -b 0.0.0.0 -p 8050 --certs-dir /certs -d /warcs/warcstore8050 -g md5 -v --trace -s 8000000000 --dedup-db-file=/dev/null --stats-db-file=/dev/null
