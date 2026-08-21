# Criminal Justice System

Sistema de gestión de expedientes criminales del FBI, construido como **monorepo Maven**
con dos aplicaciones que se comunican por HTTP:

- **Web service REST** (`ws/`): expone la API, aplica las reglas de negocio y la seguridad.
  Se empaqueta como archivo `.war` y se despliega en **Payara Server**.
- **Cliente de escritorio** (`front/`): aplicación **JavaFX** que consume esa API. No tiene
  datos ni lógica de negocio propios; todo lo pide al web service.

Ambos comparten un tercer módulo, **`shared/`**, con las clases que viajan como JSON entre
uno y otro.

---

## Cómo encajan las piezas

```
┌──────────────────────────────┐         ┌────────────────────────────────────────┐
│   front  (JavaFX)            │         │   Payara Server                        │
│                              │         │  ┌──────────────────────────────────┐  │
│  Vistas FXML                 │         │  │  ws  (criminal_justice_ws.war)   │  │
│      ↓                       │         │  │                                  │  │
│  Controllers                 │  HTTP   │  │   rest/       recursos JAX-RS    │  │
│      ↓                       │  JSON   │  │      ↓                           │  │
│  Services  ──────────────────┼────────→│  │   service/    reglas de negocio  │  │
│      ↓                       │         │  │      ↓                           │  │
│  ApiClient (HttpClient)      │←────────┼──│   repository/ interfaces         │  │
│                              │         │  │      ↓                           │  │
└──────────────────────────────┘         │  │   mocks/      datos en memoria   │  │
                                         │  └──────────────────────────────────┘  │
             ▲                           └────────────────────────────────────────┘
             │                                              ▲
             └───────────────  shared/  ────────────────────┘
                    DTOs y enums: el mismo objeto Java
                    se serializa de un lado y se lee del otro
```

La regla que ordena todo: **el front nunca decide nada importante**. Puede deshabilitar un
botón para no ofrecer algo que va a fallar, pero quien valida datos y permisos siempre es
el web service.

---

## Estructura del repositorio

```
criminal_justice_system/
├── pom.xml                     POM padre (packaging pom): lista los módulos y centraliza
│                               las versiones de librerías y plugins
├── package.json                herramientas de apoyo (formato y validación de commits)
├── biome.json                  configuración del formateador de JSON/JS
├── commitlint.config.js        reglas de los mensajes de commit
├── .husky/                     hooks de git que corren antes de commitear
│
├── shared/                     MÓDULO 1 · contrato entre el WS y el front
│   ├── pom.xml
│   └── src/main/java/com/fbi/cjs/shared/
│       ├── api/                ApiResponse, ResponseCode, ApiPaths
│       ├── dto/                CriminalDTO, UserDTO, RoleDTO, LoginRequestDTO,
│       │                       LoginResponseDTO
│       └── enums/              Role, DangerLevel, CriminalStatus
│
├── ws/                         MÓDULO 2 · API REST (se despliega en Payara)
│   ├── pom.xml
│   ├── docs/                   documentación técnica del web service
│   └── src/
│       ├── main/java/com/fbi/cjs/ws/
│       │   ├── JaxRsApplication.java   arranque de JAX-RS, define el prefijo /api/v1
│       │   ├── rest/           recursos: traducen HTTP a llamadas Java
│       │   ├── service/        reglas de negocio
│       │   ├── repository/     interfaces de acceso a datos
│       │   ├── mocks/          implementación de esas interfaces, en memoria
│       │   ├── security/       token, autenticación y autorización por rol
│       │   ├── exception/      excepciones propias y sus traductores a JSON
│       │   └── filter/         CORS
│       ├── main/webapp/
│       │   ├── index.html      página con el listado de endpoints
│       │   └── WEB-INF/        beans.xml (CDI) y glassfish-web.xml (context-root)
│       └── test/java/          pruebas del web service
│
└── front/                      MÓDULO 3 · cliente de escritorio JavaFX
    ├── pom.xml
    └── src/main/
        ├── java/com/fbi/criminal_justice_system/
        │   ├── App.java        clase principal, abre la ventana de login
        │   ├── controllers/    un controlador por vista
        │   ├── services/       llamadas al web service
        │   └── utils/          infraestructura del cliente
        ├── java/module-info.java   declaración de módulos (JPMS)
        └── resources/
            ├── com/fbi/criminal_justice_system/views/    vistas FXML
            ├── com/fbi/criminal_justice_system/styles/   hoja de estilos
            ├── com/fbi/criminal_justice_system/images/   iconos
            └── config/api.properties                     URL del web service
```

