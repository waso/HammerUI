#!/bin/bash
rm -rf ./release_1_8/
mkdir ./release_1_8/
mkdir ./release_1_8/download
mkdir ./release_1_8/download/win
mkdir ./release_1_8/download/linux
cp /home/waldek/git/hammerui/packages/hammerui-1.8-linux-portable.tar.gz ./release_1_8/download/linux/hammerui-1.8-linux-portable.tar.gz
cp /home/waldek/git/hammerui/packages/hammerui-1.8v-linux.deb ./release_1_8/download/linux/hammerui-1.8v-linux.deb
cd /home/waldek/Shared/hammerui/app-image/
mv HammerUI HammerUI-1.8
zip -r ./hammerui-1.8-win64-portable.zip ./HammerUI-1.8/
cd -
cp /home/waldek/Shared/hammerui/app-image/hammerui-1.8-win64-portable.zip ./release_1_8/download/win/
cp /home/waldek/Shared/hammerui/packages/HammerUI-1.8.msi ./release_1_8/download/win/hammerui-1.8-win64.msi
