[Setup]
AppName=BimScript
AppVersion=1.0
DefaultDirName={userdesktop}\BimScript
DefaultGroupName=BimScript
OutputDir=.
OutputBaseFilename=setup
Compression=lzma
SolidCompression=yes
SetupIconFile=ico-BIM.ico

[Files]
Source: "target/Prufprozess-AIA-Element-1.0-SNAPSHOT-jar-with-dependencies.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "script_login.txt"; DestDir: "{app}"; Flags: ignoreversion
Source: "run.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "src/main/resources/msedgedriver.exe"; DestDir: "{userdesktop}\BimScript"; Flags: ignoreversion nocompression; Excludes: uninsdelete;

Source: "C:\Users\Admin\Downloads\microsoft-jdk-21.0.4-windows-x64.msi"; DestDir: "{tmp}"; Flags: deleteafterinstall

[Code]
var
  JREInstallCheckBox: TCheckBox;

procedure InitializeWizard();
begin
  JREInstallCheckBox := TCheckBox.Create(WizardForm);
  JREInstallCheckBox.Parent := WizardForm;
  JREInstallCheckBox.Caption := 'Install JRE';
  JREInstallCheckBox.Left := 20;
  JREInstallCheckBox.Top := WizardForm.Height - 80;
  JREInstallCheckBox.Width := 200;
  JREInstallCheckBox.Checked := True;
end;

function CheckJREInstall(): Boolean;
begin
  Result := JREInstallCheckBox.Checked;
end;

[Run]
Filename: "msiexec.exe"; Parameters: "/i ""{tmp}\microsoft-jdk-21.0.4-windows-x64.msi"" /quiet /norestart"; Flags: waituntilterminated; Check: CheckJREInstall;
Filename: "cmd"; Parameters: "/c if not exist ""{app}"" mkdir ""{app}"""; Flags: runhidden
Filename: "cmd"; Parameters: "/c if not exist ""{userdesktop}\BimScript"" mkdir ""{userdesktop}\BimScript"""; Flags: runhidden
