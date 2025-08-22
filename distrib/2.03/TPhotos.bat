@echo off

pushd %CD%
cd %~dp0
java -Dfile.encoding=UTF-8 -jar TPhotos.jar
popd
