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

# Con JDK 21 instalado se puede quitar -Djava.version=17
.\mvnw.cmd "-Djava.version=17" spring-boot:run
