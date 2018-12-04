#!/bin/bash

cat /proc/meminfo | grep MemFree | cut -c 18-27
