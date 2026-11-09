# Directorio de funcionarios — Taller Spring Boot

Java 21 · Spring Boot 4.1.2 · MVC · MySQL · HTML/CSS/JS vanilla.
Requisitos del enunciado: [REQUISITOS.md](REQUISITOS.md).

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

**Forma rápida (Windows):** el script pide la clave de MySQL sin mostrarla, la verifica y arranca la app.
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