---

## Módulo `shared` · el contrato

Contiene únicamente las clases que viajan por la red. **Sin Jakarta EE, sin JavaFX, sin
lógica de negocio y sin acceso a datos.**

Existe porque el web service serializa un `CriminalDTO` a JSON y el front lo deserializa
al **mismo** `CriminalDTO`. Con una copia de la clase en cada lado, tarde o temprano se
desincronizan y el error aparece en tiempo de ejecución en vez de al compilar.

| Paquete | Clase | Para qué sirve |
| --- | --- | --- |
| `api` | `ApiResponse<T>` | El sobre que envuelve **toda** respuesta de la API |
| `api` | `ResponseCode` | Códigos de la API con su equivalente HTTP |
| `api` | `ApiPaths` | Las rutas (`/auth`, `/criminals`, …) en un solo lugar |
| `dto` | `CriminalDTO` | Un criminal tal como viaja en JSON |
| `dto` | `UserDTO` | Un usuario. La contraseña es solo de entrada, nunca sale |
| `dto` | `RoleDTO` | Rol como elemento de catálogo (valor + etiqueta visible) |
| `dto` | `LoginRequestDTO` / `LoginResponseDTO` | Cuerpo y respuesta del login |
| `enums` | `Role` | `AGENTE`, `SUPERVISOR`, `JEFE_FBI` |
| `enums` | `DangerLevel` | `BAJO`, `MEDIO`, `ALTO` |
| `enums` | `CriminalStatus` | `ACTIVO`, `CAPTURADO`, `FALLECIDO` |

Los enums viajan como su **nombre** (`"SUPERVISOR"`); el método `getLabel()` existe solo
para mostrar el texto en pantalla.

Las anotaciones de validación (`@NotBlank`, `@Size`…) viven en los DTOs, de modo que el
web service valide automáticamente lo que entra por POST y PUT.

---

## Módulo `ws` · el web service

Se empaqueta como `criminal_justice_ws.war`. Las dependencias de Jakarta EE van en scope
`provided` porque Payara ya trae la implementación (Jersey para JAX-RS, Weld para CDI,
Yasson para JSON, Hibernate Validator para las validaciones); empaquetarlas dentro del WAR
provoca conflictos de clases al desplegar.

### Las capas

| Paquete | Responsabilidad | Qué **no** hace |
| --- | --- | --- |
| `rest` | Traducir HTTP: leer parámetros, devolver el código de estado correcto | No contiene reglas de negocio |
| `service` | Reglas de negocio: qué es válido, qué es duplicado, qué es "no encontrado" | No sabe de HTTP ni de JSON |
| `repository` | **Interfaces** de acceso a datos | No tiene implementación |
| `mocks` | Implementa esas interfaces guardando todo en memoria | Nadie fuera de este paquete lo importa |
| `security` | Emitir y validar tokens, autenticar y autorizar | No define quién puede qué: eso lo dicen las anotaciones en `rest` |
| `exception` | Excepciones propias y su traducción a respuestas JSON | |
| `filter` | Cabeceras CORS | |

Los servicios inyectan la **interfaz** del repositorio, nunca una clase concreta. Por eso
cambiar el origen de los datos no obliga a tocar servicios ni recursos REST.

### Clases principales

| Clase | Qué hace |
| --- | --- |
| `JaxRsApplication` | Enciende JAX-RS y fija el prefijo `/api/v1`. Los recursos se descubren por anotaciones, sin `web.xml` |
| `AuthResource` | `POST /auth/login`, `POST /auth/logout`, `GET /auth/me` |
| `CriminalResource` | CRUD de criminales y búsqueda con filtros |
| `UserResource` | CRUD de usuarios |
| `RoleResource` | Catálogo de roles, solo lectura |
| `HealthResource` | `GET /health`, público: confirma que el WAR está desplegado |
| `TokenStore` | Emite tokens opacos con 8 horas de vigencia y los valida |
| `AuthenticationFilter` | Lee `Authorization: Bearer …` y rechaza con 401 si el token no vale |
| `AuthorizationFilter` | Aplica `@AllowedRoles` y rechaza con 403 |
| `MockData` | Los datos iniciales: 10 criminales y 3 usuarios |

### Recorrido de una petición

`POST /api/v1/criminals` con un token de supervisor:

