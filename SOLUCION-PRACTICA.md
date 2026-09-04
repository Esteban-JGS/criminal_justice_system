# Solución de la práctica: los ocho fallos

Respuestas del ejercicio descrito en [PRACTICA.md](PRACTICA.md). Los fallos estaban
repartidos en cuatro archivos del módulo de agentes.

| # | Nivel | Archivo | Síntoma |
| --- | --- | --- | --- |
| 1 | 0 | `MockAgentRepository` | No compila: método sin implementar |
| 2 | 0 | `AgentService` | No compila: clase inexistente |
| 3 | 1 | `MockAgentRepository` | No despliega: `WELD-001408` |
| 4 | 2 | `AgentResource` | 404 en todas las rutas |
| 5 | 2 | `AgentResource` | 415 al registrar |
| 6 | 2 | `AgentDTO` | 500 al registrar, pero listar funciona |
| 7 | 3 | `AgentResource` | Responde 200 en vez de 201, sin `Location` |
| 8 | 3 | `AgentResource` | Funciona sin token |

---

## 1. Método del contrato sin implementar

`ws/src/main/java/com/fbi/cjs/ws/mocks/MockAgentRepository.java`

**Error**

```text
MockAgentRepository is not abstract and does not override abstract method
deleteById(java.lang.Long) in com.fbi.cjs.ws.repository.AgentRepository
```

**Por qué pasaba.** La clase declara `implements AgentRepository`, y una interfaz es un
contrato obligatorio: hay que implementar todos sus métodos. Faltaba `deleteById`.

**Arreglo**

```java
@Override
public boolean deleteById(Long id) {
    return store.remove(id) != null;
}
```

---

## 2. Excepción con un nombre que no existe

`ws/src/main/java/com/fbi/cjs/ws/service/AgentService.java`

**Error**

```text
cannot find symbol: class ResourceNotFound
```

**Por qué pasaba.** La excepción del proyecto se llama `ResourceNotFoundException`. El
import y las tres llamadas usaban un nombre inventado. Un solo problema producía cuatro
mensajes de error, uno por cada uso.

**Arreglo.** Corregir el import y las tres construcciones, en `findById`, `update` y
`delete`:

```java
import com.fbi.cjs.ws.exception.ResourceNotFoundException;
...
new ResourceNotFoundException("Agente", id)
```

---

## 3. Bean que CDI no encontraba

`ws/src/main/java/com/fbi/cjs/ws/mocks/MockAgentRepository.java`

**Error**, al desplegar:

```text
WELD-001408: Unsatisfied dependencies for type AgentRepository with qualifiers @Default
  at injection point @Inject com.fbi.cjs.ws.service.AgentService.agentRepository
```

**Por qué pasaba.** Faltaba `@ApplicationScoped` en la clase. El archivo `beans.xml` usa
`bean-discovery-mode="annotated"`, así que CDI solo registra las clases que tienen una
anotación de scope. Sin ella la implementación existe, compila y las pruebas pasan, pero es
invisible para la inyección de dependencias.

**Arreglo**

```java
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MockAgentRepository implements AgentRepository {
```

**Por qué ninguna prueba lo detectaba.** Las pruebas crean el repositorio con `new` y llaman
`seed()` a mano, sin pasar por CDI. Las pruebas unitarias no cubren la configuración del
contenedor.

---

## 4. Ruta equivocada

`ws/src/main/java/com/fbi/cjs/ws/rest/AgentResource.java`

**Error.** Todas las rutas de `/api/v1/agents` respondían 404. Prueba en rojo:

```text
AgentResourceTest.rutaDelRecurso: la ruta debe salir de ApiPaths
  expected: <agents> but was: <agent>
```

**Por qué pasaba.** La anotación decía `@Path("agent")`, en singular y escrita a mano. La
URL final se arma con tres partes: el context-root del WAR, el `@ApplicationPath` de
`JaxRsApplication` y el `@Path` del recurso. Con la tercera mal, no hay coincidencia.

**Arreglo**

```java
import com.fbi.cjs.shared.api.ApiPaths;

@Path(ApiPaths.AGENTS)
```

---

## 5. Tipo de contenido equivocado

`ws/src/main/java/com/fbi/cjs/ws/rest/AgentResource.java`

**Error.** El POST respondía `415 Unsupported Media Type`, mientras el GET seguía
funcionando.

**Por qué pasaba.** La clase declaraba `@Consumes(MediaType.TEXT_PLAIN)`. El cliente enviaba
`Content-Type: application/json`, que no coincide con lo que el recurso dice aceptar, y
JAX-RS rechaza la petición antes de ejecutar el método. El GET no se veía afectado porque no
lleva cuerpo.

**Arreglo**

