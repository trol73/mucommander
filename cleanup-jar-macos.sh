mkdir -p dist/mac
mkdir -p dist/resources

cp dist/trolcommander.jar dist/mac/trolcommander-macosx.jar
zip -d dist/mac/trolcommander-macosx.jar "Windows-amd64/*" "Windows-x86/*" "Linux-amd64/*" "Linux-i386/*" "win/*" "linux/*" "jtermios/freebsd/*" "jtermios/linux/*" "jtermios/solaris/*" "jtermios/windows/*"
zip -d dist/mac/trolcommander-macosx.jar "com/sun/jna/freebsd-amd64/*" "com/sun/jna/freebsd-i386/*" "com/sun/jna/linux-amd64/*" "com/sun/jna/linux-arm/*" "com/sun/jna/linux-i386/*" "com/sun/jna/linux-ia64/*" "com/sun/jna/linux-ppc/*" "com/sun/jna/linux-ppc64/*" "com/sun/jna/platform/win32/*" "com/sun/jna/platform/wince/*" "com/sun/jna/platform/unix/*" "com/sun/jna/sunos-amd64/*" "com/sun/jna/sunos-sparc/*" "com/sun/jna/sunos-sparcv9/*" "com/sun/jna/sunos-x86/*" "com/sun/jna/w32ce-arm/*" "com/sun/jna/win32/*" "com/sun/jna/win32-amd64/*" "com/sun/jna/win32-x86/*"

export JVM_OPENS="--add-opens java.desktop/com.apple.eawt=ALL-UNNAMED --add-opens java.desktop/com.apple.laf=ALL-UNNAMED --add-opens java.desktop/com.apple.eio=ALL-UNNAMED"
export JVM_PARAMS="-Xmx128m -Xms128m -Dfile.encoding=UTF-8 -XX:ReservedCodeCacheSize=64m -XX:+IgnoreUnrecognizedVMOptions"
export JVM_APPLE="-Dcom.apple.smallTabs=true -Dcom.apple.hwaccel -Dapple.laf.useScreenMenuBar=true -Xdock:name=trolCommander"
export JVM_OPTS="$JVM_OPENS $JVM_PARAMS $JVM_APPLE -Djava.system.class.loader=com.mucommander.commons.file.AbstractFileClassLoader"

		
export OUT_PATH=dist-macos
export VERSION=1.0


cp res/package/osx/icon.icns dist/resources/trolCommander.icns

jpackage --input 'dist/mac/' \
                --name trolCommander \
                --app-version $VERSION \
                --main-jar trolcommander-macosx.jar \
                --main-class com.mucommander.TrolCommander \
                --resource-dir dist/resources \
                --java-options "$JVM_OPTS" \
                --type app-image \
                --dest $OUT_PATH

jpackage --input 'dist/mac/' \
                --name trolCommander \
                --main-jar trolcommander-macosx.jar \
                --main-class com.mucommander.TrolCommander \
                --resource-dir dist/resources \
                --java-options "$JVM_OPTS" \
                --type dmg \
                --dest $OUT_PATH
                
#                --runtime-image 'jre/linux' \

cp res/package/osx/icon.icns $OUT_PATH/trolCommander.app/Contents/Resources/trolCommander.icns



