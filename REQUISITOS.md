# Taller Spring Boot — Directorio de funcionarios

Resumen de requisitos del enunciado (PDF en OneDrive, Ejercicio-Clase).

## Stack
- Java 21, Spring Boot, patrón MVC, MySQL.
- Frontend: HTML, CSS y JavaScript vanilla (sin frameworks); consume la API REST.
- Seguridad: fuera del alcance del enunciado original; se añadió inicio de sesión con usuario y contraseña (tabla `usuarios` en MySQL, BCrypt). CORS: fuera del alcance.
- Los datos deben persistir al reiniciar la aplicación.

## Entidad `Funcionario` (tabla `funcionarios`)
| Campo        | Tipo   | Regla                                   |
|--------------|--------|-----------------------------------------|
| id           | número | Generado al crear, no cambia al editar  |
| nombre       | texto  | Obligatorio                             |
| cargo        | texto  | Obligatorio                             |
| departamento | texto  | Obligatorio                             |
| telefono     | texto  | Opcional                                |

## API REST — base `/api/v1/funcionarios` (JSON; 204 sin cuerpo)
| Operación         | Método y ruta                                   | Respuesta                                |
|-------------------|-------------------------------------------------|------------------------------------------|
| Listar            | GET `/api/v1/funcionarios`                      | 200 con lista; 204 si está vacío         |
| Consultar por ID  | GET `/api/v1/funcionarios/{id}`                 | 200; 404 si no existe                    |
| Registrar         | POST `/api/v1/funcionarios`                     | 201 con el funcionario creado y su id    |
| Actualizar        | PUT `/api/v1/funcionarios/{id}`                 | 200; 404 si no existe                    |
| Eliminar          | DELETE `/api/v1/funcionarios/{id}`              | 204; 404 si no existe                    |
| Buscar por nombre | GET `/api/v1/funcionarios?nombre=ana`           | 200 con coincidencias; 204 si no hay     |
| Filtrar por depto | GET `/api/v1/funcionarios?departamento=Contabilidad` | 200 con coincidencias; 204 si no hay |

## Reglas de validación
- POST: body con nombre, cargo, departamento; telefono opcional; id ausente o null.
- PUT: se reenvían los obligatorios; el id lo da la ruta; telefono puede ir vacío.
- 400 si falta un obligatorio, si está vacío o solo espacios, o si se envía id no nulo al crear.
- `nombre`: coincidencia parcial. `departamento`: nombre completo exacto.
- Ambas búsquedas ignoran mayúsculas/minúsculas.
- Filtros vacíos o solo espacios = ausentes. Si llegan ambos, se aplica el de nombre.

## Interfaz web
- Tabla con todos los funcionarios al abrir.
- Formulario para crear; el mismo formulario carga datos para editar; botón para limpiarlo.
- Confirmación antes de eliminar (si cancela, se conserva).
- Consulta directa por ID; búsqueda por fragmento de nombre; filtro por departamento; volver al listado completo.
- Sin resultados: mensaje claro y tabla vacía.
- Mensajes de éxito, sin resultados o error; la tabla refleja cambios al guardar/eliminar.
