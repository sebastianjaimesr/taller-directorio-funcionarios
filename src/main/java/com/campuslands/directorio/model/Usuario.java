package com.campuslands.directorio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Usuario que puede iniciar sesión en el directorio (tabla {@code usuarios}).
 * Los usuarios se crean desde DBeaver con el script {@code database/directorio.sql}.
 * La clave se guarda cifrada con BCrypt, nunca en texto plano.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Nombre con el que se inicia sesión. */
	@Column(nullable = false, unique = true, length = 50)
	private String usuario;

	/** Hash BCrypt de la contraseña (empieza por $2a$, $2b$ o $2y$). */
	@Column(nullable = false, length = 100)
	private String clave;

	/** Nombre que se muestra en la barra superior. */
	@Column(nullable = false, length = 120)
	private String nombre;

	/** ADMIN o USUARIO. */
	@Column(nullable = false, length = 20)
	private String rol = "USUARIO";

	/** Si es false, el usuario no puede iniciar sesión. */
	@Column(nullable = false)
	private boolean activo = true;

	public Usuario() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getClave() {
		return clave;
	}

	public void setClave(String clave) {
		this.clave = clave;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}
}
