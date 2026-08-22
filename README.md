# Sistema de Gestión para Clínica Dental

Aplicación de escritorio en **Java Swing** con base de datos **MySQL**, siguiendo
la arquitectura **MVC + DAO** descrita en el documento de contexto inicial del proyecto.

## Estructura del proyecto

```
ClinicaDental/
├── pom.xml                          # Configuración Maven (dependencia MySQL + build)
├── sql/
│   └── clinica_dental_schema.sql    # Script completo de la base de datos
└── src/main/java/com/clinicadental/
    ├── Main.java                    # Punto de entrada
    ├── util/
    │   └── ConexionBD.java          # Conexión JDBC (Singleton)
    ├── model/                       # Clases de dominio (POJOs)
    │   ├── Usuario.java
    │   ├── Doctor.java
    │   ├── Paciente.java
    │   ├── Cita.java
    │   ├── Factura.java
    │   ├── Accesorio.java
    │   └── OdontogramaItem.java
    ├── dao/                         # Capa de acceso a datos (JDBC)
    │   ├── UsuarioDAO.java          # Login / autenticación
    │   ├── PacienteDAO.java         # CRUD pacientes
    │   ├── DoctorDAO.java           # CRUD doctores
    │   ├── CitaDAO.java             # Agenda de citas + validación de conflictos
    │   ├── FacturaDAO.java          # Facturación + pagos (transacciones)
    │   ├── AccesorioDAO.java        # Inventario + movimientos
    │   └── OdontogramaDAO.java      # Estado dental por paciente
    └── view/                        # Interfaz gráfica (Swing)
        ├── LoginFrame.java          # Pantalla de login
        ├── MainFrame.java           # Dashboard con menú de navegación
        ├── PacientesPanel.java      # Módulo de Pacientes (CRUD completo)
        ├── DoctoresPanel.java       # Módulo de Doctores (CRUD completo)
        ├── CitasPanel.java          # Módulo de Citas (agenda)
        ├── OdontogramaPanel.java    # Odontograma visual interactivo
        ├── FacturacionPanel.java    # Facturación y registro de pagos
        ├── InventarioPanel.java     # Inventario y movimientos de stock
        └── ReportesPanel.java       # Estadísticas generales
```

## Arquitectura (según sección 4 del documento)

```
View (Swing) → Controller (listeners en cada Panel) → Model (DAO) → MySQL
```

Para simplificar esta versión base, la capa "Controller" vive dentro de los
listeners de cada `*Panel.java` (patrón común en apps Swing pequeñas/medianas).
Si tu equipo quiere separar Controller de View de forma más estricta, puedes
extraer esa lógica a clases `*Controller.java` sin tocar los DAO.

## Requisitos

- **JDK 11 o superior**
- **Maven** (o NetBeans, que lo trae integrado)
- **XAMPP** con MySQL activo

## Puesta en marcha

### 1. Base de datos

1. Enciende **Apache** y **MySQL** desde el panel de XAMPP.
2. Importa el script `sql/clinica_dental_schema.sql` (phpMyAdmin → Importar,
   o por terminal: `mysql -u root -p < sql/clinica_dental_schema.sql`).
3. Esto crea la base `clinica_dental` con las 18 tablas, vistas y datos de
   prueba (5 doctores, 10 pacientes, usuarios de ejemplo).

### 2. Configurar la conexión

Abre `src/main/java/com/clinicadental/util/ConexionBD.java` y ajusta si es
necesario `USUARIO` / `PASSWORD` (por defecto XAMPP usa `root` sin contraseña).

### 3. Compilar y ejecutar

**Opción A — NetBeans (recomendado, como pide el documento):**
1. Abre NetBeans → *File → Open Project* → selecciona la carpeta `ClinicaDental`.
2. NetBeans detecta el `pom.xml` y descarga automáticamente el driver MySQL.
3. Click derecho sobre `Main.java` → *Run File*.

**Opción B — línea de comandos con Maven:**
```bash
mvn clean package
java -jar target/clinica-dental.jar
```

### 4. Iniciar sesión

Usuario de prueba: `admin` / contraseña: `admin123`
(ver más usuarios en el script SQL, sección de `INSERT INTO usuarios`).

## Qué incluye esta versión base

✅ Login con validación contra la BD (hash SHA-256)
✅ Dashboard con navegación entre los 7 módulos principales
✅ CRUD completo: Pacientes, Doctores, Citas, Inventario
✅ Odontograma visual interactivo (32 piezas, historial por diente)
✅ Facturación con generación de factura + registro de pagos (transaccional)
✅ Reportes básicos con estadísticas en vivo desde la BD
✅ Validación de conflictos de horario en citas
✅ Patrón MVC + DAO tal como pide la sección 4 del documento

## Qué queda para que el equipo complete (según cronograma del documento)

- ⬜ Módulo de Usuarios/Roles con permisos granulares en la UI (la tabla
  `permisos` ya existe en la BD; falta la pantalla de administración)
- ⬜ Exportación de reportes a PDF/Excel (JasperReports o iText — hay un
  comentario en `ReportesPanel.java` marcando dónde integrarlo)
- ⬜ Calendario visual (mes/semana) para el módulo de Citas — actualmente
  es una tabla filtrable por fecha
- ⬜ Fotos de pacientes/dientes (columnas `foto_url` ya están en la BD)
- ⬜ Pruebas unitarias (JUnit) sobre los DAO
- ⬜ JavaDoc completo y manual de usuario

Cada uno de estos puntos mapea directamente a los roles asignados en la
sección 3 del documento de contexto inicial, para que cada integrante sepa
exactamente qué archivo extender.

## Nota importante

Este código fue escrito y revisado manualmente pero **no pudo compilarse ni
ejecutarse en este entorno** (no hay JDK completo ni MySQL disponibles aquí).
Al abrirlo en NetBeans con Maven, es normal necesitar 1-2 ajustes menores
(por ejemplo, la versión exacta del connector si usan una distinta). Antes de
la presentación, compílalo y pruébalo con datos reales.