1. **`AuthenticationFilter`** lee la cabecera `Authorization`, valida el token contra
   `TokenStore` y deja el usuario disponible para el resto de la petición.
   Token ausente o vencido → **401**.
2. **`AuthorizationFilter`** mira el `@AllowedRoles` del método. Rol insuficiente → **403**.
3. **Bean Validation** revisa el `CriminalDTO` según las anotaciones del DTO.
   Datos inválidos → **422** con la lista de campos que fallaron.
4. **`CriminalResource.create()`** recibe el objeto ya validado y llama al servicio.
5. **`CriminalService`** descarta el id que haya mandado el cliente, recorta espacios
   sobrantes y delega en el repositorio.
6. **`MockCriminalRepository`** asigna el id y guarda.
7. La respuesta sale como **201**, con la cabecera `Location` del recurso creado y el
   sobre `ApiResponse` en el cuerpo.

Si algo lanza una excepción, un `ExceptionMapper` la convierte al mismo formato JSON: por
eso los recursos no llevan `try/catch`.

---

## Módulo `front` · el cliente de escritorio

Aplicación JavaFX modular (`module-info.java`). Usa `java.net.http.HttpClient`, que viene
en el JDK, y Jackson para convertir JSON a objetos.

### Paquetes

| Paquete | Contenido |
| --- | --- |
| `controllers` | Un controlador por vista FXML |
| `services` | `AuthService`, `CriminalService`, `UserService`: una llamada HTTP por método |
| `utils` | Infraestructura: cliente HTTP, sesión, navegación, utilidades de interfaz |
| `resources/views` | Las vistas FXML |
| `resources/config` | `api.properties` con la URL del web service |

### Clases de `utils`

| Clase | Qué hace |
| --- | --- |
| `ApiClient` | Punto único de salida: URL base, cabecera `Authorization`, timeout, conversión JSON y traducción de errores HTTP a `ApiException` |
| `ApiException` | Error de la API: código HTTP y, si fue de validación, los campos que fallaron |
| `Session` | Token y datos del usuario conectado, en memoria |
| `BackgroundTask` | Ejecuta las llamadas al web service fuera del hilo de JavaFX |
| `FlowController` | Navegación entre vistas: pantalla completa, ventana o modal |
| `Mensaje` | Diálogos de aviso y confirmación |
| `ComboBoxUtils` | Combos que guardan un enum o DTO y muestran una etiqueta legible |
| `CriminalTable` | Configuración de las columnas de una tabla de criminales |
| `RequestGuard` | Descarta las respuestas de búsquedas que llegan tarde |
| `AppContext` | Mapa global para compartir estado entre pantallas |

### Ciclo de vida de una vista

Cada controlador extiende `Controller` y tiene dos momentos bien diferenciados:

| Método | Quién lo llama | Cuándo | Qué va ahí |
| --- | --- | --- | --- |
| `initialize()` | JavaFX | Una sola vez, al cargar el FXML | Columnas de tablas, listeners, contenido de combos |
| `onViewShown()` | `FlowController` | Cada vez que se muestra la vista | Consultas al servidor, limpiar el formulario, permisos |

Poner una consulta al servidor en `initialize()` la ejecutaría dos veces la primera vez
que se abre la pantalla.

### Recorrido de una acción

Pulsar **Buscar** en el listado de criminales:

1. El controlador arma el filtro y pide un turno a `RequestGuard`.
2. `BackgroundTask` lanza la llamada **en otro hilo**. La ventana sigue respondiendo.
3. `CriminalService` construye la URL con los filtros como query params.
4. `ApiClient` agrega la cabecera `Authorization` con el token de `Session`, envía la
   petición y convierte el JSON en `List<CriminalDTO>`.
5. La respuesta vuelve **al hilo de JavaFX**. Si el turno sigue siendo el último, la lista
   observable se reemplaza y la tabla se repinta sola.
6. Si el servidor respondió un error se muestra el mensaje; si fue un 401, la sesión se
   limpia y la aplicación vuelve al login.

---

## Requisitos

| Herramienta | Versión | Cómo comprobarlo |
| --- | --- | --- |
| JDK | 25 | `java -version` |
| Maven | 3.9 o superior | `mvn -v` |
| Payara Server | 7.2026.7 (Jakarta EE 11) | `asadmin version` |
| Node.js | opcional, solo para las herramientas de formato | `node -v` |

En Windows, `asadmin` está en `C:\Program Files\Java\payara7\bin\asadmin.bat`. Conviene
agregar esa carpeta al `PATH` para poder escribir solo `asadmin`.

