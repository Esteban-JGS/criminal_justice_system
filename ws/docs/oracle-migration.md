# De mocks a Oracle

Los mocks están aislados a propósito: viven en `com.fbi.cjs.ws.mocks` y solo implementan
las interfaces de `com.fbi.cjs.ws.repository`. Nada más del web service los conoce.

Migrar a Oracle no toca ni servicios, ni recursos REST, ni el front.

```
        HOY                                    DESPUÉS
CriminalResource                        CriminalResource        (igual)
      |                                       |
CriminalService                         CriminalService         (igual)
      |  inyecta la interfaz                  |  inyecta la interfaz
CriminalRepository  <── interfaz ──>    CriminalRepository      (igual)
      |                                       |
MockCriminalRepository                  JpaCriminalRepository   (nuevo)
 (ConcurrentHashMap)                     (EntityManager + Oracle)
```

## 1. Datasource en Payara

Con Payara corriendo, desde `payara7/bin`:

```bash
# El driver ojdbc11.jar va en payara7/glassfish/domains/domain1/lib/ y luego se reinicia el dominio.

asadmin create-jdbc-connection-pool \
  --datasourceclassname oracle.jdbc.pool.OracleDataSource \
  --restype javax.sql.DataSource \
  --property user=TU_USUARIO:password=TU_CLAVE:url="jdbc\:oracle\:thin\:@localhost\:1521/XEPDB1" \
  CriminalJusticePool

asadmin create-jdbc-resource --connectionpoolid CriminalJusticePool jdbc/CriminalJusticeDS

asadmin ping-connection-pool CriminalJusticePool
```

> Los `\:` no son un error: `asadmin` usa `:` como separador de propiedades, hay que escaparlos.

También se puede hacer desde la consola web (`http://localhost:4848`) en
**Resources → JDBC**, que es más cómodo la primera vez.

## 2. Activar la unidad de persistencia

Renombrar `src/main/resources/META-INF/persistence.xml.oracle-template` a `persistence.xml`.
Ya apunta a `jdbc/CriminalJusticeDS`.

## 3. Entidades JPA

Nuevo paquete `com.fbi.cjs.ws.entity`. La entidad **no** reemplaza al DTO: la entidad
refleja la tabla, el DTO refleja el JSON. Mantenerlos separados evita exponer columnas
internas y romper el contrato del front cada vez que cambia el esquema.

```java
@Entity
@Table(name = "CRIMINALS")
public class Criminal {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "criminalSeq")
    @SequenceGenerator(name = "criminalSeq", sequenceName = "CRIMINALS_SEQ", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 120)
    private String name;

    @Column(name = "ALIAS", length = 60)
    private String alias;

    @Column(name = "CRIME", nullable = false, length = 200)
    private String crime;

    @Enumerated(EnumType.STRING)
    @Column(name = "DANGER_LEVEL", nullable = false, length = 10)
    private DangerLevel dangerLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 12)
    private CriminalStatus status;

    // getters y setters
}
```

`@Enumerated(EnumType.STRING)` y no `ORDINAL`: si se guarda el ordinal, insertar un
valor nuevo en medio del enum corrompe silenciosamente todos los registros viejos.

## 4. Repositorio JPA

```java
@ApplicationScoped
@Alternative                     // <- lo que permite reemplazar al mock desde beans.xml
@Transactional                   // <- insert/update/delete dentro de una transacción JTA
public class JpaCriminalRepository implements CriminalRepository {

    @PersistenceContext(unitName = "CriminalJusticePU")
    private EntityManager em;

    @Override
    public List<CriminalDTO> findAll() {
        return em.createQuery("SELECT c FROM Criminal c ORDER BY c.id", Criminal.class)
                 .getResultList()
                 .stream()
                 .map(CriminalMapper::toDto)
                 .toList();
    }

    @Override
    public Optional<CriminalDTO> findById(Long id) {
        return Optional.ofNullable(em.find(Criminal.class, id)).map(CriminalMapper::toDto);
    }

    @Override
    public CriminalDTO create(CriminalDTO dto) {
        Criminal entity = CriminalMapper.toEntity(dto);
        em.persist(entity);
        em.flush();                   // fuerza el INSERT para que la secuencia asigne el id
        return CriminalMapper.toDto(entity);
    }

    // update, deleteById y search igual, con JPQL
}
```

Para `search`, usar parámetros con nombre (`:text`) y nunca concatenar strings dentro
del JPQL: la concatenación es una inyección SQL esperando a ocurrir.

## 5. Activar el cambio

En `src/main/webapp/WEB-INF/beans.xml`, descomentar:

```xml
<alternatives>
    <class>com.fbi.cjs.ws.persistence.JpaCriminalRepository</class>
    <class>com.fbi.cjs.ws.persistence.JpaUserRepository</class>
</alternatives>
```

Un `@Alternative` activado gana sobre el bean normal, así que CDI inyecta la versión JPA
sin que haya ambigüedad con el mock. Para volver a mocks (por ejemplo, si la base está
caída), se comenta el bloque y se redespliega.

## 6. Borrar los mocks (cuando ya no hagan falta)

`rm -r src/main/java/com/fbi/cjs/ws/mocks` y quitar el bloque `<alternatives>`, dejando
las clases JPA como implementación única. Si el proyecto compila después de eso, la
separación de capas estaba bien hecha.

## Script SQL de arranque

```sql
CREATE TABLE CRIMINALS (
    ID           NUMBER(19)    PRIMARY KEY,
    NAME         VARCHAR2(120) NOT NULL,
    ALIAS        VARCHAR2(60),
    CRIME        VARCHAR2(200) NOT NULL,
    DANGER_LEVEL VARCHAR2(10)  NOT NULL,
    STATUS       VARCHAR2(12)  NOT NULL
);
CREATE SEQUENCE CRIMINALS_SEQ START WITH 100 INCREMENT BY 1;

CREATE TABLE USERS (
    ID       NUMBER(19)    PRIMARY KEY,
    NAME     VARCHAR2(120) NOT NULL,
    USERNAME VARCHAR2(40)  NOT NULL UNIQUE,
    PASSWORD VARCHAR2(120) NOT NULL,
    ROLE     VARCHAR2(20)  NOT NULL,
    ACTIVE   NUMBER(1)     DEFAULT 1 NOT NULL
);
CREATE SEQUENCE USERS_SEQ START WITH 100 INCREMENT BY 1;
```

`PASSWORD VARCHAR2(120)` porque ahí va un **hash**, no la contraseña. Los mocks la
guardan en texto plano solo porque son datos de juguete.
