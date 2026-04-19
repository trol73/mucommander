VERSION='1.0.0'

JAVA_HOME_MAC_X64=./tools/jdk21-macos-x64/Contents/Home/bin/
JAVA_HOME_WINDOWS=./tools/jdk21-windows/bin

OUT_PATH=dist


TMP_OUT_PATH_MAC=dist/macos/jar
TMP_OUT_PATH_WIN=dist/windows/jar
TMP_RES_PATH=dist/resources
OUT_PATH=dist
#ARCH=$(uname -m)

MAIN_CLASS="com.mucommander.TrolCommander"
CLASS_LOADER="com.mucommander.commons.file.AbstractFileClassLoader"
APP_NAME="trolCommander"

set -e

./gradlew clean build

mkdir -p $TMP_OUT_PATH_MAC
mkdir -p $TMP_OUT_PATH_WIN
mkdir -p $TMP_RES_PATH

# ------- macos jar -------------
cp build/libs/trolcommander-$VERSION.jar $TMP_OUT_PATH_MAC/trolcommander-macosx.jar

zip -d $TMP_OUT_PATH_MAC/trolcommander-macosx.jar "Windows-amd64/*" "Windows-x86/*" "Linux-amd64/*" "Linux-i386/*" "win/*" "linux/*" "jtermios/freebsd/*" "jtermios/linux/*" "jtermios/solaris/*" "jtermios/windows/*"
zip -d $TMP_OUT_PATH_MAC/trolcommander-macosx.jar "com/sun/jna/freebsd-amd64/*" "com/sun/jna/freebsd-i386/*" "com/sun/jna/linux-amd64/*" "com/sun/jna/linux-arm/*" "com/sun/jna/linux-i386/*" "com/sun/jna/linux-ia64/*" "com/sun/jna/linux-ppc/*" "com/sun/jna/linux-ppc64/*" "com/sun/jna/platform/win32/*" "com/sun/jna/platform/wince/*" "com/sun/jna/sunos-amd64/*" "com/sun/jna/sunos-sparc/*" "com/sun/jna/sunos-sparcv9/*" "com/sun/jna/sunos-x86/*" "com/sun/jna/w32ce-arm/*" "com/sun/jna/win32/*" "com/sun/jna/win32-amd64/*" "com/sun/jna/win32-x86/*"

# -------- Windows jar ----------
cp build/libs/trolcommander-$VERSION.jar $TMP_OUT_PATH_WIN/trolcommander-windows.jar
zip -d $TMP_OUT_PATH_WIN/trolcommander-windows.jar "Mac-arm64/*" "Mac-x86_64/*"  "jtermios/Mac-x86_64/*" "jtermios/solaris/*" 
zip -d $TMP_OUT_PATH_WIN/trolcommander-windows.jar "jtermios/freebsd/*" "com/sun/jna/freebsd-amd64/*" "com/sun/jna/freebsd-i386/*" "com/sun/jna/platform/wince/*" "com/sun/jna/sunos-amd64/*" "com/sun/jna/sunos-sparc/*" "com/sun/jna/sunos-sparcv9/*" "com/sun/jna/sunos-x86/*" "com/sun/jna/w32ce-arm/*" 
zip -d $TMP_OUT_PATH_WIN/trolcommander-windows.jar "Linux-amd64/*" "Linux-i386/*" "linux/*" "jtermios/freebsd/*" "jtermios/linux/*" "jtermios/solaris/*" 
zip -d $TMP_OUT_PATH_WIN/trolcommander-windows.jar  "com/sun/jna/linux-amd64/*" "com/sun/jna/linux-arm/*" "com/sun/jna/linux-i386/*" "com/sun/jna/linux-ia64/*" "com/sun/jna/linux-ppc/*" "com/sun/jna/linux-ppc64/*" "com/sun/jna/platform/wince/*" "com/sun/jna/sunos-amd64/*" "com/sun/jna/sunos-sparc/*" "com/sun/jna/sunos-sparcv9/*" "com/sun/jna/sunos-x86/*"


JVM_OPENS="--add-opens java.desktop/javax.swing.plaf.basic=ALL-UNNAMED\
 --add-opens java.base/java.io=ALL-UNNAMED\
 --add-opens java.base/java.net=ALL-UNNAMED\
 --add-opens java.transaction.xa/javax.transaction.xa=ALL-UNNAMED\
 --add-opens java.management/javax.management=ALL-UNNAMED\
 --add-opens java.rmi/java.rmi=ALL-UNNAMED\
 --add-opens java.security.jgss/org.ietf.jgss=ALL-UNNAMED\
 --add-opens java.sql/java.sql=ALL-UNNAMED\
 --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED\
 --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED\
 --add-opens java.compiler/javax.lang.model.element=ALL-UNNAMED"

