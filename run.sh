#!/usr/bin/env sh
set -eu
mkdir -p out
javac -encoding UTF-8 -d out src/main/java/com/codemunchers/pacman/App.java
java -cp out com.codemunchers.pacman.App