```java
@Consumes(MediaType.APPLICATION_JSON)
```

---

## 6. DTO sin constructor vacío

`shared/src/main/java/com/fbi/cjs/shared/dto/AgentDTO.java`

**Error.** El POST respondía 500. En `server.log`:

```text
jakarta.json.bind.JsonbException: Cannot create instance of a class:
class com.fbi.cjs.shared.dto.AgentDTO, No default constructor found.
```

**Por qué pasaba.** Solo estaba el constructor con parámetros. Para deserializar, JSON-B
crea el objeto vacío y después llama a los setters; sin constructor sin argumentos no puede
instanciarlo. Serializar (el GET) sí funcionaba, porque para eso solo necesita los getters.

**Arreglo**

```java
public AgentDTO() {
}
```

**Por qué ninguna prueba lo detectaba.** Las pruebas construyen los DTO con el constructor
completo, nunca por deserialización de JSON.

---

## 7. Código de estado incorrecto

`ws/src/main/java/com/fbi/cjs/ws/rest/AgentResource.java`

**Error.** El agente se creaba bien, pero la respuesta era `200 OK` y sin cabecera
`Location`. El cuerpo decía `"code":"CREATED","status":201` mientras la cabecera HTTP decía
200: el sobre JSON y el protocolo se contradecían. Prueba en rojo:

```text
AgentResourceTest.createDevuelveResponse
  expected: <jakarta.ws.rs.core.Response> but was: <com.fbi.cjs.shared.api.ApiResponse>
```

**Por qué pasaba.** El método devolvía `ApiResponse<AgentDTO>` directamente. `ApiResponse`
solo arma el cuerpo; quien fija el código de estado real es JAX-RS según lo que devuelve el
método, y con un objeto plano asume 200. Además, sin un `Response` no hay forma de agregar
cabeceras.

**Arreglo**

```java
@POST
@AllowedRoles({Role.SUPERVISOR, Role.JEFE_FBI})
public Response create(@Valid AgentDTO agent, @Context UriInfo uriInfo) {
    AgentDTO created = agentService.create(agent);

    return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(created.getId())).build())
            .entity(ApiResponse.created("Agente registrado correctamente.", created)).build();
}
```

Requiere los imports `jakarta.ws.rs.core.Response`, `jakarta.ws.rs.core.Context` y
`jakarta.ws.rs.core.UriInfo`.

---

## 8. Endpoint desprotegido

`ws/src/main/java/com/fbi/cjs/ws/rest/AgentResource.java`

**Error.** Ninguno visible: `GET /agents` **sin token** devolvía la lista completa, cuando
debía responder 401. Prueba en rojo:

```text
AgentResourceTest.recursoProtegido: sin @Secured cualquiera consulta los agentes
sin autenticarse ==> expected: <true> but was: <false>
```

**Por qué pasaba.** Faltaba `@Secured` en la clase. Los filtros `AuthenticationFilter` y
`AuthorizationFilter` se aplican por `@NameBinding`, es decir, solo a los recursos que llevan
esa anotación. Sin ella, ninguno de los dos se ejecuta.

Detalle importante: los `@AllowedRoles` de los métodos seguían ahí y no hacían nada. No hay
autorización sin autenticación previa.

**Arreglo**

```java
import com.fbi.cjs.ws.security.Secured;

@Path(ApiPaths.AGENTS)
@Secured
public class AgentResource {
```

Es el fallo más grave de los ocho, y el único que no produce ningún error visible.

---

## Detalle práctico: los imports

Al sembrar los fallos, el formateador eliminó de `AgentResource` los imports que quedaron sin
uso: `ApiPaths`, `Secured`, `Response`, `Context` y `UriInfo`. Al corregir los fallos 4, 7 y
8 hay que volver a agregarlos. En el IDE es "organizar imports"; desde la terminal el error
que aparece es `cannot find symbol: class Response`.

## Comprobación final

```bash
mvn clean install
# BUILD SUCCESS, 78 pruebas en verde

asadmin deploy --force=true ws/target/criminal_justice_ws.war
# Command deploy executed successfully
```

```bash
BASE=http://localhost:8080/criminal_justice_ws/api/v1
TOKEN=$(curl -s -X POST $BASE/auth/login -H "Content-Type: application/json" \
  -d '{"username":"supervisor01","password":"1234"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents                                    # 401
curl -s -o /dev/null -w "%{http_code}\n" $BASE/agents -H "Authorization: Bearer $TOKEN"  # 200

curl -s -D- -o /dev/null -X POST $BASE/agents -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"badgeNumber":"FBI-2001","name":"Nuevo","division":"Cibercrimen","status":"ACTIVO"}'
# HTTP/1.1 201 Created
# Location: .../api/v1/agents/6
```
