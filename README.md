# DBCase 5.0

Herramienta web para el diseño visual de esquemas Entidad-Relacion y su transformacion automatica a esquema logico relacional y SQL.

---

## Tabla de contenidos

- [Descripcion general](#descripcion-general)
- [Tecnologias](#tecnologias)
- [Requisitos previos](#requisitos-previos)
- [Arranque sin Docker](#arranque-sin-docker)
- [Arranque con Docker](#arranque-con-docker)
- [Flujo de la aplicacion](#flujo-de-la-aplicacion)
- [Arquitectura del backend](#arquitectura-del-backend)
- [Calidad de codigo y build](#calidad-de-codigo-y-build)
- [Como extender la aplicacion](#como-extender-la-aplicacion)
- [CI/CD](#cicd)

---

## Descripcion general

DBCase permite disenar esquemas Entidad-Relacion de forma visual e interactiva sobre un canvas, transformarlos automaticamente al modelo logico relacional y generar el DDL SQL correspondiente. Tambien permite la transformacion inversa: a partir de SQL existente, reconstruir el esquema logico.

El usuario puede autenticarse con usuario y contrasena propios o mediante OAuth2 con GitHub o Google. Los esquemas se persisten en la nube asociados a la cuenta del usuario. La interfaz soporta multiples idiomas.

---

## Tecnologias

| Capa | Tecnologia |
|---|---|
| Frontend | Vue 3 + TypeScript + Vite + TailwindCSS + PrimeVue |
| Backend | Spring Boot 3.5 + Java 25 + Spring Security + JWT |
| Base de datos | PostgreSQL 17 |
| Contenedores | Docker + Docker Compose |
| Canvas | Konva.js |
| Editor SQL | Monaco Editor |

---

## Requisitos previos

**Sin Docker:**
- Java 25
- Maven 3.9+
- Node.js 20+ o 22+
- PostgreSQL 17 corriendo localmente

**Con Docker:**
- Docker
- Docker Compose

---

## Arranque sin Docker

### 1. Base de datos

Crea la base de datos y ejecuta el script de inicializacion:

```sql
CREATE DATABASE dbcase;
```

```bash
psql -U postgres -d dbcase -f DBCase/backend/db/script.sql
```

### 2. Backend

Crea el fichero `DBCase/backend/.env` con las variables de entorno necesarias:

<details>
<summary>Ver variables de entorno del backend</summary>

```env
DB_USER=admin
DB_PASSWORD=admin
JWT_SECRET=<clave_base64_256bits>
GITHUB_CLIENT_ID=<tu_client_id>
GITHUB_CLIENT_SECRET=<tu_client_secret>
GOOGLE_CLIENT_ID=<tu_client_id>
GOOGLE_CLIENT_SECRET=<tu_client_secret>
SSL_KEY_STORE=classpath:keystore.p12
SSL_KEY_STORE_PASSWORD=dbcase_ssl
SSL_KEY_ALIAS=dbcase
FRONTEND_URL=https://localhost:5173
```

</details>

```bash
cd DBCase/backend
./mvnw spring-boot:run
```

El backend arranca en `https://localhost:8443`.

> [!NOTE]
> En local, Spotless formatea el codigo automaticamente al compilar. En CI solo verifica y falla si el codigo no esta formateado. Ver la seccion [Calidad de codigo](#calidad-de-codigo-y-build).

### 3. Frontend

```bash
cd DBCase/frontend
npm install
npm run dev
```

El frontend arranca en `https://localhost:5173`.

> [!NOTE]
> `npm run dev` ejecuta lint y chequeo de formato antes de levantar el servidor. Si hay errores, el servidor no arranca. Para saltarse las comprobaciones durante el desarrollo usa `npm run dev:server`.

---

## Arranque con Docker

Crea el fichero `DBCase/backend/.env`:

<details>
<summary>Ver variables de entorno del backend</summary>

```env
DB_USER=admin
DB_PASSWORD=admin
JWT_SECRET=<clave_base64_256bits>
GITHUB_CLIENT_ID=<tu_client_id>
GITHUB_CLIENT_SECRET=<tu_client_secret>
GOOGLE_CLIENT_ID=<tu_client_id>
GOOGLE_CLIENT_SECRET=<tu_client_secret>
SSL_KEY_STORE=classpath:keystore.p12
SSL_KEY_STORE_PASSWORD=dbcase_ssl
SSL_KEY_ALIAS=dbcase
FRONTEND_URL=https://localhost
```

</details>

Desde la raiz del proyecto:

```bash
cd DBCase
docker compose up --build
```

Esto levanta tres contenedores:

| Contenedor | Puerto | Descripcion |
|---|---|---|
| `DBCaseFrontend` | 443, 80 | Nginx sirviendo el frontend compilado |
| `DBCaseApp` | 8443 | Spring Boot |
| `DBCaseDB` | 5431 | PostgreSQL |

La aplicacion estara disponible en `https://localhost`.

```bash
# Parar
docker compose down

# Parar y eliminar datos de la base de datos
docker compose down -v
```

---

## Flujo de la aplicacion

```
Usuario
  |
  v
Frontend (Vue 3 + Konva.js)
  |  Peticiones HTTPS a /api/*
  |  En Docker: proxied por Nginx hacia backend:8443
  v
Backend (Spring Boot)
  |  Autenticacion JWT / OAuth2
  |  Rate limiting por IP (Bucket4j: 100 req/s por IP y endpoint)
  |  Transformacion de diagramas
  v
PostgreSQL
     Persistencia de usuarios y esquemas (JSON)
```

### Autenticacion

El usuario puede registrarse con usuario y contrasena o iniciar sesion con GitHub o Google. Tras autenticarse, el backend emite un JWT que se almacena en una cookie HttpOnly y se envia automaticamente en cada peticion. Las contrasenas se almacenan con BCrypt (factor 10).

### Diseno del esquema ER

El usuario trabaja sobre un canvas interactivo (Konva.js) donde puede anadir entidades, entidades debiles, atributos, relaciones binarias, relaciones n-arias, relaciones muchos-a-muchos, relaciones totales e interrelaciones IS-A. El estado del diagrama se gestiona con Pinia y se persiste en el backend como JSON asociado al usuario.

### Transformaciones

Con un clic, el frontend envia el diagrama al endpoint `POST /api/diagram/generate` con un `TransformRequest` que indica el formato de entrada (`type`), los datos (`diagram`) y el formato de salida deseado (`transformTo`). El backend convierte la entrada al grafo interno comun usando la estrategia del tipo de entrada, y luego convierte ese grafo al formato de salida usando la estrategia del tipo de salida. Cualquier combinacion de formatos es posible: ER a logico, logico a SQL, SQL a ER, etc.

---

## Arquitectura del backend

### Patron Strategy para las transformaciones

El nucleo de la logica de transformacion esta basado en el patron Strategy. Existe una interfaz `DiagramStrategy<I>` con dos metodos principales:

- `generate(I input)`: convierte la representacion especifica de la estrategia en un grafo interno unificado (`Diagram`).
- `transform(Diagram diagram)`: convierte ese grafo interno de vuelta a la representacion especifica de la estrategia.

El endpoint `POST /api/diagram/generate` recibe un `TransformRequest` con tres campos: `type` (formato de entrada), `diagram` (los datos de entrada) y `transformTo` (formato de salida deseado). El `DiagramTransformationService` usa el `DiagramStrategyRegistry` para llamar a `generate` con la estrategia del tipo de entrada, y luego a `transform` con la estrategia del tipo de salida.

Hay tres implementaciones:

| Estrategia | Tipo de entrada/salida | `generate` parsea | `transform` produce |
|---|---|---|---|
| `ERDiagramStrategy` | `ErInput` | Entidades, relaciones y atributos del canvas | `ErInput` (diagrama ER reconstruido) |
| `LogicalDiagramStrategy` | `LogicalInput` | Esquema logico en formato texto con restricciones | `Map` con `relationship`, `restriction` y `lossRestriction` |
| `DBDiagramStrategy` | `PhysicalInput` | Sentencias `CREATE TABLE` parseadas con JSQLParser | SQL DDL como `String` |

> [!TIP]
> Para anadir soporte a un nuevo tipo de diagrama basta con implementar `DiagramStrategy` y anotarla con `@Service`. El `DiagramStrategyRegistry` la registrara automaticamente en tiempo de ejecucion.

### Seguridad

- Autenticacion stateless con JWT via cookie HttpOnly.
- OAuth2 con GitHub y Google: tras el login, el `OAuth2LoginSuccessHandler` crea o recupera el usuario y emite el JWT.
- CORS configurado para aceptar unicamente el origen del frontend.
- Rate limiting por IP y endpoint con Bucket4j (100 peticiones por segundo).
- Todas las rutas requieren autenticacion excepto `/api/user/login` y `/api/user/register`.

### Dominios personalizados

El usuario puede definir dominios de datos propios (por ejemplo, `DNI` con base `VARCHAR`). Estos dominios se almacenan en la tabla `user_custom_domains` y se tienen en cuenta durante la transformacion ER para asignar el tipo correcto a los atributos.

---

## Calidad de codigo y build

### Backend

El backend usa Maven con tres herramientas de calidad integradas en el ciclo de build.

<details>
<summary>Spotless (formateo automatico)</summary>

Spotless aplica el estilo Google Java Format (AOSP) y elimina imports no usados.

- En local (`mvn compile` o `mvn spring-boot:run`): aplica el formato automaticamente. Si el codigo no esta formateado, lo corrige.
- En CI (cuando la variable de entorno `CI` esta presente): solo verifica. Si el codigo no esta formateado, el build falla.

```bash
./mvnw spotless:apply   # formatear
./mvnw spotless:check   # solo verificar
```

</details>

<details>
<summary>Checkstyle (reglas de estilo)</summary>

Valida reglas de estilo definidas en `config/checkstyle/checkstyle.xml`. Se ejecuta en la fase `validate`, antes de compilar. Si hay violaciones, el build falla. Las supresiones estan en `config/checkstyle/checkstyle-suppressions.xml`.

```bash
./mvnw checkstyle:check
```

</details>

<details>
<summary>Tests</summary>

Los tests usan H2 como base de datos en memoria, por lo que no necesitan PostgreSQL para ejecutarse.

```bash
./mvnw test
```

</details>

---

### Frontend

El frontend usa npm scripts encadenados con `npm-run-all2`.

<details>
<summary>Linting (Oxlint + ESLint)</summary>

Se usan dos linters en serie. Oxlint es una primera pasada rapida escrita en Rust. ESLint hace una segunda pasada con reglas especificas de Vue, TypeScript, imports y Playwright.

```bash
npm run lint        # ejecuta ambos en serie
npm run lint:fix    # corrige automaticamente lo que pueda
```

</details>

<details>
<summary>Formato (Prettier)</summary>

```bash
npm run format        # aplica formato a src/
npm run format:check  # solo verifica (usado en CI)
```

</details>

<details>
<summary>Type check, tests y build</summary>

```bash
npm run type-check    # vue-tsc --build
npm run test:unit     # Vitest + jsdom
npm run test:e2e      # Playwright
npm run build         # type-check + vite build en paralelo, resultado en dist/
```

</details>

---

## Como extender la aplicacion

### Anadir un nuevo tipo de nodo al diagrama ER

1. Define el nuevo tipo en `src/types/er-diagram-elements.ts`.
2. Crea el componente visual en `src/components/canvas/nodes/`.
3. Registralo en `DiagramCanvas.vue`.
4. Anade la logica de transformacion en `ERDiagramStrategy.java`.

### Anadir soporte para un nuevo tipo de diagrama o transformacion

1. Implementa la interfaz `DiagramStrategy<I>` en el paquete `strategies`.
2. Anotala con `@Service`. El `DiagramStrategyRegistry` la registrara automaticamente.
3. Anade el nuevo valor al enum `DiagramType`.

### Anadir un nuevo endpoint en el backend

1. Crea el `@RestController` en el paquete `controller`.
2. Si la ruta debe ser publica, anadela a la lista de `permitAll` en `SecurityConfig`.
3. Escribe los tests en `src/test/`.

### Anadir un nuevo idioma

Los ficheros de traduccion estan en `src/i18n/`. Anade un nuevo fichero de mensajes y registralo en `src/i18n/index.ts`.

---

## CI/CD

El repositorio tiene tres workflows de GitHub Actions:

| Workflow | Cuando se ejecuta | Que hace |
|---|---|---|
| `Backend CI` | Push/PR a `main` con cambios en `DBCase/backend/**` | Compila, verifica formato (Spotless check), Checkstyle y tests |
| `Frontend CI` | Push/PR a `main` con cambios en `DBCase/frontend/**` | Lint, format check, type check, tests unitarios, build y tests E2E |
| `Deploy` | Cuando Backend CI o Frontend CI terminan con exito en `main` | SSH al servidor, `git pull` y `docker compose up --build` |

Para el deploy es necesario configurar los siguientes secrets en el repositorio (Settings > Secrets > Actions):

| Secret | Descripcion |
|---|---|
| `SSH_HOST` | IP o dominio del servidor |
| `SSH_USER` | Usuario SSH |
| `SSH_PRIVATE_KEY` | Clave privada SSH (contenido completo) |
| `SSH_PORT` | Puerto SSH (normalmente `22`) |

> [!WARNING]
> Nunca subas el fichero `.env` al repositorio. Contiene credenciales sensibles. Asegurate de que esta en el `.gitignore`.
