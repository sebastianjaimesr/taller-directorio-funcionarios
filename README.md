# Directorio de funcionarios — Taller Spring Boot

Java 21 · Spring Boot 4.1.2 · MVC · MySQL · HTML/CSS/JS vanilla.
Requisitos del enunciado: [REQUISITOS.md](REQUISITOS.md).

**Demo en línea (GitHub Pages):** https://sebastianjaimesr.github.io/taller-directorio-funcionarios/
GitHub Pages solo sirve archivos estáticos, así que la demo usa una API simulada que guarda los datos
en el navegador con las mismas reglas que la API real. La versión completa (Spring Boot + MySQL) se ejecuta localmente.

## Estructura
```
src/main/java/com/campuslands/directorio/
  model/Funcionario.java            entidad JPA (tabla funcionarios)
  dto/FuncionarioRequest.java       cuerpo de POST/PUT con validaciones
  repository/FuncionarioRepository  consultas por nombre y departamento
  service/FuncionarioService        reglas de negocio y filtros
  controller/FuncionarioController  API REST /api/v1/funcionarios
  exception/                        404, 400 y mensajes JSON de error
src/main/resources/static/          index.html, css/estilos.css, js/app.js
src/test/                           pruebas de la API (H2 en memoria)
```

## Cómo ejecutarlo

**Credenciales:** copia `config/application.properties.example` como `config/application.properties`
y pon tu usuario y clave de MySQL. Ese archivo no se sube a git y Spring Boot lo carga solo.

**Forma rápida (Windows):** arranca la app (si no hay `config/application.properties`, pide usuario y clave).
```powershell
powershell -ExecutionPolicy Bypass -File .\iniciar.ps1
```

**Forma manual:**
1. Tener MySQL corriendo en `localhost:3306`. La base `directorio_funcionarios` se crea sola.
2. Indicar la contraseña de MySQL (el usuario por defecto es `root`):
   ```powershell
   $env:DB_PASSWORD = "tu_contraseña"
   ```
   O edita `spring.datasource.password` en `src/main/resources/application.properties`.
3. Arrancar:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. Abrir http://localhost:8080

## Pruebas
```powershell
.\mvnw.cmd test
```

> El proyecto pide Java 21. Si solo tienes JDK 17, puedes compilar temporalmente con
> `.\mvnw.cmd "-Djava.version=17" spring-boot:run`.