Para usar **Payara 6** (Jakarta EE 10), en el `pom.xml` de la raíz cambiar
`jakartaee.version` a `10.0.0` y `java.version` a `21`. El código no cambia.

---

## Puesta en marcha

### Paso 1 · Compilar el monorepo

Desde la raíz del proyecto:

```bash
mvn clean install
```

Compila los tres módulos en orden (`shared` → `ws` → `front`) y ejecuta las pruebas. Maven
deduce ese orden del grafo de dependencias.

Al terminar deben existir:

- `ws/target/criminal_justice_ws.war`
- `front/target/criminal-justice-system-front-1.0.jar`

### Paso 2 · Arrancar el servidor Payara

```bash
asadmin start-domain domain1
```

Tarda unos segundos. Para comprobar que quedó arriba:

```bash
asadmin list-domains
```

Debe decir `domain1 running`. La consola de administración queda en
`http://localhost:4848`.

### Paso 3 · Desplegar el web service

```bash
asadmin deploy --force=true ws/target/criminal_justice_ws.war
```

Alternativa gráfica: en `http://localhost:4848`, ir a **Applications → Deploy**, elegir el
archivo `.war` y confirmar.

### Paso 4 · Comprobar que la API responde

```bash
curl http://localhost:8080/criminal_justice_ws/api/v1/health
```

Respuesta esperada:

```json
{
  "code": "OK",
  "data": {
    "application": "criminal-justice-system-ws",
    "apiVersion": "/api/v1",
    "dataSource": "MOCK",
    "serverTime": "...",
    "javaVersion": "25.0.4"
  },
  "message": "El servicio está arriba.",
  "status": 200,
  "success": true
}
```

En `http://localhost:8080/criminal_justice_ws/` hay una página con el listado completo de
endpoints.

### Paso 5 · Arrancar el cliente de escritorio

En otra terminal, desde la raíz del proyecto:

```bash
mvn -pl front javafx:run
```

Se abre la ventana de login. Entrar con cualquiera de los usuarios de la tabla siguiente.

### Al terminar

```bash
asadmin stop-domain domain1
```

La aplicación queda desplegada: al volver a arrancar el dominio está disponible otra vez,
con los datos en su estado inicial.

---

## Usuarios y permisos

Contraseña de todos: `1234`

| Usuario | Rol | Criminales | Usuarios |
| --- | --- | --- | --- |
| `agente01` | `AGENTE` | consultar y buscar | sin acceso |
| `supervisor01` | `SUPERVISOR` | consultar, crear y editar | consultar |
| `jefe01` | `JEFE_FBI` | consultar, crear, editar y eliminar | crear, editar y eliminar |

Ningún usuario puede cambiarse el rol a sí mismo, desactivarse ni eliminarse: el sistema
podría quedarse sin nadie capaz de administrarlo.

---

## Pantallas del cliente

| Pantalla | Qué permite hacer | Quién la usa |
| --- | --- | --- |
| **Login** | Autenticarse contra el web service | todos |
| **Dashboard** | Ver los datos de la sesión y navegar al resto de pantallas | todos |
| **Criminales** | Listar, buscar por texto, registrar, editar y eliminar | ver: todos · editar: `SUPERVISOR` y `JEFE_FBI` · eliminar: `JEFE_FBI` |
| **Búsqueda avanzada** | Combinar texto libre con estatus y peligrosidad | todos |
| **Usuarios** | Listar, crear, editar y eliminar usuarios | ver: `SUPERVISOR` y `JEFE_FBI` · modificar: `JEFE_FBI` |

Los formularios de registro y edición se abren como ventana modal sobre el listado.

---

## Referencia de la API

Base: `http://localhost:8080/criminal_justice_ws/api/v1`

