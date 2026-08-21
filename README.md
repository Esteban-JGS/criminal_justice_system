# Criminal Justice System

Monorepo del sistema del FBI: una **API REST (JAX-RS)** desplegada en **Payara** y un
**cliente de escritorio JavaFX** que la consume por HTTP.

Los datos son **mocks en memoria**, aislados detrás de interfaces de repositorio para que
cambiarlos por Oracle no obligue a tocar el resto del código
(ver [ws/docs/oracle-migration.md](ws/docs/oracle-migration.md)).

## Estructura

```
criminal_justice_system/
├── pom.xml                  POM padre: módulos + versiones centralizadas
├── shared/                  DTOs y enums que viajan como JSON (sin Jakarta, sin JavaFX)
├── ws/                      WAR con la API REST -> se despliega en Payara
│   ├── src/main/java/com/fbi/cjs/ws/
│   │   ├── rest/            Recursos JAX-RS (traducen HTTP <-> Java)
│   │   ├── service/         Reglas de negocio
│   │   ├── repository/      Interfaces de acceso a datos
│   │   ├── mocks/           Implementación en memoria  <-- se borra al migrar a Oracle
│   │   ├── security/        Token, filtros de autenticación y autorización
│   │   ├── exception/       Excepciones + ExceptionMappers
│   │   └── filter/          CORS
│   └── docs/oracle-migration.md
├── front/                   App JavaFX (cliente del WS)
│   └── src/main/java/com/fbi/criminal_justice_system/
│       ├── controllers/     Controladores de las vistas FXML
│       ├── services/        Llamadas al WS
│       └── utils/           ApiClient, Session, BackgroundTask, FlowController
└── ejemplo-TareaProgra3WS-main/   Proyecto SOAP de referencia (no se compila)
```

`ejemplo-TareaProgra3WS-main/` no está en `<modules>`: queda como material de consulta.

## Requisitos

| Herramienta | Versión | Notas |
|---|---|---|
| JDK | 25 | probado con Oracle JDK 25.0.4 |
| Maven | 3.9+ | |
| Payara Server | 7.2026.7 | Jakarta EE 11 (JAX-RS 4.0) |

El WAR se compila y despliega correctamente sobre esa combinación. Para Payara 6
(Jakarta EE 10), en el `pom.xml` de la raíz poné `jakartaee.version` en `10.0.0` y
`java.version` en `21`; el código no cambia.

## Construir

```bash
mvn clean install
```

Maven deduce el orden `shared → ws → front` del grafo de dependencias.

Artefactos: `ws/target/criminal_justice_ws.war` y
`front/target/criminal-justice-system-front-1.0.jar`.

## 1) Desplegar el web service

```bash
asadmin start-domain domain1
asadmin deploy --force=true ws/target/criminal_justice_ws.war
```

En Windows, `asadmin` está en `C:\Program Files\Java\payara7\bin\asadmin.bat`. También se
puede desplegar desde la consola web `http://localhost:4848` → **Applications → Deploy**.

Comprobación rápida (endpoint público):

```bash
curl http://localhost:8080/criminal_justice_ws/api/v1/health
```

En `http://localhost:8080/criminal_justice_ws/` hay una página con todos los endpoints.

## 2) Arrancar el front

```bash
mvn -pl front javafx:run
```

La URL del WS vive en `front/src/main/resources/config/api.properties` y se puede
sobreescribir sin recompilar:

```bash
mvn -pl front javafx:run -Dcjs.api.url=http://192.168.1.50:8080/criminal_justice_ws/api/v1
```

Usuarios de prueba (contraseña `1234`):

| Usuario | Rol | Puede |
|---|---|---|
| `agente01` | AGENTE | consultar criminales |
| `supervisor01` | SUPERVISOR | + crear y editar criminales, ver usuarios |
| `jefe01` | JEFE_FBI | + eliminar criminales y administrar usuarios |

## API

Base: `http://localhost:8080/criminal_justice_ws/api/v1`

| Método | Ruta | Acceso |
|---|---|---|
| GET | `/health` | público |
| POST | `/auth/login` | público |
| POST | `/auth/logout` | token |
| GET | `/auth/me` | token |
| GET | `/criminals?search=&status=&dangerLevel=` | token |
| GET | `/criminals/{id}` | token |
| POST | `/criminals` | SUPERVISOR, JEFE_FBI |
| PUT | `/criminals/{id}` | SUPERVISOR, JEFE_FBI |
| DELETE | `/criminals/{id}` | JEFE_FBI |
| GET | `/users`, `/users/{id}` | SUPERVISOR, JEFE_FBI |
| POST, PUT, DELETE | `/users` | JEFE_FBI |
| GET | `/roles` | token |

Toda respuesta usa el mismo sobre:

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

Códigos: `200` OK, `201` creado, `401` sin token o token vencido, `403` rol insuficiente,
`404` no existe, `409` duplicado, `422` datos inválidos.

### Probar con curl

```bash
BASE=http://localhost:8080/criminal_justice_ws/api/v1

# 1. Login -> token
curl -s -X POST $BASE/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jefe01","password":"1234"}'

TOKEN=pega-aqui-el-token

# 2. Listar y filtrar
curl -s $BASE/criminals -H "Authorization: Bearer $TOKEN"
curl -s "$BASE/criminals?search=cobra&status=ACTIVO" -H "Authorization: Bearer $TOKEN"

# 3. Crear
curl -s -X POST $BASE/criminals \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"name":"Nuevo Sospechoso","alias":"El Nuevo","crime":"Contrabando","dangerLevel":"MEDIO","status":"ACTIVO"}'

# 4. Eliminar (solo JEFE_FBI)
curl -s -X DELETE $BASE/criminals/11 -H "Authorization: Bearer $TOKEN"
```

