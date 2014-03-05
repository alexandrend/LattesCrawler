#!/bin/bash

# javac -cp .;lib/* Main.java
# java -cp .;lib/* Main
# -classpath -cp

rm -r bin
mkdir bin
javac -cp .:lib/jsoup-1.7.3.jar -d bin src/br/ufpb/ci/labsna/lattescrawler/*.java
cd bin
java -cp .:../lib/jsoup-1.7.3.jar br.ufpb.ci.labsna.lattescrawler.LattesCrawler ../codelattes.txt 1 > ../result.gml