| Método | Ruta | Acceso | Descripción |
| --- | --- | --- | --- |
| GET | `/health` | público | Estado del servicio |
| POST | `/auth/login` | público | Devuelve token y usuario |
| POST | `/auth/logout` | token | Invalida el token |
| GET | `/auth/me` | token | Usuario de la sesión actual |
| GET | `/criminals` | token | Lista. Filtros: `search`, `status`, `dangerLevel` |
| GET | `/criminals/{id}` | token | Un criminal |
| POST | `/criminals` | `SUPERVISOR`, `JEFE_FBI` | Registrar |
| PUT | `/criminals/{id}` | `SUPERVISOR`, `JEFE_FBI` | Actualizar |
| DELETE | `/criminals/{id}` | `JEFE_FBI` | Eliminar |
| GET | `/users` | `SUPERVISOR`, `JEFE_FBI` | Lista de usuarios |
| GET | `/users/{id}` | `SUPERVISOR`, `JEFE_FBI` | Un usuario |
| POST | `/users` | `JEFE_FBI` | Crear |
| PUT | `/users/{id}` | `JEFE_FBI` | Actualizar |
| DELETE | `/users/{id}` | `JEFE_FBI` | Eliminar |
| GET | `/roles` | token | Catálogo de roles |

### El sobre de respuesta

Todas las respuestas tienen la misma forma, tanto en éxito como en error:

```json
{
  "status": 200,
  "code": "OK",
  "message": "Se encontraron 10 criminales.",
  "data": [ ... ],
  "errors": null,
  "success": true
}
```

| Campo | Contenido |
| --- | --- |
| `status` | Código HTTP |
| `code` | El mismo código como texto (`OK`, `NOT_FOUND`, `CONFLICT`…) |
| `message` | Mensaje legible, apto para mostrar al usuario |
| `data` | El contenido pedido, o `null` si hubo error |
| `errors` | Lista de errores de validación, o `null` |
| `success` | `true` si el `status` es 2xx |

### Códigos que devuelve

| Código | Significa |
| --- | --- |
| `200` | Operación correcta |
| `201` | Recurso creado (incluye cabecera `Location`) |
| `401` | Falta el token, o está vencido o revocado |
| `403` | El rol no alcanza para esa operación |
| `404` | El recurso no existe |
| `409` | Conflicto: nombre de usuario duplicado, o intento de quitarse el acceso a sí mismo |
| `422` | Datos inválidos; el detalle viene en `errors` |

### Probar la API con curl

```bash
BASE=http://localhost:8080/criminal_justice_ws/api/v1

# 1. Iniciar sesión y obtener el token
curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jefe01","password":"1234"}'

TOKEN=pegar-aqui-el-token-devuelto

# 2. Listar criminales
curl -s $BASE/criminals -H "Authorization: Bearer $TOKEN"

# 3. Buscar con filtros combinados
curl -s "$BASE/criminals?search=cobra&status=ACTIVO&dangerLevel=ALTO" \
  -H "Authorization: Bearer $TOKEN"

# 4. Registrar un criminal
curl -s -X POST $BASE/criminals \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Nuevo Sospechoso","alias":"El Nuevo","crime":"Contrabando","dangerLevel":"MEDIO","status":"ACTIVO"}'

# 5. Actualizar (el id va en la URL, no en el cuerpo)
curl -s -X PUT $BASE/criminals/11 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Nuevo Sospechoso","alias":"El Nuevo","crime":"Contrabando agravado","dangerLevel":"ALTO","status":"CAPTURADO"}'

# 6. Eliminar
curl -s -X DELETE $BASE/criminals/11 -H "Authorization: Bearer $TOKEN"

# 7. Cerrar sesión
curl -s -X POST $BASE/auth/logout -H "Authorization: Bearer $TOKEN"
```

---

## Seguridad

El login devuelve un **token opaco** con 8 horas de vigencia, que el cliente envía en cada
petición:

```
Authorization: Bearer <token>
```

Dos filtros de JAX-RS lo procesan, y solo sobre los recursos anotados con `@Secured`:

1. **Autenticación** (`AuthenticationFilter`) responde el *quién sos*. Sin token válido,
   **401**.
2. **Autorización** (`AuthorizationFilter`) responde el *podés hacer esto*. Lee la
   anotación `@AllowedRoles` del método o de la clase y, si el rol no está en la lista,
   **403**.

`@AllowedRoles` es una anotación propia del proyecto y no la estándar
`jakarta.annotation.security.@RolesAllowed`, porque Payara intercepta esa última y la
valida contra su propio sistema de seguridad; como aquí la identidad la maneja
`TokenStore`, el contenedor respondería un 401 en HTML antes de llegar al método.

La contraseña nunca sale en una respuesta: el web service la pone en `null` antes de
responder.

---

## Configuración

### URL del web service

El cliente la lee de `front/src/main/resources/config/api.properties`:

```properties
api.baseUrl=http://localhost:8080/criminal_justice_ws/api/v1
api.timeoutSeconds=15
```

Se puede cambiar sin recompilar, en este orden de prioridad:

