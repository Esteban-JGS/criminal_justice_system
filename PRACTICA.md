# Práctica: diagnóstico de fallos en el web service

## Contexto

El módulo de **agentes** del web service quedó a medias. Están escritos el DTO, el
repositorio, el servicio y el recurso REST, siguiendo los mismos patrones que ya existen
para criminales y usuarios, pero el módulo no funciona. El proyecto ni siquiera compila.

El objetivo de la práctica no es escribir código nuevo, sino **encontrar los ocho fallos,
explicar por qué ocurren y corregirlos**.

## Reglas

1. No borre un archivo para escribirlo de nuevo desde cero. Corrija el fallo puntual.
2. No modifique las pruebas. Las pruebas describen el comportamiento esperado; si las
   cambia para que pasen, el resultado deja de ser válido.
3. No modifique los módulos de criminales ni de usuarios. Ese código funciona y sirve como
   referencia.
4. Registre cada fallo en la bitácora del final: síntoma, causa y corrección.

## Qué debe hacer el módulo cuando esté terminado

Base de la API: `http://localhost:8080/criminal_justice_ws/api/v1`

| Método | Ruta | Acceso | Respuesta esperada |
| --- | --- | --- | --- |
| GET | `/agents` | cualquier usuario autenticado | 200 con la lista de 5 agentes |
| GET | `/agents?search=texto` | cualquier usuario autenticado | 200, filtra por nombre, placa o división |
| GET | `/agents?status=ACTIVO` | cualquier usuario autenticado | 200, filtra por estado |
| GET | `/agents/{id}` | cualquier usuario autenticado | 200, o 404 si no existe |
| POST | `/agents` | SUPERVISOR, JEFE_FBI | 201 con la cabecera `Location` |
| PUT | `/agents/{id}` | SUPERVISOR, JEFE_FBI | 200, o 404 si no existe |
| DELETE | `/agents/{id}` | JEFE_FBI | 200, o 404 si no existe |

Reglas de negocio:

- La placa tiene formato `FBI-0000` y no se puede repetir entre agentes (409).
- Sin token, cualquier ruta de `/agents` responde 401.
- Con un rol insuficiente, responde 403.
- Los datos inválidos responden 422 con el detalle de los campos.

Usuarios de prueba, contraseña `1234`: `agente01`, `supervisor01`, `jefe01`.

## Preparación

```bash
git pull
git checkout -b practica-agentes-SUNOMBRE
mvn clean install
```

El último comando falla. Ahí empieza el trabajo.

Para desplegar y probar:

```bash
asadmin start-domain domain1
asadmin deploy --force=true ws/target/criminal_justice_ws.war
```

En Windows, `asadmin` está en `C:\Program Files\Java\payara7\bin\asadmin.bat`.

El log del servidor, que va a necesitar en dos de los niveles, está en:

```text
C:\Program Files\Java\payara7\glassfish\domains\domain1\logs\server.log
```

## Los cuatro niveles

Los niveles van en orden. Cada uno oculta al siguiente: hasta que el proyecto no compile no
se puede desplegar, y hasta que no despliegue no se pueden ver los fallos de ejecución.

### Nivel 0. No compila (2 fallos)

`mvn clean install` no llega a construir el archivo `.war`.

Lea los mensajes de `[ERROR]` completos, con el archivo y la línea de cada uno. Maven los
imprime dos veces, primero al compilar y otra vez en el resumen final, así que en total son
cinco errores distintos. Cinco errores no significan cinco problemas: cuente cuántas causas
hay en realidad.

Nivel terminado cuando `mvn clean install` responde `BUILD SUCCESS`.

### Nivel 1. No despliega (1 fallo)

El `.war` ya se construye y las pruebas corren, pero Payara rechaza la aplicación al
desplegarla. Es normal que en este punto haya pruebas en rojo: corresponden a fallos de
niveles posteriores.

Lea la respuesta del comando `asadmin deploy`, que menciona una clase concreta y el
subsistema que se quejó. Después compare esa clase con su equivalente del módulo de
criminales: hay algo que una tiene y la otra no.

Nivel terminado cuando `asadmin deploy` responde `Command deploy executed successfully`.

### Nivel 2. Falla al usarlo (3 fallos)

La aplicación levanta, pero la API no responde como debería. Dos de estos tres fallos ya
aparecen señalados en las pruebas; el tercero solo se ve al ejecutar la API. Para probarla:

```bash
BASE=http://localhost:8080/criminal_justice_ws/api/v1
TOKEN=$(curl -s -X POST $BASE/auth/login -H "Content-Type: application/json" \
  -d '{"username":"supervisor01","password":"1234"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# Listar
curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents -H "Authorization: Bearer $TOKEN"

# Registrar
curl -s -o /dev/null -w "%{http_code}\n" -X POST $BASE/agents \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"badgeNumber":"FBI-2001","name":"Nuevo Agente","division":"Cibercrimen","status":"ACTIVO"}'
```

Los síntomas aparecen en este orden, uno después de corregir el anterior:

| Síntoma | Por dónde buscar |
| --- | --- |
| Listar responde 404, aunque la aplicación está desplegada | La URL de un recurso se arma con tres partes concatenadas. Revise cuáles son y de dónde sale cada una |
| Registrar responde 415 Unsupported Media Type | El servidor indica que no acepta el formato enviado. Revise qué formato declara aceptar el recurso |
| Registrar responde 500, pero listar sigue funcionando | Leer funciona y escribir no. Piense qué hace el servidor al recibir JSON que no hace al enviarlo. El `server.log` tiene el mensaje exacto, en la línea `Caused by:` |

