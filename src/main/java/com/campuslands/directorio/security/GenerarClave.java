package com.campuslands.directorio.security;

import java.util.Scanner;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utilidad para crear usuarios desde DBeaver.
 * <p>
 * En VS Code: abre este archivo y pulsa "Run" encima del método main.
 * Escribe la contraseña y copia el hash que aparece en un INSERT de la tabla usuarios:
 *
 * <pre>
 * INSERT INTO usuarios (usuario, clave, nombre, rol) VALUES ('juan', '$2a$10$...', 'Juan Pérez', 'USUARIO');
 * </pre>
 */
public class GenerarClave {

	public static void main(String[] args) {
		String clave;
		if (args.length > 0) {
			clave = args[0];
		} else {
			System.out.print("Contraseña a cifrar: ");
			try (Scanner entrada = new Scanner(System.in)) {
				clave = entrada.nextLine();
			}
		}
		if (clave == null || clave.isBlank()) {
			System.out.println("La contraseña no puede estar vacía.");
			return;
		}
		String hash = new BCryptPasswordEncoder().encode(clave);
		System.out.println();
		System.out.println("Hash BCrypt (cópialo en la columna clave):");
		System.out.println(hash);
	}
}
