-- =====================================================================
--  Directorio de funcionarios — script para DBeaver (MySQL 8)
-- =====================================================================
--  Cómo usarlo en DBeaver:
--    1. Conéctate a tu MySQL local (localhost:3306, usuario root).
--    2. Abre este archivo: Archivo > Abrir archivo... > database/directorio.sql
--       (o clic derecho en la conexión > Editor SQL > Nuevo script y pega el contenido).
--    3. Ejecuta TODO el script con Alt + X ("Ejecutar script").
--    4. Refresca la conexión (F5): verás la base directorio_funcionarios
--       con las tablas usuarios y funcionarios.
--
--  Usuarios que crea este script (para iniciar sesión en la app):
--    usuario: admin      contraseña: Admin123*
--    usuario: consulta   contraseña: Consulta123*
--
--  Las contraseñas NO se guardan en texto plano: se guarda su hash BCrypt.
--  Para crear otro usuario, genera el hash con la clase GenerarClave
--  (src/main/java/.../security/GenerarClave.java) y haz un INSERT como los de abajo.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS directorio_funcionarios
	CHARACTER SET utf8mb4
	COLLATE utf8mb4_unicode_ci;

USE directorio_funcionarios;

-- ---------------------------------------------------------------------
-- Usuarios que pueden iniciar sesión
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
	id      BIGINT       NOT NULL AUTO_INCREMENT,
	usuario VARCHAR(50)  NOT NULL,
	clave   VARCHAR(100) NOT NULL COMMENT 'Hash BCrypt de la contraseña',
	nombre  VARCHAR(120) NOT NULL COMMENT 'Nombre que se muestra en la app',
	rol     VARCHAR(20)  NOT NULL DEFAULT 'USUARIO' COMMENT 'ADMIN o USUARIO',
	activo  BIT(1)       NOT NULL DEFAULT b'1' COMMENT '0 = no puede iniciar sesión',
	PRIMARY KEY (id),
	CONSTRAINT uk_usuarios_usuario UNIQUE (usuario)
) ENGINE = InnoDB;

-- INSERT IGNORE: si el usuario ya existe, no lo duplica ni da error.
INSERT IGNORE INTO usuarios (usuario, clave, nombre, rol, activo) VALUES
	('admin',    '$2a$10$HpglLOJ58e9GYFj70CCqaecxq4T8uyrcfupPSHq/9ah.C98VHimMm', 'Administrador',    'ADMIN',   b'1'),
	('consulta', '$2a$10$Z0UglOQgkN3kN.tS7QeTjOEcAb1hc5RCrC1qNccBefyGUR6hYSVh.', 'Usuario Consulta', 'USUARIO', b'1');

-- ---------------------------------------------------------------------
-- Funcionarios del directorio
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS funcionarios (
	id           BIGINT       NOT NULL AUTO_INCREMENT,
	nombre       VARCHAR(120) NOT NULL,
	cargo        VARCHAR(120) NOT NULL,
	departamento VARCHAR(120) NOT NULL,
	telefono     VARCHAR(30)  NULL,
	PRIMARY KEY (id)
) ENGINE = InnoDB;

-- Datos de ejemplo (solo si la tabla está vacía)
INSERT INTO funcionarios (nombre, cargo, departamento, telefono)
SELECT * FROM (
	SELECT 'Ana Pérez'      AS nombre, 'Contadora'            AS cargo, 'Contabilidad'   AS departamento, '3001112233' AS telefono UNION ALL
	SELECT 'Luis Gómez',              'Analista de sistemas',          'Sistemas',                       NULL                     UNION ALL
	SELECT 'Mariana López',           'Coordinadora',                  'Talento Humano',                 '3104445566'             UNION ALL
	SELECT 'Carlos Ruiz',             'Tesorero',                      'Contabilidad',                   '3157778899'             UNION ALL
	SELECT 'Valentina Rojas',         'Desarrolladora',                'Sistemas',                       '3012223344'
) AS datos
WHERE NOT EXISTS (SELECT 1 FROM funcionarios);

-- ---------------------------------------------------------------------
-- Consultas útiles (ejecútalas una por una con Ctrl + Enter)
-- ---------------------------------------------------------------------
-- Ver usuarios (la clave aparece cifrada):
-- SELECT id, usuario, nombre, rol, activo FROM usuarios;

-- Desactivar un usuario (ya no podrá entrar):
-- UPDATE usuarios SET activo = b'0' WHERE usuario = 'consulta';

-- Crear un usuario nuevo (pega el hash generado con GenerarClave):
-- INSERT INTO usuarios (usuario, clave, nombre, rol) VALUES ('juan', '$2a$10$...hash...', 'Juan Pérez', 'USUARIO');

-- Cambiar la contraseña de un usuario:
-- UPDATE usuarios SET clave = '$2a$10$...hash...' WHERE usuario = 'admin';

-- Ver funcionarios:
-- SELECT * FROM funcionarios ORDER BY id;