Nivel terminado cuando listar responde 200 y registrar crea el agente.

### Nivel 3. Funciona, pero está mal (2 fallos)

Los dos últimos fallos no producen ningún error. La API responde, los datos se guardan y a
simple vista todo parece correcto. Aun así son fallos, y uno de los dos es el más grave de
la práctica.

Para encontrarlos, compare el comportamiento real contra la tabla de la sección "Qué debe
hacer el módulo cuando esté terminado", y el código contra `CriminalResource`.

Estos dos comandos ayudan:

```bash
# Muestra las cabeceras completas de la respuesta, no solo el cuerpo
curl -s -D- -o /dev/null -X POST $BASE/agents \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"badgeNumber":"FBI-2002","name":"Otro Agente","division":"Forense","status":"ACTIVO"}'

# Compare el mismo caso sin token en los dos recursos
curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents
curl -s -o /dev/null -w "%{http_code}\n" $BASE/criminals
```

Nivel terminado cuando `mvn test` está completamente en verde y el comportamiento coincide
con la tabla.

## Las pruebas como referencia

```bash
mvn test
```

El módulo de agentes tiene 28 pruebas. Al inicio no llegan a ejecutarse porque el proyecto
no compila; después van pasando a verde conforme avance.

Los mensajes de las pruebas indican qué se espera, no solo que algo no coincide. Por
ejemplo:

```text
[ERROR]   AgentResourceTest.createDevuelveResponse:76 un POST que crea algo responde 201
y el header Location; eso necesita construir un Response ==> expected:
<jakarta.ws.rs.core.Response> but was: <com.fbi.cjs.shared.api.ApiResponse>
```

Tenga presente que **dos de los ocho fallos no los detecta ninguna prueba**. Que `mvn test`
esté en verde no significa que la práctica esté terminada: la aplicación también tiene que
desplegar y comportarse como indica la tabla. Una de las preguntas de la entrega es
justamente por qué esos dos fallos no se pueden detectar con pruebas unitarias.

## Criterios de aceptación

Antes de entregar, verifique que estos comandos den estos resultados:

```bash
mvn clean install
# BUILD SUCCESS y todas las pruebas en verde

asadmin deploy --force=true ws/target/criminal_justice_ws.war
# Command deploy executed successfully
```

```bash
BASE=http://localhost:8080/criminal_justice_ws/api/v1
TOKEN=$(curl -s -X POST $BASE/auth/login -H "Content-Type: application/json" \
  -d '{"username":"supervisor01","password":"1234"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents
# 401

curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents -H "Authorization: Bearer $TOKEN"
# 200

curl -s -D- -o /dev/null -X POST $BASE/agents -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"badgeNumber":"FBI-2001","name":"Nuevo","division":"Cibercrimen","status":"ACTIVO"}'
# HTTP/1.1 201 Created
# Location: http://localhost:8080/criminal_justice_ws/api/v1/agents/<id>
```

## Dónde buscar según el tipo de problema

| Tipo de problema | Dónde mirar |
| --- | --- |
| Compilación | El mensaje de `[ERROR]`: archivo, línea y símbolo que no encuentra |
| Despliegue | La salida de `asadmin deploy` y el `server.log` |
| Una ruta responde 404 | `JaxRsApplication`, el `@Path` del recurso y la clase `ApiPaths` |
| Un 4xx al enviar datos | Las anotaciones de la clase del recurso |
| Un 500 | El `server.log`, en la línea `Caused by:` |
| Token y permisos | `@Secured` y `@AllowedRoles` en `CriminalResource` |

En la mayoría de los casos la respuesta aparece al abrir el archivo equivalente del módulo
de criminales o de usuarios y compararlo con el de agentes.

## Bitácora

Complete esta tabla con los ocho fallos.

| # | Archivo | Síntoma observado | Causa | Corrección aplicada | Cómo lo comprobó |
| --- | --- | --- | --- | --- | --- |
| 1 | | | | | |
| 2 | | | | | |
| 3 | | | | | |
| 4 | | | | | |
| 5 | | | | | |
| 6 | | | | | |
| 7 | | | | | |
| 8 | | | | | |

Responda además, en dos o tres líneas cada una:

1. ¿Por qué el fallo del nivel 1 no lo detecta ninguna prueba unitaria?
2. De los dos fallos del nivel 3, ¿cuál es más peligroso en un sistema real y por qué?
3. ¿Qué le agregaría al proyecto para que estos ocho fallos no se repitan?

## Ejercicio opcional

Si termina antes, agregue al módulo de agentes un filtro por división:

```text
GET /agents?division=Cibercrimen
```

Debe funcionar combinado con los filtros que ya existen (`search` y `status`) y necesita su
propia prueba en `AgentRepositoryContractTest`. Como referencia, revise cómo está resuelto
el filtro por nivel de peligrosidad en el módulo de criminales.

Para este ejercicio sí puede tocar los archivos de prueba: la regla 2 se refiere a no
alterar las pruebas existentes para que pasen, no a que no se puedan agregar nuevas. Si
cambia la firma del método `search` del repositorio, va a tener que ajustar las llamadas de
las pruebas actuales; indique ese cambio en la entrega.

## Entrega

1. La rama con los ocho fallos corregidos y los commits con mensajes en formato
   Conventional Commits, por ejemplo `fix(ws): corregir la ruta del recurso de agentes`.
2. La bitácora completa y las tres preguntas respondidas.
3. La salida de `mvn test` en verde.
