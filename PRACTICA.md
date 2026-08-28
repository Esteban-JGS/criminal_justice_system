# Práctica · El CRUD de Agentes no funciona

Un compañero dejó a medias el módulo de **agentes** del web service: agentes del FBI con
placa, división y estado. Escribió todo el código —DTO, repositorio, servicio y recurso
REST— siguiendo los mismos patrones que ya existen para criminales y usuarios.

El problema es que **no funciona**. Ni siquiera compila.

Tu trabajo no es reescribirlo: es **averiguar por qué falla cada cosa y arreglarlo**.

---

## Reglas del juego

1. **Prohibido borrar un archivo y escribirlo de nuevo desde cero.** Se corrige el fallo,
   no se reemplaza el archivo. Si lo reescribís, no aprendiste a diagnosticar.
2. **Prohibido tocar las pruebas.** Los tests describen cómo debe comportarse el código; si
   los cambiás para que pasen, estás falsificando el resultado.
3. **No toques los criminales ni los usuarios.** Ese código funciona y es tu mejor fuente
   de consulta: casi todas las respuestas están ahí.
4. Por cada fallo tenés que **anotar en la bitácora** qué síntoma viste, cuál era la causa
   y cómo lo arreglaste. Eso vale tanto como el código.

---

## Qué debe hacer el módulo cuando esté listo

Base de la API: `http://localhost:8080/criminal_justice_ws/api/v1`

| Método | Ruta | Quién puede | Respuesta correcta |
|---|---|---|---|
| GET | `/agents` | cualquiera con token | 200 con la lista de 5 agentes |
| GET | `/agents?search=texto` | cualquiera con token | 200, filtra por nombre, placa o división |
| GET | `/agents?status=ACTIVO` | cualquiera con token | 200, filtra por estado |
| GET | `/agents/{id}` | cualquiera con token | 200, o 404 si no existe |
| POST | `/agents` | SUPERVISOR, JEFE_FBI | **201** con la cabecera `Location` |
| PUT | `/agents/{id}` | SUPERVISOR, JEFE_FBI | 200, o 404 si no existe |
| DELETE | `/agents/{id}` | JEFE_FBI | 200, o 404 si no existe |

Reglas de negocio:

- La placa tiene formato `FBI-0000` y **no se puede repetir** entre agentes (409).
- Sin token, cualquier ruta de `/agents` responde **401**.
- Con un rol insuficiente, responde **403**.
- Los datos inválidos responden **422** con el detalle de los campos.

Datos de prueba (contraseña `1234` para todos): `agente01`, `supervisor01`, `jefe01`.

---

## Preparación

```bash
# 1. Traer el código
git pull

# 2. Crear tu rama de trabajo
git checkout -b practica-agentes-TUNOMBRE

# 3. Intentar compilar (va a fallar: ahí empieza el ejercicio)
mvn clean install
```

Para desplegar y probar:

```bash
asadmin start-domain domain1
asadmin deploy --force=true ws/target/criminal_justice_ws.war
```

El log del servidor, que vas a necesitar, está en:

```
C:\Program Files\Java\payara7\glassfish\domains\domain1\logs\server.log
```

---

## Los cuatro niveles

Van en orden: no podés ver el síntoma del nivel 2 hasta que el nivel 1 esté resuelto.

### Nivel 0 · No compila · 2 fallos

`mvn clean install` no llega a construir el `.war`.

**Qué hacer:** leé los mensajes de `[ERROR]` completos, no solo la última línea. Fijate en
qué archivo y en qué línea ocurre cada uno. Ojo: **cinco mensajes de error no significan
cinco problemas**; contá cuántas causas distintas hay en realidad.

**Terminaste este nivel cuando:** `mvn clean install` dice `BUILD SUCCESS`.

### Nivel 1 · No despliega · 1 fallo

El `.war` se construye y `mvn test` corre, pero Payara rechaza la aplicación al
desplegarla.

**Qué hacer:** leé la respuesta del comando `asadmin deploy` y buscá el mismo texto en
`server.log`. El mensaje menciona una clase concreta y una palabra clave que te dice qué
subsistema se quejó. Compará esa clase con su equivalente del módulo de criminales:
**algo que una tiene, la otra no**.

**Terminaste este nivel cuando:** `asadmin deploy` dice `Command deploy executed successfully`.

### Nivel 2 · Revienta al usarlo · 3 fallos

La aplicación levanta, pero la API no responde como debería. Estos tres los vas a
encontrar probando con `curl`:

```bash
BASE=http://localhost:8080/criminal_justice_ws/api/v1
TOKEN=$(curl -s -X POST $BASE/auth/login -H "Content-Type: application/json" \
  -d '{"username":"supervisor01","password":"1234"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# a) Listar
curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents -H "Authorization: Bearer $TOKEN"

# b) Registrar
curl -s -o /dev/null -w "%{http_code}\n" -X POST $BASE/agents \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"badgeNumber":"FBI-2001","name":"Nuevo Agente","division":"Cibercrimen","status":"ACTIVO"}'
```

Los síntomas que vas a ver, en este orden:

