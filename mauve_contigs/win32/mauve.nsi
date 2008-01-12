;Pomo NSIS Script For Mauve
;(c)2k4-7 Aaron Darling
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
;  InstallDir "$PROGRAMFILES\Mauve $%release_version%"
  

  ;Get previous installation folder from registry if available
;  InstallDirRegKey HKLM "Software\Mauve" "$%release_version%"
;  InstallDirRegKey HKCU "Software\Mauve" "$%release_version%"

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING
  !define MUI_FINISHPAGE_TEXT "Mauve has been successfully installed.\r\nYou may launch Mauve from the Start Menu."

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "COPYING"
  !define MUI_PAGE_CUSTOMFUNCTION_SHOW .checkPreviousVersions
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  !insertmacro MUI_PAGE_FINISH
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"


;--------------------------------
; Look for a previous installation and prompt for uninstall
;
Function .checkPreviousVersions

	; first set the default install path
	StrCpy $INSTDIR "$PROGRAMFILES\Mauve $%release_version%"

	; call userInfo plugin to get user info.  The plugin puts the result in the stack
	userInfo::getAccountType
	pop $8
	strCmp $8 "Admin" AdminInst UserInst

	AdminInst:
	; get the admin install dir, if available
	ReadRegStr $9 HKLM Software\Mauve "$%release_version%"
	IfErrors uninstLoop1
	StrCpy $INSTDIR $9
	uninstLoop1:

	; look for previous versions to uninstall

	StrCpy $0 0
	loop1:
	  ClearErrors
	  EnumRegValue $1 HKLM Software\Mauve $0
;	  MessageBox MB_YESNO|MB_ICONQUESTION " (key $0, version $1, dir $2)" IDNO cont IDYES cont
;	cont:
	  IfErrors done1
	  IntOp $0 $0 + 1
	  ReadRegStr $3 HKLM Software\Mauve $1
	  StrCmp $3 "" loop1
	  StrCmp $1 "" versUnknown1 vOk1
	versUnknown1:
	  StrCpy $1 "unknown"
	vOk1:
	  IfErrors done1
	  MessageBox MB_YESNO|MB_ICONQUESTION " A previous installation of Mauve (version $1, dir $3) was detected.$\n We strongly recommend uninstalling it.$\n Would you like to uninstall it?" IDNO loop1
	  ExecWait "$3\Uninstall.exe"
	  Goto loop1
	done1:
   
	UserInst:
	; if there was no admin install dir, get the user install dir if available
	StrCmp $9 "" userRegInstDir uninstLoop2
	userRegInstDir:
	ReadRegStr $9 HKCU Software\Mauve "$%release_version%"
	IfErrors userDefaultInstdir
	StrCpy $INSTDIR $9
	userDefaultInstDir:
	StrCmp $9 "" userDefaultInstDir2 uninstLoop2
	userDefaultInstDir2:
	strCmp $8 "Admin" uninstLoop2 UserDefaultInstDir3
	UserDefaultInstDir3:
	StrCpy $9 "$PROFILE\Mauve $%release_version%"
	uninstLoop2:

	; look for previous versions to uninstall

	StrCpy $0 0
	loop2:
	  ClearErrors
	  EnumRegValue $1 HKCU Software\Mauve $0
;	  MessageBox MB_YESNO|MB_ICONQUESTION " (key $0, version $1, dir $2)" IDNO cont IDYES cont
;	cont:
	  IfErrors done2
	  IntOp $0 $0 + 1
	  ReadRegStr $3 HKCU Software\Mauve $1
	  StrCmp $3 "" loop2
	  StrCmp $1 "" versUnknown2 vOk2
	versUnknown2:
	  StrCpy $1 "unknown"
	vOk2:
	  IfErrors done2
	  MessageBox MB_YESNO|MB_ICONQUESTION " A previous installation of Mauve (version $1, dir $3) was detected.$\n We strongly recommend uninstalling it.$\n Would you like to uninstall it?" IDNO loop2
	  ExecWait "$3\Uninstall.exe"
	  Goto loop2
	done2:

	StrCmp $9 "" noSpecialInstDir
	StrCpy $INSTDIR $9
	noSpecialInstDir:

FunctionEnd


;--------------------------------
;Installer Sections

Section "mauve" SecMauve

; call userInfo plugin to get user info.  The plugin puts the result in the stack
    userInfo::getAccountType
    pop $8
    strCmp $8 "Admin" AdminInst2 UserInst2

AdminInst2:
  SetShellVarContext all   
