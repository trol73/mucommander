VERSION='1.0.0'


TMP_OUT_PATH=dist/macos/jar
TMP_RES_PATH=dist/resources
OUT_PATH=dist
ARCH=$(uname -m)

./gradlew clean build

mkdir -p $TMP_OUT_PATH
mkdir -p $TMP_RES_PATH

cp build/libs/trolcommander-$VERSION.jar $TMP_OUT_PATH/trolcommander-macosx.jar
#cp dist/trolcommander.jar dist/mac/trolcommander-macosx.jar
zip -d $TMP_OUT_PATH/trolcommander-macosx.jar "Windows-amd64/*" "Windows-x86/*" "Linux-amd64/*" "Linux-i386/*" "win/*" "linux/*" "jtermios/freebsd/*" "jtermios/linux/*" "jtermios/solaris/*" "jtermios/windows/*"
zip -d $TMP_OUT_PATH/trolcommander-macosx.jar "com/sun/jna/freebsd-amd64/*" "com/sun/jna/freebsd-i386/*" "com/sun/jna/linux-amd64/*" "com/sun/jna/linux-arm/*" "com/sun/jna/linux-i386/*" "com/sun/jna/linux-ia64/*" "com/sun/jna/linux-ppc/*" "com/sun/jna/linux-ppc64/*" "com/sun/jna/platform/win32/*" "com/sun/jna/platform/wince/*" "com/sun/jna/sunos-amd64/*" "com/sun/jna/sunos-sparc/*" "com/sun/jna/sunos-sparcv9/*" "com/sun/jna/sunos-x86/*" "com/sun/jna/w32ce-arm/*" "com/sun/jna/win32/*" "com/sun/jna/win32-amd64/*" "com/sun/jna/win32-x86/*"

export JVM_OPENS="--add-opens java.desktop/com.apple.eawt=ALL-UNNAMED --add-opens java.desktop/com.apple.laf=ALL-UNNAMED --add-opens java.desktop/com.apple.eio=ALL-UNNAMED --add-opens java.desktop/com.apple.laf.AquaLookAndFeel=ALL-UNNAMED"
export JVM_PARAMS="-Xmx128m -Xms128m -Dfile.encoding=UTF-8 -XX:ReservedCodeCacheSize=64m -XX:+IgnoreUnrecognizedVMOptions"
export JVM_APPLE="-Dcom.apple.smallTabs=true -Dcom.apple.hwaccel -Dapple.laf.useScreenMenuBar=true -Xdock:name=trolCommander"
export JVM_LOGING="-Dslf4j.provider=ch.qos.logback.classic.spi.LogbackServiceProvider"
export JVM_OPTS="$JVM_OPENS $JVM_PARAMS $JVM_APPLE $JVM_LOGING -Djava.system.class.loader=com.mucommander.commons.file.AbstractFileClassLoader"
		



cp res/package/osx/icon.icns $TMP_RES_PATH/trolCommander.icns

jpackage --input "$TMP_OUT_PATH/" \
         --name trolCommander \
         --app-version $VERSION \
         --main-jar trolcommander-macosx.jar \
         --main-class com.mucommander.TrolCommander \
         --resource-dir "$TMP_RES_PATH" \
         --java-options "$JVM_OPTS" \
         --type app-image \
         --dest "${OUT_PATH}/macos-$ARCH"

jpackage --input "$TMP_OUT_PATH/" \
         --name "trolCommander" \
         --app-version $VERSION \
         --main-jar trolcommander-macosx.jar \
         --main-class com.mucommander.TrolCommander \
         --resource-dir "$TMP_RES_PATH" \
         --java-options "$JVM_OPTS" \
         --type dmg \
         --dest $OUT_PATH
                
#                --runtime-image 'jre/linux' \
mv $OUT_PATH/trolCommander-$VERSION.dmg $OUT_PATH/trolCommander-$ARCH-$VERSION.dmg

rm $TMP_RES_PATH/trolCommander.icns
mv $TMP_OUT_PATH/trolcommander-macosx.jar $OUT_PATH/trolcommander-macosx.jar
rmdir $TMP_RES_PATH
rmdir $TMP_OUT_PATH
rmdir dist/macos
