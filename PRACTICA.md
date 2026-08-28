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

## Cómo trabajar según su entorno

Las cinco tareas de la práctica son siempre las mismas: compilar, correr las pruebas,
desplegar, leer el log y llamar a la API. Lo que cambia es dónde se hace cada una.

### NetBeans

| Tarea | Cómo se hace |
| --- | --- |
| Abrir el proyecto | `File > Open Project` sobre la carpeta raíz del repositorio. NetBeans lo reconoce como proyecto Maven con módulos y muestra `shared`, `ws` y `front` |
| Compilar | Clic derecho en el proyecto raíz, `Clean and Build`. Los errores salen en la ventana `Output`; al hacer doble clic sobre la línea de un error, abre el archivo en esa línea |
| Correr las pruebas | Clic derecho en el módulo `ws`, `Test`. El resultado sale en la ventana `Test Results`, con los mensajes de cada prueba que falla |
| Correr una sola clase de prueba | Abra la clase, clic derecho dentro del editor, `Test File` |
| Registrar Payara | Ventana `Services`, nodo `Servers`, clic derecho, `Add Server`, elija `Payara Server` e indique la carpeta `C:\Program Files\Java\payara7` y el dominio `domain1` |
| Arrancar el servidor | En `Services > Servers`, clic derecho sobre Payara, `Start` |
| Desplegar | Con el servidor registrado, clic derecho en el módulo `ws`, `Run`. También sirve el comando `asadmin deploy` desde una terminal |
| Ver el log | En `Services > Servers`, clic derecho sobre Payara, `View Server Log` |
| Llamar a la API | NetBeans no trae cliente REST. Use Git Bash, PowerShell o Postman, como se explica más abajo |

### VS Code

Instale dos extensiones: **Extension Pack for Java** (Microsoft) y **REST Client**
(`humao.rest-client`).

