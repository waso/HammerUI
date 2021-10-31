HammerUI is a REST API client application.
It is a standalone JavaFX application that doesn't require Java to be installed on PC to run.
Java runtime is embedded in the executable.
Java and JavaFX version is 16.

![app](https://hammerui.com/img/hammerui_screen.png)

Building overview
----------------------
There are 3 scripts available to build distribution packages, one for Linux, Windows and MacOS.
Each script also builds app-image, which is "portable" version of HammerUI that can be run without installation.


Building under linux
----------------------
# ./build_linux.sh
# ./app-image/HammerUI/HammerUI


Building under windows
----------------------
# ./build_win.sh
# ./app-image/HammerUI/HammerUI


Building under MacOS
----------------------
# ./build_mac.sh
# ./app-image/HammerUI/HammerUI


Releasing
----------------------
$ release_prepare.sh


Running via IDE
----------------------
Running via IDE/cmd is as simple as running maven command:

$ mvn clean javafx:run


Running via IDE in Debug
----------------------
$ clean javafx:run@debug  (and then attach IDE to remote debugger running locally on 127.0.0.1:8000)