JVM_OPENS_APPLE="--add-opens java.desktop/com.apple.eawt=ALL-UNNAMED\
 --add-opens java.desktop/com.apple.laf=ALL-UNNAMED\
 --add-opens java.desktop/com.apple.eio=ALL-UNNAMED\
 --add-opens java.desktop/com.apple.laf.AquaLookAndFeel=ALL-UNNAMED"

JVM_PARAMS="-Xmx128m -Xms128m -Dfile.encoding=UTF-8 -XX:ReservedCodeCacheSize=64m -XX:+IgnoreUnrecognizedVMOptions"
JVM_LOGING="-Dslf4j.provider=ch.qos.logback.classic.spi.LogbackServiceProvider"
JVM_OPTS="$JVM_OPENS $JVM_OPENS_APPLE $JVM_PARAMS $JVM_LOGING -Djava.system.class.loader=$CLASS_LOADER -Dlog.stdout=false"


# ------------ shadow

cp build/distributions/trolCommander-$VERSION.zip $OUT_PATH/

# ---------- macos -------------------

cp res/package/osx/icon.icns $TMP_RES_PATH/trolCommander.icns

ARCH="arm64" 

jpackage --input "$TMP_OUT_PATH_MAC/" \
         --name $APP_NAME \
         --app-version $VERSION \
         --main-jar trolcommander-macosx.jar \
         --main-class $MAIN_CLASS \
         --resource-dir "$TMP_RES_PATH" \
         --java-options "$JVM_OPTS" \
         --type app-image \
         --dest "${OUT_PATH}/macos-$ARCH"

jpackage --input "$TMP_OUT_PATH_MAC/" \
         --name $APP_NAME \
         --app-version $VERSION \
         --main-jar trolcommander-macosx.jar \
         --main-class $MAIN_CLASS \
         --resource-dir "$TMP_RES_PATH" \
         --java-options "$JVM_OPTS" \
         --type dmg \
         --dest $OUT_PATH

mv $OUT_PATH/trolCommander-$VERSION.dmg $OUT_PATH/trolCommander-$ARCH-$VERSION.dmg

ARCH="x64"

$JAVA_HOME_MAC_X64/jpackage --input "$TMP_OUT_PATH_MAC/" \
         --name $APP_NAME \
         --app-version $VERSION \
         --main-jar trolcommander-macosx.jar \
         --main-class $MAIN_CLASS \
         --resource-dir "$TMP_RES_PATH" \
         --java-options "$JVM_OPTS" \
         --type app-image \
         --dest "${OUT_PATH}/macos-$ARCH"

$JAVA_HOME_MAC_X64/jpackage --input "$TMP_OUT_PATH_MAC/" \
         --name $APP_NAME \
         --app-version $VERSION \
         --main-jar trolcommander-macosx.jar \
         --main-class $MAIN_CLASS \
         --resource-dir "$TMP_RES_PATH" \
         --java-options "$JVM_OPTS" \
         --type dmg \
         --dest $OUT_PATH
                

mv $OUT_PATH/trolCommander-$VERSION.dmg $OUT_PATH/trolCommander-$ARCH-$VERSION.dmg

rm $TMP_RES_PATH/trolCommander.icns
mv $TMP_OUT_PATH_MAC/trolcommander-macosx.jar $OUT_PATH/trolcommander-macosx.jar
rmdir $TMP_RES_PATH
rmdir $TMP_OUT_PATH_MAC
rmdir dist/macos


# ----------- windows

JVM_OPTS="$JVM_OPENS $JVM_PARAMS $JVM_LOGING -Djava.system.class.loader=$CLASS_LOADER"


wine $JAVA_HOME_WINDOWS/jpackage.exe \
         --type app-image \
         --input "$TMP_OUT_PATH_WIN/" \
         --name $APP_NAME \
         --app-version $VERSION \
         --main-jar trolcommander-windows.jar \
         --main-class $MAIN_CLASS \
         --java-options "$JVM_OPTS" \
         --resource-dir "package/windows" \
         --dest ${OUT_PATH}
cd $OUT_PATH
zip -rm trolCommander-windows-${VERSION}.zip trolCommander
cd ..


mv $TMP_OUT_PATH_WIN/trolcommander-windows.jar $OUT_PATH/trolcommander-windows.jar
rmdir $TMP_OUT_PATH_WIN
rmdir dist/windows

