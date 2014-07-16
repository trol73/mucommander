; -*- coding: utf-8 -*-
; trolcommander install script
;

; Include Modern UI
!include MUI2.nsh

; The name of the installer
Name "trolCommander @MU_VERSION@"

; The file to write
OutFile @MU_OUT@

; Installer icon
!define MUI_ICON @MU_ICON@
!define MUI_UNICON @MU_ICON@

; The default installation directory
InstallDir $PROGRAMFILES\trolCommander
; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM SOFTWARE\trolCommander "Install_Dir"

; Specifies the requested execution level for Windows Vista. 
; Necessary for correct uninstallation of Start menu shortcuts.
RequestExecutionLevel admin

; Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_DIRECTORY
!define MUI_COMPONENTSPAGE_NODESC
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_RUN "$INSTDIR\trolCommander.exe"
!define MUI_FINISHPAGE_SHOWREADME_NOTCHECKED
!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\readme.txt"
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

; Languages
; Installer should support same languages as trolCommander.
!insertmacro MUI_LANGUAGE "English" ; first language is the default language
!insertmacro MUI_LANGUAGE "French"
!insertmacro MUI_LANGUAGE "Spanish"
!insertmacro MUI_LANGUAGE "SpanishInternational"
!insertmacro MUI_LANGUAGE "German"
!insertmacro MUI_LANGUAGE "Czech"
!insertmacro MUI_LANGUAGE "SimpChinese"
!insertmacro MUI_LANGUAGE "TradChinese"
!insertmacro MUI_LANGUAGE "Polish"
!insertmacro MUI_LANGUAGE "Hungarian"
!insertmacro MUI_LANGUAGE "Russian"
!insertmacro MUI_LANGUAGE "Slovenian"
!insertmacro MUI_LANGUAGE "Romanian"
!insertmacro MUI_LANGUAGE "Italian"
!insertmacro MUI_LANGUAGE "Korean"
!insertmacro MUI_LANGUAGE "Portuguese"
!insertmacro MUI_LANGUAGE "PortugueseBR"
!insertmacro MUI_LANGUAGE "Dutch"
!insertmacro MUI_LANGUAGE "Slovak"
!insertmacro MUI_LANGUAGE "Japanese"
!insertmacro MUI_LANGUAGE "Swedish"
!insertmacro MUI_LANGUAGE "Danish"

; The stuff to install
Section "trolCommander @MU_VERSION@ (required)"
  ; Read only section. It will always be set to install.
  SectionIn RO

  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  ; Copy trolCommander files
  File /oname=trolCommander.exe @MU_EXE@
  File /oname=trolcommander.jar @MU_JAR@
  File /oname=readme.txt @MU_README@
  File /oname=license.txt @MU_LICENSE@
  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\trolCommander "Install_Dir" "$INSTDIR"
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\trolCommander" "DisplayName" "trolCommander (remove only)"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\trolCommander" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\trolCommander" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\trolCommander" "NoRepair" 1
  WriteUninstaller "uninstall.exe"
  
  ; Create Start Menu directory and shortcuts
  CreateDirectory "$SMPROGRAMS\trolCommander"
  CreateShortCut "$SMPROGRAMS\trolCommander\trolCommander.lnk" "$INSTDIR\trolCommander.exe" "" "" 0 SW_SHOWMINIMIZED
  CreateShortCut "$SMPROGRAMS\trolCommander\Read Me.lnk" "$INSTDIR\readme.txt" "" "" 0
  CreateShortCut "$SMPROGRAMS\trolCommander\License.lnk" "$INSTDIR\license.txt" "" "" 0
  CreateShortCut "$SMPROGRAMS\trolCommander\Uninstall.lnk" "$INSTDIR\uninstall.exe" "" "" 0 
SectionEnd

; Quick launch shortcut (optional section)
Section "Quick Launch shortcut"
  CreateShortCut "$QUICKLAUNCH\trolCommander.lnk" "$INSTDIR\trolCommander.exe" "" "" 0 SW_SHOWMINIMIZED
SectionEnd

; Desktop shortcut (optional section)
Section "Desktop shortcut"
  CreateShortCut "$DESKTOP\trolCommander.lnk" "$INSTDIR\trolCommander.exe" "" "" 0 SW_SHOWMINIMIZED
SectionEnd

; Special uninstall section.
Section "Uninstall"
  ; remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\trolCommander"
  DeleteRegKey HKLM SOFTWARE\trolCommander
  ; remove files
  Delete $INSTDIR\trolCommander.exe
  Delete $INSTDIR\trolcommander.jar
  Delete $INSTDIR\trolCommander.lnk
  Delete $INSTDIR\readme.txt
  Delete $INSTDIR\license.txt
  ; MUST REMOVE UNINSTALLER, too
  Delete $INSTDIR\uninstall.exe
  ; remove shortcuts, if any.
  Delete "$SMPROGRAMS\trolCommander\*.*"
  Delete "$QUICKLAUNCH\trolCommander.lnk"
  Delete "$DESKTOP\trolCommander.lnk"
  ; remove directories used.
  RMDir "$SMPROGRAMS\trolCommander"
  RMDir "$INSTDIR"
SectionEnd

; eof
