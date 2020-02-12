cd src
javac -d ../build -cp ".:../lib/*" SimQSS.java

cd ..
cp manifest.txt build/manifest.txt

cd build
jar cfvm Sim2HIOA.jar manifest.txt


