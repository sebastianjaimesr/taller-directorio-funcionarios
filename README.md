# Directorio de funcionarios — Taller Spring Boot

Java 21 · Spring Boot 4.1.2 · Spring Security · MVC · MySQL · HTML/CSS/JS vanilla.
Requisitos del enunciado: [REQUISITOS.md](REQUISITOS.md).

**Demo en línea (GitHub Pages):** https://sebastianjaimesr.github.io/taller-directorio-funcionarios/
(usuario demo: `campus2023` / `campus2023`)
GitHub Pages solo sirve archivos estáticos, así que la demo usa una API simulada que guarda los datos
en el navegador con las mismas reglas que la API real. La versión completa (Spring Boot + MySQL) se ejecuta localmente.

## Inicio de sesión
La aplicación pide **usuario y contraseña** antes de mostrar el directorio.
Los usuarios viven en la tabla `usuarios` de MySQL (la misma base `directorio_funcionarios`)
y se administran desde **DBeaver**. Las contraseñas se guardan cifradas con **BCrypt**.

| Usuario      | Contraseña     | Rol     |
|--------------|----------------|---------|
| `campus2023` | `campus2023` | ADMIN   |
| `consulta`   | `Consulta123*` | USUARIO |

Flujo: `login.html` → `POST /api/v1/auth/login` (Spring Security valida contra la tabla `usuarios`)
→ cookie de sesión `JSESSIONID` → `index.html`. Sin sesión, la API responde `401` y las páginas
redirigen al login. `POST /api/v1/auth/logout` cierra la sesión; `GET /api/v1/auth/yo` devuelve el usuario actual.

**Intentos fallidos:** después de **3 intentos fallidos** el servidor responde `423` y la página redirige a
`error.html`, que muestra una cuenta regresiva. El login de ese navegador queda bloqueado **5 minutos**
(aunque se recargue la página). `GET /api/v1/auth/estado` indica si está bloqueado y cuántos segundos faltan.
Se configura en `security/IntentosLogin.java` (`MAXIMO_INTENTOS`, `TIEMPO_BLOQUEO`).

### Preparar la base de datos en DBeaver
1. En DBeaver crea (o abre) la conexión **MySQL** a `localhost:3306` con tu usuario `root`.
   Si DBeaver lo pide, en *Driver properties* pon `allowPublicKeyRetrieval=true`.
2. *Archivo → Abrir archivo…* → `database/directorio.sql`.
3. Ejecuta todo el script con **Alt + X**. Crea la base, las tablas `usuarios` y `funcionarios`,
   los dos usuarios de arriba y 5 funcionarios de ejemplo. Se puede ejecutar varias veces sin duplicar datos.
4. Refresca la conexión (**F5**) y verás las tablas.

### Crear más usuarios
1. En VS Code abre `src/main/java/com/campuslands/directorio/security/GenerarClave.java` y pulsa **Run**.
2. Escribe la contraseña; copia el hash que imprime.
3. En DBeaver:
   ```sql
   INSERT INTO usuarios (usuario, clave, nombre, rol)
   VALUES ('juan', '$2a$10$...hash...', 'Juan Pérez', 'USUARIO');
   ```
Para bloquear a alguien: `UPDATE usuarios SET activo = b'0' WHERE usuario = 'juan';`

## Estructura
```
src/main/java/com/campuslands/directorio/
  model/Funcionario.java            entidad JPA (tabla funcionarios)
  model/Usuario.java                entidad JPA (tabla usuarios, clave BCrypt)
  dto/FuncionarioRequest.java       cuerpo de POST/PUT con validaciones
  repository/FuncionarioRepository  consultas por nombre y departamento
  repository/UsuarioRepository      búsqueda de usuario para el login
  config/SeguridadConfig            reglas de Spring Security (login, logout, 401)
  security/UsuarioDetailsService    carga el usuario desde MySQL
  security/IntentosLogin            bloqueo tras 3 intentos fallidos
  security/GenerarClave             utilidad para cifrar contraseñas nuevas
  controller/AuthController         GET /api/v1/auth/yo
  service/FuncionarioService        reglas de negocio y filtros
  controller/FuncionarioController  API REST /api/v1/funcionarios
  exception/                        404, 400 y mensajes JSON de error
src/main/resources/static/          login.html, index.html, error.html, css/estilos.css, js/*.js
src/test/                           pruebas de la API y del login (H2 en memoria)
database/directorio.sql             script para ejecutar en DBeaver
```

## Cómo ejecutarlo

**Credenciales:** copia `config/application.properties.example` como `config/application.properties`
y pon tu usuario y clave de MySQL. Ese archivo no se sube a git y Spring Boot lo carga solo.

**Forma rápida (Windows):** arranca la app (si no hay `config/application.properties`, pide usuario y clave).
```powershell
powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
```

**Forma manual:**
1. Tener MySQL corriendo en `localhost:3306` y haber ejecutado `database/directorio.sql` en DBeaver.
2. Indicar la contraseña de MySQL (el usuario por defecto es `root`):
   ```powershell
   $env:DB_PASSWORD = "tu_contraseña"
   ```
   O edita `spring.datasource.password` en `src/main/resources/application.properties`.
3. Arrancar:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. Abrir http://localhost:8080 e iniciar sesión con `campus2023` / `campus2023`.

## Pruebas
```powershell
.\mvnw.cmd test
```

> El proyecto pide Java 21. Si solo tienes JDK 17, puedes compilar temporalmente con
> `.\mvnw.cmd "-Djava.version=17" spring-boot:run`.