UserInst2:

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
CreateDirectory "$SMPROGRAMS\Mauve $%release_version%"
Delete "$SMPROGRAMS\Mauve $%release_version%\Mauve.lnk"
CreateShortCut "$SMPROGRAMS\Mauve $%release_version%\Mauve.lnk" "$1\bin\javaw" "-jar -Xmx1000m Mauve.jar" "$INSTDIR\mauve.ico"
CreateShortCut "$SMPROGRAMS\Mauve $%release_version%\Mauve ChangeLog.lnk" "$INSTDIR\ChangeLog.html"
CreateShortCut "$SMPROGRAMS\Mauve $%release_version%\Mauve License.lnk" "notepad.exe" "COPYING" "$INSTDIR\COPYING" 0
CreateShortCut "$SMPROGRAMS\Mauve $%release_version%\Mauve Online Documentation.lnk" "$INSTDIR\Mauve Online Documentation.url" "" "$INSTDIR\Mauve Online Documentation.url" 0
CreateShortCut "$SMPROGRAMS\Mauve $%release_version%\Uninstall Mauve.lnk" "$INSTDIR\Uninstall.exe"


DetailPrint "Creating Mauve shortcut for java version $0 in directory $1"
Goto JavaDone

JavaMessage:
  MessageBox MB_YESNOCANCEL "Could not detect a suitable Java Runtime Environment.  Mauve requires Java version 1.4 or later.  Would you like to install it now?  Mauve will not work correctly without Java 1.4." IDYES InstallJava IDNO JavaDone
  Quit

InstallJava:
  SetOutPath $TEMP
  File "win32\jre-6u2-windows-i586-p-iftw.exe"
  ExecWait "$TEMP\jre-6u2-windows-i586-p-iftw.exe"
  Delete /REBOOTOK "$TEMP\jre-6u2-windows-i586-p-iftw.exe"
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
  File "ChangeLog.html"
  File "COPYING"
  File "README"
  File "win32\mauveAligner.exe"
  File "win32\progressiveMauve.exe"
  File "win32\muscle_aed.exe"
  File "Mauve.jar"
  File "win32\mauve.ico"
  File "win32\Mauve Online Documentation.url"

  ; 64-bit programs
  SetOutPath "$INSTDIR\win64"
  File "win64\mauveAligner.exe"
  File "win64\progressiveMauve.exe"
  File "win64\muscle_aed.exe"

  ; external dependencies
  SetOutPath "$INSTDIR\ext"
  File "ext\*.jar"

  ;Store installation folder
  strCmp $8 "Admin" AdminInstKey UserInstKey

  AdminInstKey:
    WriteRegStr HKLM "Software\Mauve" "$%release_version%" $INSTDIR
    Goto InstKeyDone
  UserInstKey:
    WriteRegStr HKCU "Software\Mauve" "$%release_version%" $INSTDIR
  InstKeyDone:

SectionEnd

Section "Uninstaller"
; compare the result with the string "Admin" to see if the user is admin. If match, jump 3 lines down.
    strCmp $8 "Admin" AdminUnInst UserUnInst

  AdminUnInst:
	; Write the uninstall keys for Windows
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Mauve $%release_version%" "DisplayName" "Mauve $%release_version%"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Mauve $%release_version%" "UninstallString" "$INSTDIR\Uninstall.exe"
	Goto UninstEnd
   
  UserUnInst:
	; Write the uninstall keys for Windows
	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Mauve $%release_version%" "DisplayName" "Mauve $%release_version%"
	WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\Mauve $%release_version%" "UninstallString" "$INSTDIR\Uninstall.exe"

  UninstEnd:
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
	
	; call userInfo plugin to get user info.  The plugin puts the result in the stack
	userInfo::getAccountType
	pop $8

	;Delete Files
	; Top level directory files
	
	Delete /REBOOTOK "$INSTDIR\ChangeLog.html"
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
	
	strCmp $8 "Admin" AdminContext UserContext
	AdminContext:
	SetShellVarContext all
	UserContext:

	; Delete shortcuts
	Delete /REBOOTOK "$SMPROGRAMS\Mauve.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve $%release_version%\Mauve.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve $%release_version%\Mauve License.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve $%release_version%\Mauve ChangeLog.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve $%release_version%\Mauve User Guide.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve $%release_version%\Mauve Online Documentation.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\Mauve $%release_version%\Uninstall Mauve.lnk"
	RMDir "$SMPROGRAMS\Mauve $%release_version%"
	
	;Delete Uninstaller And Unistall Registry Entries
	strCmp $8 "Admin" AdminRemove UserRemove
	AdminRemove:
	DeleteRegValue HKLM "SOFTWARE\Mauve" $%release_version%
	DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Mauve $%release_version%"
;	DeleteRegKey /ifempty HKLM "Software\Mauve"
	Goto RemoveDone
	UserRemove:
	DeleteRegValue HKCU "SOFTWARE\Mauve" $%release_version%
	DeleteRegKey HKCU "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\Mauve $%release_version%"
;	DeleteRegKey /ifempty HKCU "Software\Mauve"
	RemoveDone:
	Delete "$INSTDIR\Uninstall.exe"
	RMDir "$INSTDIR"
	

SectionEnd

