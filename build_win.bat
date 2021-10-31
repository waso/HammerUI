@echo on
set JAVA_HOME=z:\jdk-16.0.1
call mvn clean compile -DskipTests assembly:single
del /Q /S java_runtime
del /Q /S output
del /Q /S HammerUI
del /Q /S packages
del /Q /S app-image
rmdir /Q /S java_runtime
rmdir /Q /S output
rmdir /Q /S HammerUI
rmdir /Q /S packages
rmdir /Q /S app-image
mkdir output
copy target\hammerui-jar-with-dependencies.jar output\hammerui.jar
call jlink -v --module-path z:\javafx-jmods-16   --add-modules java.naming,javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.base,java.prefs,java.logging,java.sql,javafx.web,jdk.crypto.cryptoki     --add-options "-XX:+UseSerialGC -Dorg.apache.commons.logging.Log=com.wsojka.hammerui.log.InMemoryLog -Dorg.apache.commons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG"   --output ./java_runtime
call jpackage --verbose  --type app-image -n HammerUI   -i output   --main-jar hammerui.jar   --main-class com.wsojka.hammerui.Main   --runtime-image ./java_runtime   --dest app-image   --description "REST API client"    --vendor "HammerUI"   --copyright "Copyright 2021 HammerUI, All rights reserved" --app-version 1.8
xcopy /I z:\hammerui\src\main\resources\licenses z:\hammerui\app-image\HammerUI\app\licenses
xcopy /I z:\hammerui\src\main\resources\licenses z:\hammerui\output\licenses
call jpackage --verbose  --type msi -n HammerUI   -i output   --main-jar hammerui.jar   --main-class com.wsojka.hammerui.Main   --runtime-image ./java_runtime   --dest packages   --description "REST API client"    --vendor "HammerUI"   --copyright "Copyright 2021 HammerUI, All rights reserved"   --license-file LICENSE.txt  --icon src/main/resources/icons/logo.ico  --win-menu --win-menu-group "HammerUI" --app-version 1.8