| Síntoma | Pista |
|---|---|
| La lista responde **404** aunque la aplicación está desplegada | ¿Cómo se arma la URL completa de un recurso? Son tres pedazos concatenados |
| Registrar responde **415 Unsupported Media Type** | El servidor te está diciendo que no acepta lo que le mandaste. ¿Qué formato dice aceptar? |
| Registrar responde **500**, pero listar sigue funcionando bien | Leer funciona y escribir no: ¿qué hace el servidor al recibir JSON que no hace al enviarlo? El `server.log` tiene la respuesta exacta |

**Terminaste este nivel cuando:** listar responde 200 y registrar crea el agente.

### Nivel 3 · Funciona, pero está mal · 2 fallos

Los dos últimos **no producen ningún error**. La API responde, los datos se guardan y a
simple vista todo parece correcto. Aun así, ambos son bugs, y uno de los dos es el más
grave de toda la práctica.

Para encontrarlos, comparate contra la tabla de "Qué debe hacer el módulo" y contra el
recurso de criminales.

Dos comandos que ayudan:

```bash
# Mirá las cabeceras completas de la respuesta, no solo el cuerpo
curl -s -D- -o /dev/null -X POST $BASE/agents \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"badgeNumber":"FBI-2002","name":"Otro Agente","division":"Forense","status":"ACTIVO"}'

# Probá qué pasa SIN token, y compará con el mismo caso en /criminals
curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents
curl -s -o /dev/null -w "%{http_code}\n" $BASE/criminals
```

**Terminaste este nivel cuando:** `mvn test` está completamente en verde.

---

## Tu semáforo: las pruebas

```bash
mvn test
```

Hay 28 pruebas del módulo de agentes. Al empezar no corren siquiera, porque el proyecto no
compila; después irán pasando de rojo a verde a medida que arregles.

Leé el mensaje de cada fallo: están escritos para decirte **qué se espera**, no solo que
algo no coincide. Por ejemplo:

```
AgentResourceTest.createDevuelveResponse
  un POST que crea algo responde 201 y el header Location
  expected: <jakarta.ws.rs.core.Response> but was: <com.fbi.cjs.shared.api.ApiResponse>
```

Cuidado: **hay dos fallos que ninguna prueba detecta.** Que `mvn test` esté verde no
significa que hayas terminado; también tiene que desplegar y comportarse como dice la tabla.
Pensá por qué una prueba unitaria no puede ver esos dos.

---

## Bitácora (se entrega)

Completá esta tabla con los ocho fallos. Es la parte que más peso tiene en la nota.

| # | Archivo | Síntoma que observé | Causa real | Cómo lo arreglé | Cómo comprobé que quedó bien |
|---|---|---|---|---|---|
| 1 | | | | | |
| 2 | | | | | |
| 3 | | | | | |
| 4 | | | | | |
| 5 | | | | | |
| 6 | | | | | |
| 7 | | | | | |
| 8 | | | | | |

Además, respondé en dos o tres líneas cada una:

1. ¿Por qué el fallo del nivel 1 no lo detecta ninguna prueba unitaria?
2. De los dos fallos del nivel 3, ¿cuál te parece más peligroso en un sistema real y por qué?
3. Si tuvieras que evitar que estos ocho fallos vuelvan a pasar, ¿qué agregarías al proyecto?

---

## Criterios de aceptación

Antes de entregar, esto tiene que dar exactamente esto:

```bash
mvn clean install                  # BUILD SUCCESS, todas las pruebas en verde
asadmin deploy --force=true ws/target/criminal_justice_ws.war   # deploy successful
```

```bash
BASE=http://localhost:8080/criminal_justice_ws/api/v1
TOKEN=...   # el de supervisor01

curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents                              # 401
curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents -H "Authorization: Bearer $TOKEN"   # 200
curl -s -D- -o /dev/null -X POST $BASE/agents -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"badgeNumber":"FBI-2001","name":"Nuevo","division":"Cibercrimen","status":"ACTIVO"}'
# 201 Created + Location: .../agents/6
```

---

## Dónde mirar cuando te atores

| Si el problema es de… | Mirá… |
|---|---|
| Compilación | El mensaje de `[ERROR]`: archivo, línea y nombre del símbolo |
| Despliegue | La salida de `asadmin deploy` y `server.log` |
| Una ruta que da 404 | `JaxRsApplication`, el `@Path` del recurso y `ApiPaths` |
| Un 4xx al mandar datos | Las anotaciones de la clase del recurso |
| Un 500 | `server.log`: la causa real está en el `Caused by:` |
| Permisos y token | `@Secured` y `@AllowedRoles` en `CriminalResource` |

Y la regla que resuelve la mayoría: **abrí el archivo equivalente de criminales o usuarios
y comparalo línea por línea con el de agentes.**

---

## Extensión opcional

Si terminás antes, agregá al módulo de agentes un filtro por división:

```
GET /agents?division=Cibercrimen
```

Tiene que funcionar combinado con los filtros que ya existen (`search` y `status`), y
necesita su propia prueba en `AgentRepositoryContractTest`. Fijate cómo está resuelto el
filtro por nivel de peligrosidad en criminales.

---

## Entrega

1. Tu rama con los ocho fallos corregidos, commits con mensajes en formato
   Conventional Commits (`fix(ws): ...`).
2. La bitácora completa y las tres preguntas respondidas.
3. La salida de `mvn test` en verde.
