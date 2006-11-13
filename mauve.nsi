;Pomo NSIS Script For Mauve
;(c)2k4-6 Aaron Darling
;

;--------------------------------
;Include Modern UI
  !include "MUI.nsh"
;--------------------------------
;General


  Name "Mauve $%release_version%"

  ;Do A CRC Check
  CRCCheck On

  ;Output File Name
  OutFile "dist\mauve_installer_$%release_version%.exe"

  ;The Default Installation Directory
  InstallDir "$PROGRAMFILES\Mauve"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\Mauve" ""

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "COPYING"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "mauve" SecMauve

; Look for Java Runtime Environment
push $0
push $1
push $2
FindJava:

ReadRegStr $0 HKLM "Software\JavaSoft\Java Runtime Environment" CurrentVersion
IfErrors JavaMessage
ReadRegStr $1 HKLM "Software\JavaSoft\Java Runtime Environment\$0" JavaHome
IfErrors JavaMessage

; check what version of java this is
StrCpy $2 $1 1
StrCmp $2 "1" CheckJavaMinorVersion JavaOk

CheckJavaMinorVersion:
StrCpy $2 $1 1 2
StrCmp $2 "0" JavaMessage
StrCmp $2 "1" JavaMessage
StrCmp $2 "2" JavaMessage
; java 1.3 is no longer supported
StrCmp $2 "3" JavaMessage

JavaOk:

SetOutPath $INSTDIR
Delete "$SMPROGRAMS\Mauve.lnk"
CreateShortCut "$SMPROGRAMS\Mauve.lnk" "$1\bin\javaw" "-jar -Xmx1000m Mauve.jar" "$INSTDIR\mauve.ico"
CreateDirectory "$SMPROGRAMS\Mauve"
Delete "$SMPROGRAMS\Mauve\Mauve.lnk"
CreateShortCut "$SMPROGRAMS\Mauve.lnk" "$1\bin\javaw" "-jar -Xmx1000m Mauve.jar" "$INSTDIR\mauve.ico"
CreateShortCut "$SMPROGRAMS\Mauve\Mauve ChangeLog.lnk" "notepad.exe" "ChangeLog" "$INSTDIR\ChangeLog" 0
CreateShortCut "$SMPROGRAMS\Mauve\Mauve License.lnk" "notepad.exe" "COPYING" "$INSTDIR\COPYING" 0
CreateShortCut "$SMPROGRAMS\Mauve\Mauve User Guide.lnk" "$INSTDIR\mauve_docs.pdf" "" "$INSTDIR\mauve_docs.pdf" 0
CreateShortCut "$SMPROGRAMS\Mauve\Mauve Online Documentation.lnk" "$INSTDIR\Mauve Online Documentation.url" "" "$INSTDIR\Mauve Online Documentation.url" 0


DetailPrint "Creating Mauve shortcut for java version $0 in directory $1"
Goto JavaDone

JavaMessage:
  MessageBox MB_YESNOCANCEL "Could not detect a suitable Java Runtime Environment.  Mauve requires Java version 1.4 or later.  Would you like to install it now?  Mauve will not work correctly without Java 1.4." IDYES InstallJava IDNO JavaDone
  Quit

InstallJava:
  SetOutPath $TEMP
  File "jre-1_5_0_06-windows-i586-p-iftw.exe"
  ExecWait "$TEMP\jre-1_5_0_06-windows-i586-p-iftw.exe"
  Delete /REBOOTOK "$TEMP\jre-1_5_0_06-windows-i586-p-iftw.exe"
  Goto FindJava


JavaDone:
pop $2
pop $1
pop $0


  ;Install Files
  SetOutPath $INSTDIR
  SetCompress Auto
  SetOverwrite IfNewer

  ; Top level directory files
  File "ChangeLog"
  File "COPYING"
  File "README"
  File "win32\mauveAligner.exe"
  File "win32\progressiveMauve.exe"
  File "win32\muscle_aed.exe"
  File "Mauve.jar"
  File "mauve.ico"
  File "mauve_docs.pdf"
  File "Mauve Online Documentation.url"

  ; external dependencies
  SetOutPath "$INSTDIR\ext"
  File "ext\*.jar"

  ;Store installation folder
  WriteRegStr HKCU "Software\Mauve" "" $INSTDIR

SectionEnd

Section "Uninstaller"
	; Write the uninstall keys for Windows
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\mauve" "DisplayName" "Mauve $%release_version%"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\mauve" "UninstallString" "$INSTDIR\Uninstall.exe"
	WriteUninstaller "Uninstall.exe"
SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_SecMauve ${LANG_ENGLISH} "Install the Mauve genome alignment software."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${SecMauve} $(DESC_SecMauve)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"
	
	;Delete Files
	; Top level directory files
	
	Delete /REBOOTOK "$INSTDIR\ChangeLog"
	Delete /REBOOTOK "$INSTDIR\COPYING"
	Delete /REBOOTOK "$INSTDIR\README"
	Delete /REBOOTOK "$INSTDIR\mauveAligner.exe"
	Delete /REBOOTOK "$INSTDIR\progressiveMauve.exe"
	Delete /REBOOTOK "$INSTDIR\muscle_aed.exe"
	Delete /REBOOTOK "$INSTDIR\Mauve.jar"
	Delete /REBOOTOK "$INSTDIR\mauve.ico"
	Delete /REBOOTOK "$INSTDIR\mauve_docs.pdf"
	Delete /REBOOTOK "$INSTDIR\Mauve Online Documentation.url"
	
	; External dependency files
	
	Delete /REBOOTOK "$INSTDIR\ext\*.jar"
	RMDir "$INSTDIR\ext"
	
	; Delete shortcuts
	Delete /REBOOTOK "$SMPROGRAMS\Mauve.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve\Mauve.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve\Mauve License.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve\Mauve ChangeLog.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve\Mauve User Guide.lnk"
	RMDir "$SMPROGRAMS\Mauve"
	
	;Delete Uninstaller And Unistall Registry Entries
	Delete "$INSTDIR\Uninstall.exe"
	DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Mauve"
	DeleteRegKey HKEY_LOCAL_MACHINE "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Mauve"
	RMDir "$INSTDIR"
	
	DeleteRegKey /ifempty HKCU "Software\Mauve"

SectionEnd

