VERSION='1.0.0'


TMP_OUT_PATH=dist
#TMP_RES_PATH=dist/resources
OUT_PATH=dist
ARCH=$(uname -m)

set -e

./gradlew clean build

mkdir -p $TMP_OUT_PATH
#mkdir -p $TMP_RES_PATH

cp build/libs/trolcommander-$VERSION.jar $TMP_OUT_PATH/trolcommander-linux.jar
zip -d $TMP_OUT_PATH/trolcommander-linux.jar "Windows-amd64/*" "Windows-x86/*" "Mac-arm64/*" "Mac-x86_64/*" "win/*" "jtermios/freebsd/*" "jtermios/Mac-x86_64/*" "jtermios/solaris/*" "jtermios/windows/*"
zip -d $TMP_OUT_PATH/trolcommander-linux.jar "com/sun/jna/freebsd-amd64/*" "com/sun/jna/freebsd-i386/*" "com/sun/jna/platform/win32/*" "com/sun/jna/platform/wince/*" "com/sun/jna/sunos-amd64/*" "com/sun/jna/sunos-sparc/*" "com/sun/jna/sunos-sparcv9/*" "com/sun/jna/sunos-x86/*" "com/sun/jna/w32ce-arm/*" "com/sun/jna/win32/*" "com/sun/jna/win32-amd64/*" "com/sun/jna/win32-x86/*"

export JVM_OPENS="--add-opens java.desktop/com.apple.eawt=ALL-UNNAMED --add-opens java.desktop/com.apple.laf=ALL-UNNAMED --add-opens java.desktop/com.apple.eio=ALL-UNNAMED --add-opens java.desktop/com.apple.laf.AquaLookAndFeel=ALL-UNNAMED"
export JVM_PARAMS="-Xmx128m -Xms128m -Dfile.encoding=UTF-8 -XX:ReservedCodeCacheSize=64m -XX:+IgnoreUnrecognizedVMOptions"
export JVM_LOGING="-Dslf4j.provider=ch.qos.logback.classic.spi.LogbackServiceProvider"
export JVM_OPTS="$JVM_OPENS $JVM_PARAMS $JVM_LOGING -Djava.system.class.loader=com.mucommander.commons.file.AbstractFileClassLoader"
		



#cp res/package/osx/icon.icns $TMP_RES_PATH/trolCommander.icns

jpackage --input "$TMP_OUT_PATH/" \
         --name trolCommander \
         --app-version $VERSION \
         --main-jar trolcommander-linux.jar \
         --main-class com.mucommander.TrolCommander \
         --resource-dir "$TMP_RES_PATH" \
         --java-options "$JVM_OPTS" \
         --type rpm \
         --dest "${OUT_PATH}"

jpackage --input "$TMP_OUT_PATH/" \
         --name trolCommander \
         --app-version $VERSION \
         --main-jar trolcommander-linux.jar \
         --main-class com.mucommander.TrolCommander \
         --resource-dir "$TMP_RES_PATH" \
         --java-options "$JVM_OPTS" \
         --type deb \
         --dest $OUT_PATH
                
#                --runtime-image 'jre/linux' \
#mv $OUT_PATH/trolCommander-$VERSION.dmg $OUT_PATH/trolCommander-$ARCH-$VERSION.dmg

#rm $TMP_RES_PATH/trolCommander.icns
#mv $TMP_OUT_PATH/trolcommander-lunux.jar $OUT_PATH/trolcommander-linux.jar
#rmdir $TMP_RES_PATH
#rmdir $TMP_OUT_PATH
#rmdir dist/macos
