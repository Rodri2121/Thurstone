# Thurstone

Plataforma web del test de orientación vocacional de Thurstone: el evaluado
compara pares de actividades y se genera su perfil de interés por carrera. Hecha
con **Spring Boot + Thymeleaf**, con **PostgreSQL** para datos y sesiones.

Tiene dos roles:

- **Psicólogo** — inicia sesión, registra evaluados y les asigna un test, lo que
  genera una **clave de acceso de un solo uso**. Consulta los resultados desde su panel.
- **Evaluado** — no es usuario del sistema: entra con la clave que le da el
  psicólogo, responde el test y ve sus resultados.

## Requisitos

- **JDK 17** o superior
- **PostgreSQL** en ejecución (probado con PostgreSQL 18)
- No necesitas instalar Maven: usa el wrapper `mvnw` incluido

## Base de datos

Crea la base una sola vez:

```sql
CREATE DATABASE thurstone;
```

Las tablas de dominio las crea Hibernate al arrancar (`ddl-auto=update`) y el
esquema de sesiones se aplica desde `schema.sql`. Ajusta tu conexión en
`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/thurstone
spring.datasource.username=postgres
spring.datasource.password=tu_contraseña
```

## Arranque

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Luego abre **http://localhost:8081**

> Si al arrancar ves "JAVA_HOME is not defined", apunta `JAVA_HOME` a tu JDK 17+
> antes de ejecutar el comando.

## Primer acceso (psicólogo)

La primera vez que arranca, si no hay usuarios, se crea un psicólogo de
demostración para entrar sin pasos manuales:

- **Usuario:** `psicologo@thurstone.test`
- **Contraseña:** `thurstone123`

Inicia sesión en `/login`. Cámbialo en producción o desactiva la semilla con
`thurstone.seed-demo-user=false`.

## Tests

```bash
mvnw.cmd test
```

Los tests corren sobre **H2 en memoria**, así que no necesitan un PostgreSQL en
ejecución.
