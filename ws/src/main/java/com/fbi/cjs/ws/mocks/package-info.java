/**
 * Implementaciones en memoria de los repositorios, para desarrollar y probar la
 * API sin base de datos.
 *
 * <p>
 * Ninguna clase fuera de este paquete debe importar nada de aquí: los servicios
 * inyectan la interfaz, no la implementación. Por eso migrar a Oracle es
 * agregar las clases JPA como {@code @Alternative} y activarlas en
 * {@code beans.xml}.
 *
 * <p>
 * Los datos viven mientras el WAR esté desplegado; al redesplegar vuelven al
 * estado de {@link com.fbi.cjs.ws.mocks.MockData}.
 */
package com.fbi.cjs.ws.mocks;
