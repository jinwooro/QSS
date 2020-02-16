cd src
javac -d ../build -cp ".:../lib/*" SimQSS.java

cd ..
cp manifest.txt build/manifest.txt

cd build
jar cfvm Sim2HIOA.jar manifest.txt SimQSS.class adapter/*.class structure/*.class

cd ..
rm -rf bin
mkdir bin
mv build/Sim2HIOA.jar bin/
rm -rf build

