-- =============================================================================
-- CASE_REGISTRY
-- =============================================================================
-- Esta es una unica tabla, construida a partir de la planilla que la oficina de
-- expedientes del centro llevaba manualmente antes de tener un sistema propio.
-- Cada fila combina, en un mismo registro, datos del expediente, del criminal
-- investigado, del agente asignado y de todo lo que se fue acumulando durante
-- el caso: teléfonos de contacto, delitos imputados, víctimas, evidencia,
-- tribunal y sentencia.
--
-- Sintaxis Oracle, coherente con ws/docs/oracle-migration.md.
-- =============================================================================

CREATE TABLE CASE_REGISTRY (
    CASE_NUMBER            VARCHAR2(20)   NOT NULL,
    AGENT_BADGE            VARCHAR2(20)   NOT NULL,

    OPENED_DATE            DATE           NOT NULL,
    CASE_STATUS            VARCHAR2(20)   NOT NULL
        CHECK (CASE_STATUS IN ('EN_INVESTIGACION', 'EN_JUICIO', 'CERRADO', 'ARCHIVADO')),

    CRIMINAL_NAME           VARCHAR2(120)  NOT NULL,
    CRIMINAL_ALIAS          VARCHAR2(60),
    CRIMINAL_DANGER_LEVEL   VARCHAR2(10)   NOT NULL
        CHECK (CRIMINAL_DANGER_LEVEL IN ('ALTO', 'MEDIO', 'BAJO')),
    CRIMINAL_STATUS         VARCHAR2(12)   NOT NULL
        CHECK (CRIMINAL_STATUS IN ('ACTIVO', 'CAPTURADO', 'FALLECIDO')),
    CRIMINAL_PHONES         VARCHAR2(100),

    CHARGES                 VARCHAR2(200)  NOT NULL,
    VICTIMS                 VARCHAR2(200),

    ARREST_DATE             DATE,
    ARREST_LOCATION         VARCHAR2(200),

    COURT_NAME              VARCHAR2(150),
    COURT_ADDRESS           VARCHAR2(200),
    SENTENCE_DATE           DATE,
    SENTENCE_DESCRIPTION     VARCHAR2(300),

    EVIDENCE_ITEMS           VARCHAR2(300),

    AGENT_NAME               VARCHAR2(120)  NOT NULL,
    AGENT_DIVISION           VARCHAR2(80)   NOT NULL,
    DIVISION_LOCATION        VARCHAR2(150)  NOT NULL,
    AGENT_STATUS             VARCHAR2(12)   NOT NULL
        CHECK (AGENT_STATUS IN ('ACTIVO', 'SUSPENDIDO', 'RETIRADO')),

    CASE_ROLE               VARCHAR2(30)   NOT NULL,
    ASSIGNED_DATE           DATE           NOT NULL,

    CONSTRAINT PK_CASE_REGISTRY PRIMARY KEY (CASE_NUMBER, AGENT_BADGE)
);

-- =============================================================================
-- Datos de muestra
-- =============================================================================
-- CRIMINAL_PHONES, CHARGES, VICTIMS y EVIDENCE_ITEMS guardan varios valores
-- separados por coma dentro de un mismo campo.
--
-- El caso EXP-2024-0001 tiene dos agentes asignados (Dana Scully y Fox Mulder),
-- así que aparece en dos filas. La agente Dana Scully también esta asignada al
-- caso EXP-2024-0003, así que sus propios datos (nombre, división, ubicacion,
-- estado) también aparecen en dos filas distintas.
--
-- Los agentes Eliot Ness y Roberto Alvarado pertenecen a la misma división
-- ("Crimen Organizado"), y los casos EXP-2024-0004 y EXP-2024-0005 comparten
-- el mismo tribunal.
--
-- Victor Salazar tiene dos expedientes distintos (EXP-2024-0001 y EXP-2024-0006),
-- así que sus datos personales y sus teléfonos quedan repetidos en filas que
-- pertenecen a casos diferentes.

INSERT INTO CASE_REGISTRY (
    CASE_NUMBER, AGENT_BADGE, OPENED_DATE, CASE_STATUS,
    CRIMINAL_NAME, CRIMINAL_ALIAS, CRIMINAL_DANGER_LEVEL, CRIMINAL_STATUS, CRIMINAL_PHONES,
    CHARGES, VICTIMS, ARREST_DATE, ARREST_LOCATION,
    COURT_NAME, COURT_ADDRESS, SENTENCE_DATE, SENTENCE_DESCRIPTION, EVIDENCE_ITEMS,
    AGENT_NAME, AGENT_DIVISION, DIVISION_LOCATION, AGENT_STATUS,
    CASE_ROLE, ASSIGNED_DATE
) VALUES (
    'EXP-2024-0001', 'FBI-1001', TO_DATE('2024-01-15', 'YYYY-MM-DD'), 'EN_INVESTIGACION',
    'Victor Salazar', 'El Fantasma', 'ALTO', 'ACTIVO', '8888-1234,8888-5678',
    'Lavado de dinero,Fraude bancario', 'Banco Popular de Costa Rica', NULL, NULL,
    NULL, NULL, NULL, NULL, 'Estados de cuenta bancarios,Correos electrónicos',
    'Dana Scully', 'Ciencias Forenses', 'Edificio Central, San José', 'ACTIVO',
    'Investigador principal', TO_DATE('2024-01-15', 'YYYY-MM-DD')
);

