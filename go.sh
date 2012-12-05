#!/bin/bash
if [ -f "RUNNING_PID" ]; then
    echo "will not start play because either RUNNING_PID file exists"
else
    if [ -f "NO_GUARD" ]; then
        rm  NO_GUARD
    fi
    while true ; do
        if [ -f "RUNNING_PID" -o -f "NO_GUARD" ]; then
            echo "will not start play because either RUNNING_PID or NO_GUARD file exists"
            break
        fi

        echo "starting play server"
        play "start 80" &> res.out

        if [ $? -ne 0 ]; then
            echo "something wrong when trying to run play"
            cat res.out
            if [ -f "RUNNING_PID" ]; then
                echo "stopping play"
                play stop
            fi
            echo "exiting"
            break
        fi

    done

fi