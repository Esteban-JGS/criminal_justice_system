package com.fbi.cjs.ws.security;

import com.fbi.cjs.shared.enums.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Roles que pueden ejecutar un recurso o método REST. La anotación del método
 * gana sobre la de la clase.
 *
 * <p>
 * Existe en lugar de {@code jakarta.annotation.security.RolesAllowed} porque
 * esa la intercepta el propio Payara y la valida contra su sistema de seguridad
 * (realms, identity stores). Como acá la identidad la maneja
 * {@link TokenStore}, el contenedor no reconoce al usuario y responde un 401 en
 * HTML antes de que el método se ejecute. Con anotación propia la autorización
 * queda en {@link AuthorizationFilter} y los errores siempre salen con el mismo
 * JSON.
 *
 * <p>
 * Ventaja adicional: recibe el enum {@link Role} directamente, así que un rol
 * mal escrito no compila.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface AllowedRoles {

	Role[] value();
}