INSERT INTO CASE_REGISTRY (
    CASE_NUMBER, AGENT_BADGE, OPENED_DATE, CASE_STATUS,
    CRIMINAL_NAME, CRIMINAL_ALIAS, CRIMINAL_DANGER_LEVEL, CRIMINAL_STATUS, CRIMINAL_PHONES,
    CHARGES, VICTIMS, ARREST_DATE, ARREST_LOCATION,
    COURT_NAME, COURT_ADDRESS, SENTENCE_DATE, SENTENCE_DESCRIPTION, EVIDENCE_ITEMS,
    AGENT_NAME, AGENT_DIVISION, DIVISION_LOCATION, AGENT_STATUS,
    CASE_ROLE, ASSIGNED_DATE
) VALUES (
    'EXP-2024-0001', 'FBI-1002', TO_DATE('2024-01-15', 'YYYY-MM-DD'), 'EN_INVESTIGACION',
    'Victor Salazar', 'El Fantasma', 'ALTO', 'ACTIVO', '8888-1234,8888-5678',
    'Lavado de dinero,Fraude bancario', 'Banco Popular de Costa Rica', NULL, NULL,
    NULL, NULL, NULL, NULL, 'Estados de cuenta bancarios,Correos electrónicos',
    'Fox Mulder', 'Casos Sin Resolver', 'Edificio Anexo, San José', 'ACTIVO',
    'Apoyo', TO_DATE('2024-01-18', 'YYYY-MM-DD')
);

INSERT INTO CASE_REGISTRY (
    CASE_NUMBER, AGENT_BADGE, OPENED_DATE, CASE_STATUS,
    CRIMINAL_NAME, CRIMINAL_ALIAS, CRIMINAL_DANGER_LEVEL, CRIMINAL_STATUS, CRIMINAL_PHONES,
    CHARGES, VICTIMS, ARREST_DATE, ARREST_LOCATION,
    COURT_NAME, COURT_ADDRESS, SENTENCE_DATE, SENTENCE_DESCRIPTION, EVIDENCE_ITEMS,
    AGENT_NAME, AGENT_DIVISION, DIVISION_LOCATION, AGENT_STATUS,
    CASE_ROLE, ASSIGNED_DATE
) VALUES (
    'EXP-2024-0002', 'FBI-1004', TO_DATE('2023-11-02', 'YYYY-MM-DD'), 'EN_JUICIO',
    'Ramón Oviedo', 'El Toro', 'ALTO', 'ACTIVO', '8777-2222',
    'Tráfico de armas', NULL, TO_DATE('2023-11-10', 'YYYY-MM-DD'), 'Puntarenas, zona portuaria',
    'Tribunal Penal de Puntarenas', 'Calle Central, Puntarenas', NULL, NULL, 'Armas decomisadas,Bitácora de envíos',
    'Eliot Ness', 'Crimen Organizado', 'Torre de Investigaciones, Heredia', 'SUSPENDIDO',
    'Investigador principal', TO_DATE('2023-11-02', 'YYYY-MM-DD')
);

INSERT INTO CASE_REGISTRY (
    CASE_NUMBER, AGENT_BADGE, OPENED_DATE, CASE_STATUS,
    CRIMINAL_NAME, CRIMINAL_ALIAS, CRIMINAL_DANGER_LEVEL, CRIMINAL_STATUS, CRIMINAL_PHONES,
    CHARGES, VICTIMS, ARREST_DATE, ARREST_LOCATION,
    COURT_NAME, COURT_ADDRESS, SENTENCE_DATE, SENTENCE_DESCRIPTION, EVIDENCE_ITEMS,
    AGENT_NAME, AGENT_DIVISION, DIVISION_LOCATION, AGENT_STATUS,
    CASE_ROLE, ASSIGNED_DATE
) VALUES (
    'EXP-2024-0003', 'FBI-1001', TO_DATE('2024-02-01', 'YYYY-MM-DD'), 'EN_INVESTIGACION',
    'Sandra Quirós', 'La Araña', 'MEDIO', 'ACTIVO', '8666-3333,8666-4444',
    'Fraude bancario', 'Cooperativa de Ahorro RL', NULL, NULL,
    NULL, NULL, NULL, NULL, 'Transferencias sospechosas',
    'Dana Scully', 'Ciencias Forenses', 'Edificio Central, San José', 'ACTIVO',
    'Investigador principal', TO_DATE('2024-02-01', 'YYYY-MM-DD')
);

