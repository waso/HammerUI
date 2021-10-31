#!/bin/bash
export JAVA_HOME=/Users/waldek/jdk-16.0.1/
mvn clean compile test assembly:single
rm -rf ./java_runtime
rm -rf ./output
rm -rf ./java_runtime_windows
rm -rf ./output_windows
rm -rf ./HammerUI
rm -rf ./packages
rm -rf ./app-image

mkdir ./output
cp ./target/hammerui-jar-with-dependencies.jar ./output/hammerui.jar

jlink --compress=2 --module-path=/Users/waldek/javafx-jmods-16 --add-modules java.naming,javafx.controls,javafx.fxml,javafx.graphics,javafx.base,java.base,java.prefs,java.logging,java.sql,javafx.web,jdk.crypto.cryptoki  --add-options="-XX:+UseSerialGC -Dorg.apache.commons.logging.Log=com.wsojka.hammerui.log.InMemoryLog -Dorg.apache.commons.logging.simplelog.showdatetime=true -Dorg.apache.commons.logging.simplelog.log.org.apache.http=DEBUG"   --output ./java_runtime

jpackage  --type app-image -n HammerUI -i ./output/ --main-jar hammerui.jar --main-class com.wsojka.hammerui.Main --runtime-image ./java_runtime --dest app-image --description "REST API client"  --vendor "HammerUI" --copyright "Copyright 2021 HammerUI, All rights reserved"  --app-version 1.8

cp -r ./src/main/resources/licenses/ ./app-image/HammerUI/lib

cp -r ./src/main/resources/licenses/ ./output/licenses/

jpackage --type dmg -n HammerUI -i ./output/ --main-jar hammerui.jar --main-class com.wsojka.hammerui.Main --runtime-image ./java_runtime --dest packages  --description "HammerUI"   --vendor "HammerUI" --copyright "Copyright 2021 HammerUI, All rights reserved"  --license-file LICENSE.txt --icon src/main/resources/icons/logo.icns --app-version 1.8

cd ./app-image/
mv HammerUI HammerUI-1.8
tar zvcf ../packages/hammerui-1.8-macos-portable.tar.gz ./HammerUI-1.8/