```bash
# 1. Propiedad del sistema
mvn -pl front javafx:run -Dcjs.api.url=http://192.168.1.50:8080/criminal_justice_ws/api/v1

# 2. Variable de entorno
set CJS_API_URL=http://192.168.1.50:8080/criminal_justice_ws/api/v1
```

### Versiones de Java y Jakarta EE

Ambas están en las propiedades del `pom.xml` de la raíz:

```xml
<java.version>25</java.version>
<jakartaee.version>11.0.0</jakartaee.version>
```

Al cambiarlas, los tres módulos las heredan.

### Datos de prueba

Están en `ws/src/main/java/com/fbi/cjs/ws/mocks/MockData.java`. Viven en memoria mientras
el WAR esté desplegado: al redesplegar vuelven a su estado inicial.

---

## Pruebas

```bash
mvn test
```

50 pruebas con JUnit 5. Corren en menos de un segundo, sin Payara y sin base de datos.

| Módulo | Qué cubre |
| --- | --- |
| `shared` | El sobre `ApiResponse`: códigos, datos y errores de validación |
| `ws` · repositorios | El contrato de `CriminalRepository` y `UserRepository` |
| `ws` · servicios | Reglas de negocio: unicidad, normalización, 404 y auto-bloqueo |
| `ws` · seguridad | Emisión, validación, revocación y vencimiento de tokens |

Las pruebas de repositorio son **contratos abstractos** (`CriminalRepositoryContractTest`,
`UserRepositoryContractTest`): describen el comportamiento que debe cumplir cualquier
implementación, sin importar de dónde vengan los datos. Para verificar una implementación
nueva basta con extenderlas:

```java
class OtraImplementacionTest extends CriminalRepositoryContractTest {
    @Override
    protected CriminalRepository newRepository() {
        return unRepositorioConLosDiezRegistrosDePrueba();
    }
}
```

En las pruebas los servicios se arman a mano (`service.criminalRepository = ...`) sin
levantar CDI; por eso los campos inyectados no son `private`.

Para ejecutar una sola clase:

```bash
mvn test -Dtest=UserServiceTest
```

---

## Formato y convenciones

```bash
mvn spotless:apply     # formatea todo el código Java del monorepo
mvn spotless:check     # falla si algo no está formateado
npm run format         # equivalente a spotless:apply
```

Los hooks de git (Husky) ejecutan el formateador antes de cada commit y validan que el
mensaje siga **Conventional Commits**:

```
feat(front): agregar la pantalla de busqueda avanzada
fix(ws): corregir el filtro de estatus
docs: actualizar la guia de despliegue
```

Tipos aceptados: `feat`, `fix`, `docs`, `chore`, `style`, `refactor`, `ci`, `test`,
`revert`, `perf`.

---

## Solución de problemas

**`Application name criminal_justice_ws is already in use` al desplegar.**
Queda una carpeta de la aplicación sin registro en el dominio, normalmente porque se borró
el WAR mientras estaba desplegado. Con el dominio detenido:

```bash
asadmin stop-domain domain1
rm -rf "C:/Program Files/Java/payara7/glassfish/domains/domain1/applications/criminal_justice_ws"
asadmin start-domain domain1
asadmin deploy ws/target/criminal_justice_ws.war
```

**`Unable to load class ...Filter` en `server.log`.**
Es un aviso del escáner de anotaciones del contenedor web, que intenta cargar como filtros
de servlet unas clases que son filtros de JAX-RS. No afecta al funcionamiento: se confirma
comprobando que `/criminals` sin token responde 401 y que un agente recibe 403 al crear.

**El cliente dice "No se pudo conectar con el servidor del FBI".**
El web service no está desplegado, o el cliente apunta a otra URL. Verificar en este orden:

```bash
asadmin list-domains                                              # ¿está arriba el dominio?
curl http://localhost:8080/criminal_justice_ws/api/v1/health      # ¿responde la API?
```

Y revisar `api.baseUrl` en `front/src/main/resources/config/api.properties`.

**El cliente responde 401 en todas las pantallas.**
El token venció (8 horas) o se redesplegó el WAR, lo que vacía las sesiones activas. Basta
con volver a iniciar sesión.

**El puerto 8080 está ocupado.**
Cambiar el del dominio:

```bash
asadmin set server-config.network-config.network-listeners.network-listener.http-listener-1.port=8181
```

Después hay que actualizar `api.baseUrl` en el archivo de configuración del cliente.
