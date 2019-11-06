#!/bin/bash

cd "$(dirname "$0")"

ln -sf armhf/libvideo.so armhf/libvideo.so.0

export LD_LIBRARY_PATH=armhf
java -jar RivetCam.jar $@