### Seguridad

El token es opaco y vive en memoria (`TokenStore`), con 8 horas de vigencia. Dos filtros
JAX-RS lo procesan: uno autentica (401) y otro autoriza según `@AllowedRoles` (403).

Se usa `@AllowedRoles` propio y no `jakarta.annotation.security.@RolesAllowed` porque
Payara intercepta esa última y la valida contra su propio sistema de seguridad; como la
identidad la maneja el `TokenStore`, el contenedor respondía un 401 en HTML antes de
llegar al método.

## REST vs SOAP (el ejemplo de referencia)

| | SOAP (`ejemplo-TareaProgra3WS-main`) | REST (este proyecto) |
|---|---|---|
| Transporte | XML sobre POST siempre | JSON, un verbo HTTP por operación |
| Contrato | WSDL generado | URLs + códigos HTTP |
| Registro | `sun-jaxws.xml` + un servlet por servicio | `@ApplicationPath` y `@Path`, sin XML |
| Operación | el verbo va en el nombre (`crearProyecto`) | el verbo es HTTP (`POST /criminals`) |
| Errores | SOAP Fault | código HTTP + cuerpo JSON |
| Runtime | Metro empaquetado en el WAR | Jersey, ya incluido en Payara |

## Pruebas

```bash
mvn test
```

50 pruebas con JUnit 5, sin necesidad de Payara ni de base de datos: corren en menos de un
segundo contra los mocks en memoria.

| Dónde | Qué cubre |
|---|---|
| `shared` | el sobre `ApiResponse`: códigos, datos y errores de validación |
| `ws` · repositorios | el contrato de `CriminalRepository` y `UserRepository` |
| `ws` · servicios | reglas de negocio: unicidad, normalización, 404, auto-bloqueo |
| `ws` · seguridad | emisión, validación, revocación y vencimiento de tokens |

Las pruebas de repositorio son **contratos abstractos**
(`CriminalRepositoryContractTest`, `UserRepositoryContractTest`): declaran lo que debe
cumplir cualquier implementación. Cuando exista `JpaCriminalRepository`, basta con
extenderlas para comprobar que Oracle se comporta igual que los mocks:

```java
class JpaCriminalRepositoryTest extends CriminalRepositoryContractTest {
    @Override
    protected CriminalRepository newRepository() {
        return unRepositorioJpaConLosDiezRegistrosDePrueba();
    }
}
```

Los servicios se arman a mano en las pruebas (`service.criminalRepository = ...`), sin CDI.
Por eso los campos inyectados no son `private`.

## Problemas comunes

**`Application name criminal_justice_ws is already in use` al desplegar.** Pasa si se borró
el WAR (por ejemplo con `mvn clean`) mientras la aplicación estaba desplegada: queda la
carpeta sin registro en el dominio. Se resuelve borrándola con el dominio detenido:

```bash
asadmin stop-domain domain1
rm -rf "C:/Program Files/Java/payara7/glassfish/domains/domain1/applications/criminal_justice_ws"
asadmin start-domain domain1
asadmin deploy ws/target/criminal_justice_ws.war
```

**`Unable to load class ...Filter` en `server.log`.** Es un aviso del escáner de anotaciones
del contenedor web, que intenta cargar como filtros de servlet unas clases que son filtros
de JAX-RS. No afecta: la autenticación y la autorización funcionan (se comprueba con que
`/criminals` sin token responda 401 y un agente reciba 403 al crear).

**La aplicación de escritorio dice que no se puede conectar.** El WS no está desplegado o
apunta a otra URL: revisá `GET /api/v1/health` y `front/src/main/resources/config/api.properties`.

## Herramientas del repo

```bash
mvn spotless:apply     # formatea todo el Java del monorepo
npm run format         # lo mismo, vía npm
```

Husky corre Spotless y Biome antes de cada commit, y commitlint valida que el mensaje siga
Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`...).

## Pantallas del cliente

| Pantalla | Qué hace | Quién entra |
|---|---|---|
| Login | Autentica contra el WS y guarda el token de la sesión | todos |
| Dashboard | Datos de la sesión y navegación | todos |
| Criminales | Listado con búsqueda, alta, edición y borrado | ver: todos · editar: SUPERVISOR, JEFE_FBI · borrar: JEFE_FBI |
| Búsqueda avanzada | Texto libre combinado con estatus y peligrosidad | todos |
| Usuarios | Listado con alta, edición y borrado | ver: SUPERVISOR, JEFE_FBI · modificar: JEFE_FBI |

Los botones se deshabilitan según el rol, pero la autorización real la aplica el WS: la
API responde 403 aunque la petición llegue desde fuera de la aplicación.

## Estado actual

- API REST completa: auth por token, roles, validación y manejo de errores uniforme.
- Datos: **mocks en memoria**, se reinician al redesplegar el WAR.
- Front completo contra el WS: login, dashboard, CRUD de criminales, búsqueda avanzada y
  administración de usuarios.
- Pruebas: 50 con JUnit 5 sobre los mocks, ejecutables con `mvn test`.
- Pendiente: la migración a Oracle (ver [ws/docs/oracle-migration.md](ws/docs/oracle-migration.md)).