INSERT INTO CASE_REGISTRY (
    CASE_NUMBER, AGENT_BADGE, OPENED_DATE, CASE_STATUS,
    CRIMINAL_NAME, CRIMINAL_ALIAS, CRIMINAL_DANGER_LEVEL, CRIMINAL_STATUS, CRIMINAL_PHONES,
    CHARGES, VICTIMS, ARREST_DATE, ARREST_LOCATION,
    COURT_NAME, COURT_ADDRESS, SENTENCE_DATE, SENTENCE_DESCRIPTION, EVIDENCE_ITEMS,
    AGENT_NAME, AGENT_DIVISION, DIVISION_LOCATION, AGENT_STATUS,
    CASE_ROLE, ASSIGNED_DATE
) VALUES (
    'EXP-2024-0004', 'FBI-1006', TO_DATE('2023-08-10', 'YYYY-MM-DD'), 'CERRADO',
    'Diego Montero', 'El Sombra', 'MEDIO', 'CAPTURADO', '8555-9999',
    'Extorsión', 'Comerciante del Mercado Central,Asociación de Comerciantes',
    TO_DATE('2023-09-05', 'YYYY-MM-DD'), 'San José, Mercado Central',
    'Tribunal Penal del Primer Circuito Judicial de San José', 'Avenida 6, San José',
    TO_DATE('2024-01-20', 'YYYY-MM-DD'), 'Ocho años de prisión por extorsión agravada',
    'Grabaciones telefónicas,Testimonios de comerciantes',
    'Roberto Alvarado', 'Crimen Organizado', 'Torre de Investigaciones, Heredia', 'ACTIVO',
    'Investigador principal', TO_DATE('2023-08-10', 'YYYY-MM-DD')
);

INSERT INTO CASE_REGISTRY (
    CASE_NUMBER, AGENT_BADGE, OPENED_DATE, CASE_STATUS,
    CRIMINAL_NAME, CRIMINAL_ALIAS, CRIMINAL_DANGER_LEVEL, CRIMINAL_STATUS, CRIMINAL_PHONES,
    CHARGES, VICTIMS, ARREST_DATE, ARREST_LOCATION,
    COURT_NAME, COURT_ADDRESS, SENTENCE_DATE, SENTENCE_DESCRIPTION, EVIDENCE_ITEMS,
    AGENT_NAME, AGENT_DIVISION, DIVISION_LOCATION, AGENT_STATUS,
    CASE_ROLE, ASSIGNED_DATE
) VALUES (
    'EXP-2024-0005', 'FBI-1003', TO_DATE('2023-12-01', 'YYYY-MM-DD'), 'EN_JUICIO',
    'Luisa Fernández', 'La Cobra', 'ALTO', 'ACTIVO', '8444-1111',
    'Narcotráfico,Tráfico de personas', NULL,
    TO_DATE('2023-12-15', 'YYYY-MM-DD'), 'Limón, zona fronteriza',
    'Tribunal Penal del Primer Circuito Judicial de San José', 'Avenida 6, San José',
    NULL, NULL, 'Paquetes decomisados,Registros migratorios',
    'Clarice Starling', 'Ciencias del Comportamiento', 'Cuartel Regional, Cartago', 'ACTIVO',
    'Investigador principal', TO_DATE('2023-12-01', 'YYYY-MM-DD')
);

INSERT INTO CASE_REGISTRY (
    CASE_NUMBER, AGENT_BADGE, OPENED_DATE, CASE_STATUS,
    CRIMINAL_NAME, CRIMINAL_ALIAS, CRIMINAL_DANGER_LEVEL, CRIMINAL_STATUS, CRIMINAL_PHONES,
    CHARGES, VICTIMS, ARREST_DATE, ARREST_LOCATION,
    COURT_NAME, COURT_ADDRESS, SENTENCE_DATE, SENTENCE_DESCRIPTION, EVIDENCE_ITEMS,
    AGENT_NAME, AGENT_DIVISION, DIVISION_LOCATION, AGENT_STATUS,
    CASE_ROLE, ASSIGNED_DATE
) VALUES (
    'EXP-2024-0006', 'FBI-1003', TO_DATE('2024-03-05', 'YYYY-MM-DD'), 'EN_INVESTIGACION',
    'Victor Salazar', 'El Fantasma', 'ALTO', 'ACTIVO', '8888-1234,8888-5678',
    'Falsificación', 'Registro Nacional', NULL, NULL,
    NULL, NULL, NULL, NULL, 'Documentos falsificados',
    'Clarice Starling', 'Ciencias del Comportamiento', 'Cuartel Regional, Cartago', 'ACTIVO',
    'Investigador principal', TO_DATE('2024-03-05', 'YYYY-MM-DD')
);

COMMIT;
