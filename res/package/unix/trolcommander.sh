#! /bin/sh

TROLCOMMANDER_ARGS="@ARGS@"
JAVA_ARGS="@JAVA_ARGS@"

# Locates the java executable.
if [ "$JAVA_HOME" != "" ] ; then
    JAVA=$JAVA_HOME/bin/java
else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        echo "Error: cannot find java VM."
        exit 1
    else
        JAVA=java
    fi
fi

# Resolve the path to the trolcommander.jar located in the same directory as this script
if [ -h $0 ]
then
    # This script has been invoked from a symlink, resolve the link's target (i.e. the path to this script)
    TROLCOMMANDER_SH=`ls -l "$0"`
    TROLCOMMANDER_SH=${TROLCOMMANDER_SH#*-> }
else
    TROLCOMMANDER_SH=$0
fi

CURRENT_DIR=`dirname "$TROLCOMMANDER_SH"`
TROLCOMMANDER_JAR=$CURRENT_DIR/trolcommander.jar

OPEN_ARGS="--add-opens java.base/java.io=ALL-UNNAMED --add-opens java.desktop/sun.awt.X11=ALL-UNNAMED --add-opens java.base/java.net=ALL-UNNAMED --add-opens java.desktop/javax.swing.plaf.basic=ALL-UNNAMED --add-opens java.transaction.xa/javax.transaction.xa=ALL-UNNAMED --add-opens java.management/javax.management=ALL-UNNAMED --add-opens java.rmi/java.rmi=ALL-UNNAMED --add-opens java.security.jgss/org.ietf.jgss=ALL-UNNAMED --add-opens java.sql/java.sql=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.http=ALL-UNNAMED --add-opens java.base/sun.net.www.protocol.https=ALL-UNNAMED --add-opens jdk.httpserver/com.sun.net.httpserver=ALL-UNNAMED --add-opens java.compiler/javax.lang.model.element=ALL-UNNAMED"
if [ ! -f $TROLCOMMANDER_JAR ]
then
    echo "Error: cannot find file trolcommander.jar in directory $CURRENT_DIR"
    exit 1
fi

# Starts trolcommander.
$JAVA $JAVA_ARGS $OPEN_ARGS -DGNOME_DESKTOP_SESSION_ID=$GNOME_DESKTOP_SESSION_ID -DKDE_FULL_SESSION=$KDE_FULL_SESSION -DKDE_SESSION_VERSION=$KDE_SESSION_VERSION -jar $TROLCOMMANDER_JAR $TROLCOMMANDER_ARGS $@