| Tarea | Cómo se hace |
| --- | --- |
| Compilar | Terminal integrada (`Ctrl` + `` ` ``) y `mvn clean install`. También desde el panel `MAVEN`, en `Lifecycle > install` |
| Ver los errores | Salen en la terminal; con `Ctrl` + clic sobre la ruta del archivo se abre en la línea exacta. Los errores del análisis en vivo aparecen en el panel `Problems` |
| Correr las pruebas | Vista `Testing`, el icono del matraz en la barra lateral. También `mvn test` en la terminal |
| Correr una sola clase o método | Botón de reproducción que aparece junto al nombre de la clase o del método en el editor |
| Desplegar | VS Code no administra Payara. Use la terminal con `asadmin`, o la consola web en `http://localhost:4848`, en `Applications > Deploy` |
| Ver el log | `File > Open File` sobre `server.log`. Para seguirlo mientras ocurre, use la terminal (ver más abajo) |
| Llamar a la API | Abra `ws/docs/agents.http` y haga clic en `Send Request`, el enlace que aparece encima de cada petición |

El archivo `ws/docs/agents.http` ya trae las quince peticiones de la práctica, incluidas las
que deben fallar, con el resultado esperado anotado en cada una. Ejecute primero el login:
las demás toman el token de esa respuesta automáticamente.

### Terminal

Los comandos de este documento están escritos para **Git Bash**, que viene con Git para
Windows. Ahí funcionan tal cual están.

En **PowerShell** hay tres diferencias que conviene conocer, porque los síntomas se
confunden con fallos de la práctica:

1. `curl` no es curl: es un alias de `Invoke-WebRequest`. Escriba `curl.exe` con la
   extensión.
2. PowerShell le quita las comillas al JSON que se pasa con `-d`, así que un POST hecho con
   `curl.exe` llega mal formado al servidor y responde 500 aunque el código esté bien. Para
   enviar cuerpos JSON use `Invoke-RestMethod`.
3. `Invoke-RestMethod` lanza una excepción cuando la respuesta es 4xx o 5xx, así que para
   ver esos códigos hay que capturarla.

Equivalencias verificadas:

```powershell
$base = "http://localhost:8080/criminal_justice_ws/api/v1"

# Código de estado de una petición sin cuerpo (401, 403, 404...)
curl.exe -s -o NUL -w "%{http_code}`n" "$base/agents"

# Login y token
$login = Invoke-RestMethod -Method Post -Uri "$base/auth/login" `
  -ContentType "application/json" `
  -Body '{"username":"supervisor01","password":"1234"}'
$token = $login.data.token

# Listar
Invoke-RestMethod -Uri "$base/agents" -Headers @{ Authorization = "Bearer $token" }

# Registrar y ver el código de estado y la cabecera Location
$r = Invoke-WebRequest -Method Post -Uri "$base/agents" -ContentType "application/json" `
  -Headers @{ Authorization = "Bearer $token" } `
  -Body '{"badgeNumber":"FBI-2001","name":"Nuevo Agente","division":"Cibercrimen","status":"ACTIVO"}' `
  -UseBasicParsing
$r.StatusCode
$r.Headers.Location

# Ver el código de estado de una respuesta de error
try { Invoke-RestMethod -Uri "$base/agents" | Out-Null } catch {
  $_.Exception.Response.StatusCode.value__
}

# Seguir el log del servidor mientras ocurre
Get-Content "C:\Program Files\Java\payara7\glassfish\domains\domain1\logs\server.log" -Tail 50 -Wait
```

En Git Bash, el equivalente de la última línea es:

```bash
tail -f -n 50 "/c/Program Files/Java/payara7/glassfish/domains/domain1/logs/server.log"
```

## Los cuatro niveles

Los niveles van en orden. Cada uno oculta al siguiente: hasta que el proyecto no compile no
se puede desplegar, y hasta que no despliegue no se pueden ver los fallos de ejecución.

### Nivel 0. No compila (2 fallos)

`mvn clean install` no llega a construir el archivo `.war`. En NetBeans es `Clean and
Build`; en VS Code, el mismo comando en la terminal o `Lifecycle > install`.

Lea los mensajes de `[ERROR]` completos, con el archivo y la línea de cada uno. Maven los
imprime dos veces, primero al compilar y otra vez en el resumen final, así que en total son
cinco errores distintos. Cinco errores no significan cinco problemas: cuente cuántas causas
hay en realidad.

Nivel terminado cuando la compilación responde `BUILD SUCCESS`.

### Nivel 1. No despliega (1 fallo)

El `.war` ya se construye y las pruebas corren, pero Payara rechaza la aplicación al
desplegarla. Es normal que en este punto haya pruebas en rojo: corresponden a fallos de
niveles posteriores.

Lea la respuesta del despliegue, que menciona una clase concreta y el subsistema que se
quejó. El mismo texto queda en el `server.log`. Después compare esa clase con su
equivalente del módulo de criminales: hay algo que una tiene y la otra no.

Nivel terminado cuando el despliegue responde `Command deploy executed successfully`, o
cuando NetBeans muestra la aplicación desplegada sin errores.

### Nivel 2. Falla al usarlo (3 fallos)

La aplicación levanta, pero la API no responde como debería. Dos de estos tres fallos ya
aparecen señalados en las pruebas; el tercero solo se ve al ejecutar la API.

Para probarla desde VS Code, use las peticiones 5 y 10 de `ws/docs/agents.http`. Desde una
terminal Git Bash:

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

Si trabaja en PowerShell y el POST responde 500 desde el primer intento, revise antes el
punto 2 de la sección de terminal: puede ser el JSON mal formado y no el fallo de la
práctica.

Nivel terminado cuando listar responde 200 y registrar crea el agente.

### Nivel 3. Funciona, pero está mal (2 fallos)

Los dos últimos fallos no producen ningún error. La API responde, los datos se guardan y a
simple vista todo parece correcto. Aun así son fallos, y uno de los dos es el más grave de
la práctica.

Para encontrarlos, compare el comportamiento real contra la tabla de la sección "Qué debe
hacer el módulo cuando esté terminado", y el código contra `CriminalResource`.

En VS Code, las peticiones 4 y 10 de `ws/docs/agents.http` muestran los dos síntomas: el
panel de respuesta trae el código de estado y todas las cabeceras. Desde Git Bash:

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

En NetBeans, clic derecho en el módulo `ws` y `Test`. En VS Code, la vista `Testing`.

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

En VS Code, el equivalente es ejecutar de arriba abajo las peticiones de
`ws/docs/agents.http` y comprobar que cada una devuelve lo que dice su comentario.

## Dónde buscar según el tipo de problema

| Tipo de problema | Dónde mirar |
| --- | --- |
| Compilación | El mensaje de `[ERROR]`: archivo, línea y símbolo que no encuentra |
| Despliegue | La salida del despliegue y el `server.log` |
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
3. La salida de `mvn test` en verde, o la captura de la ventana de pruebas de su IDE.
