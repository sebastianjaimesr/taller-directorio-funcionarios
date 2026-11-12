# Arranca el directorio de funcionarios.
# Uso:  powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
#
# Credenciales de MySQL: se leen de config/application.properties (no se sube a git).
# Si ese archivo no existe, el script pide usuario y clave (la clave no se muestra).

Set-Location $PSScriptRoot

if (-not (Test-Path ".\config\application.properties") -and -not $env:DB_PASSWORD) {
	$usuario = Read-Host "Usuario de MySQL (Enter = root)"
	if ($usuario) { $env:DB_USERNAME = $usuario }
	$segura = Read-Host "Clave de MySQL" -AsSecureString
	$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura)
	try {
		$env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
	} finally {
		[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
	}
}

# Usa el JDK 21 de la carpeta de usuario si existe (el proyecto pide Java 21).
# Si no hay JDK 21, compila para Java 17 desde cero (clean) para no mezclar versiones.
$java = Get-ChildItem "$env:USERPROFILE\jdk-21" -Recurse -Filter java.exe -ErrorAction SilentlyContinue | Select-Object -First 1
if ($java) {
	$env:JAVA_HOME = $java.Directory.Parent.FullName
	Write-Host "Usando JDK 21: $env:JAVA_HOME"
	.\mvnw.cmd spring-boot:run
} else {
	Write-Host "No se encontro JDK 21 en $env:USERPROFILE\jdk-21; se compila para Java 17."
	.\mvnw.cmd "-Djava.version=17" clean spring-boot:run
}
