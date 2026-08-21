package com.fbi.cjs.ws.security;

import jakarta.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un recurso o método REST como "requiere token".
 *
 * <p>
 * {@code @NameBinding} hace que los filtros de seguridad se apliquen solo a lo
 * anotado; sin esto también correrían sobre {@code /auth/login}, que es
 * público.
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Secured {
}
