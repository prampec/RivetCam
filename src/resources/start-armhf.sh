#!/bin/bash

cd "$(dirname "$0")"

export LD_LIBRARY_PATH=armhf
java -jar RivetCam.jar $@