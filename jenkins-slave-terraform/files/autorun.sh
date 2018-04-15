#!/bin/bash

#Start the first process

collectd &
status=$?
if [ $status -ne 0 ]; then
        echo "Failed to start collecd: $status"
        exit $status
fi

/usr/sbin/sshd &
status=$?
if [ $status -ne 0 ]; then
        echo "Failed to start collecd: $status"
        exit $status
fi

node /usr/local/src/statsd/stats.js /usr/local/src/statsd/config.js
status=$?

if [ $status -ne 0 ]; then
	echo "Failed to start statsd: $status"
	exit $status
fi
