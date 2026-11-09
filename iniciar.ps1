# Arranca el directorio de funcionarios pidiendo la contraseña de MySQL sin mostrarla.
# Uso:  powershell -ExecutionPolicy Bypass -File .\iniciar.ps1

Set-Location $PSScriptRoot

if (-not $env:DB_PASSWORD) {
	$segura = Read-Host "Clave de MySQL para el usuario root" -AsSecureString
	$ptr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($segura)
	try {
		$env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($ptr)
	} finally {
		[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($ptr)
	}
}

# Valida la contraseña antes de arrancar Spring Boot.
$mysql = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
if (Test-Path $mysql) {
	$env:MYSQL_PWD = $env:DB_PASSWORD
	& $mysql -u root -e "SELECT 1" *> $null
	$ok = $LASTEXITCODE -eq 0
	Remove-Item Env:MYSQL_PWD
	if (-not $ok) {
		Write-Host "La clave de MySQL no es correcta. Vuelve a ejecutar el script." -ForegroundColor Red
		Remove-Item Env:DB_PASSWORD
		exit 1
	}
	Write-Host "Conexion a MySQL correcta. Arrancando la aplicacion..." -ForegroundColor Green
}

# Con JDK 21 instalado se puede quitar -Djava.version=17
.\mvnw.cmd "-Djava.version=17" spring-boot:run
