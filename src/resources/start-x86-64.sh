#!/bin/bash

cd "$(dirname "$0")"

export LD_LIBRARY_PATH=x86-64
java -jar RivetCam.jar $@