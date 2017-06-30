RivetCam depends on v4l (video4linux), and v4l4j (video4linux4java).
Unfortunately v4l4j is not maintained any more but I wouldn't say, that is is obsolete.
But for this reason the v4l4j jar is bundled to this application.

v4l4j also needs native libraries, that can be compiled from the sources. Some native libraries
are attached to the application.
libv4l4j.so and libvideo.so.0 needs to be in the **LD_LIBRARY_PATH** environment variable,
when running the RivetCam application.