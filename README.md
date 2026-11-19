# Seguros BPO — Microservicios

Cuatro microservicios Spring Boot 4.1 (JDK 21) sobre **MongoDB Atlas**, que dan servicio a la app Android Seguros BPO.

| Servicio | Puerto | Base de datos | Responsabilidad |
|---|---|---|---|
| `roles-service` | 8081 | `segurosbpo_roles` | Catálogo de roles: Asegurado, Liquidador, Analista |
| `usuarios-service` | 8082 | `segurosbpo_usuarios` | Usuarios, login, contraseña y avatar. Consulta a `roles-service` |
| `catalogos-service` | 8083 | `segurosbpo_catalogos` | Compañías de seguros y tipos de siniestro |
| `siniestros-service` | 8084 | `segurosbpo_siniestros` | Casos y gestiones. Consulta a `catalogos-service` y a `usuarios-service` |

Cada servicio tiene su propia base de datos. Cuando uno necesita un dato de otro se lo pide por HTTP
con **WebClient**: `siniestros` guarda solo el `companiaCodigo` y el `liquidadorId`, y pregunta por
el nombre cuando arma la respuesta.

```
   roles-service (8081)
          ^
          | WebClient
   usuarios-service (8082) <----+
                                | WebClient
   catalogos-service (8083) <---+--- siniestros-service (8084)
                                      WebClient
```

## Conexión a MongoDB

La cadena de conexión no está en el código. Cada servicio la arma desde la variable de entorno
`MONGO_URI` y le agrega su propia base de datos:

```properties
spring.mongodb.uri=${MONGO_URI}/segurosbpo_roles
```

Antes de levantar nada hay que definir esa variable. En `.env.example` está el formato;
copia ese archivo como `.env` para tenerlo a mano (`.env` está ignorado por Git y **no se sube**).

Ojo: Spring Boot **no lee el archivo `.env` por su cuenta**. Hay que entregarle la variable al proceso.
La forma más cómoda en Windows es dejarla fija una vez, desde PowerShell:

```powershell
setx MONGO_URI "mongodb+srv://USUARIO:CLAVE@TU_CLUSTER.mongodb.net"
```

Después hay que **cerrar y volver a abrir IntelliJ** para que tome la variable. Si falta, el servicio
no parte y el error dice `Could not resolve placeholder 'MONGO_URI'`.

En MongoDB Atlas también hay que tener la IP del computador en la lista blanca
(**Network Access → Add IP Address**), o la conexión falla con un error de TLS.

## Las entidades

Son documentos de Mongo, no tablas:

```java
@Document(collection = "gestiones")
@Data
public class Gestion {

    @Id
    private String id;

    @Field("siniestro_id")
    private String siniestroId;
    ...
}
```

- `@Document` dice en qué colección vive.
- `@Id` es de `org.springframework.data.annotation`, no de JPA, y el id es **`String`**: Mongo genera
  un `ObjectId` en hexadecimal, no un número correlativo.
- `@Field` indica cómo se llama el campo dentro de la colección. Las propiedades en `camelCase`
  quedan en `snake_case` (`siniestroId` → `siniestro_id`).
- `@Data` de Lombok genera los getters y setters. Los objetos se crean con `new` y setters, no con builder.
- Los repositorios extienden `MongoRepository<Entidad, String>`.
- Los servicios y controladores usan `@RequiredArgsConstructor` para inyectar dependencias.
- Los DTOs son `record` de Java 21.

En IntelliJ IDEA el soporte de Lombok viene incluido. Si aparecen errores como `cannot find symbol getNombre()`,
revisar **Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing**.

## Cómo levantarlos

1. Definir `MONGO_URI` (ver arriba) y tener la IP autorizada en Atlas.
2. Levantar en este orden, porque unos consultan a otros:

   1. `roles-service` (`RolesServiceApplication`)
   2. `usuarios-service` (`UsuariosServiceApplication`)
   3. `catalogos-service` (`CatalogosServiceApplication`)
   4. `siniestros-service` (`SiniestrosServiceApplication`)

Las bases y colecciones se crean solas. La primera vez que cada servicio encuentra su colección vacía
carga los datos del caso:

- Roles con id fijo: `"1"` Asegurado, `"2"` Liquidador, `"3"` Analista
- 8 asegurados (`@correo.cl`), 4 liquidadores y 1 analista (`@segurosbpo.cl`), todos con contraseña `123456`
- 5 compañías de seguros y 6 tipos de siniestro

Los roles llevan id fijo a propósito: `usuarios` guarda ese valor en `rol_id`, y si Mongo inventara
un `ObjectId` no calzaría. Si cambias los nombres de los campos o las semillas, borra la base en Atlas:
la carga solo ocurre cuando la colección está vacía.

## Pruebas

20 pruebas unitarias, 5 por servicio, con JUnit 5 y Mockito. No necesitan Mongo ni que los servicios
estén levantados: los repositorios y los clientes HTTP van simulados.

```
mvn test
```

## Endpoints

### roles-service — `http://localhost:8081`

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/roles` | Lista de roles |
| GET | `/api/roles/{id}` | Un rol · 404 si no existe |

### usuarios-service — `http://localhost:8082`

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/api/usuarios?buscar=texto` | — | Lista (el filtro es opcional) |
| GET | `/api/usuarios/{id}` | — | Usuario · 404 |
| POST | `/api/usuarios` | `{"nombre","correo","contrasena","rut","equipo","rolId"}` | 201 usuario creado · 400 · 409 correo repetido |
| POST | `/api/usuarios/login` | `{"correo","contrasena"}` | 200 usuario · 401 credenciales incorrectas |
| PUT | `/api/usuarios/{id}` | `{"nombre","correo","rut","equipo","rolId"}` | 200 usuario · 404 · 409 |
| PUT | `/api/usuarios/{id}/contrasena` | `{"actual","nueva"}` | 204 · 401 clave actual incorrecta |
| DELETE | `/api/usuarios/{id}` | — | 204 · 404 (borra también su foto) |
| POST | `/api/usuarios/{id}/avatar` | multipart/form-data, campo `archivo` (imagen) | 200 usuario con `avatarUrl` · 400 si no es imagen |
| GET | `/api/usuarios/{id}/avatar` | — | La imagen JPEG (la URL completa viene en `avatarUrl`) · 404 si no tiene foto |

El campo `equipo` vale `VEHICULOS` o `HOGAR` en los liquidadores, y viene vacío en los demás.

Respuesta de un usuario (nunca incluye la contraseña):

```json
{
  "id": "6afe148b62e21d913e7b83f5",
  "nombre": "Camila Rojas Fuentes",
  "correo": "crojas@correo.cl",
  "rut": "15.842.371-5",
  "equipo": null,
  "rolId": "1",
  "rolNombre": "Asegurado",
  "avatarUrl": null
}
```

Los identificadores son **texto**, no números: `id` es el `ObjectId` que genera Mongo y `rolId` es
el id fijo del rol.

`avatarUrl` es `null` mientras el usuario no suba una foto. Al subirla queda, por ejemplo,
`"http://10.0.2.2:8082/api/usuarios/6afe.../avatar?v=3f2a...e1.jpg"` (el host es el mismo con el que
se hizo la petición). El parámetro `v` cambia con cada foto nueva, así la app no muestra una versión
antigua guardada en caché.

### catalogos-service — `http://localhost:8083`

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/api/companias` | — | Lista de compañías |
| GET | `/api/companias/{codigo}` | — | Una compañía · 404 |
| POST | `/api/companias` | `{"nombre","plazoDiasHabiles","activa"}` | 201 · 400 · 409 nombre repetido |
| GET | `/api/tipos-siniestro` | — | Lista de tipos |
| GET | `/api/tipos-siniestro/{codigo}` | — | Un tipo · 404 |
| POST | `/api/tipos-siniestro` | `{"nombre","esVehiculo","documentos":[]}` | 201 · 400 · 409 |

El `codigo` se arma desde el nombre: `Seguros Cordillera` queda como `SEGUROS_CORDILLERA`.

### siniestros-service — `http://localhost:8084`

| Método | Ruta | Cuerpo | Respuesta |
|---|---|---|---|
| GET | `/api/siniestros?aseguradoId={id}` | — | Los casos de ese asegurado |
| GET | `/api/siniestros?liquidadorId={id}` | — | Los casos de ese liquidador |
| GET | `/api/siniestros?sinAsignar=true` | — | Los casos recibidos sin liquidador |
| GET | `/api/siniestros/{id}` | — | Un caso · 404 |
| POST | `/api/siniestros` | `{"aseguradoId","companiaCodigo","tipoCodigo","fechaOcurrencia","comuna","descripcion","huboTerceros"}` | 201 · 400 |
| PUT | `/api/siniestros/{id}/asignar` | `{"liquidadorId"}` | 200 · 400 si el área no corresponde |
| PUT | `/api/siniestros/{id}/avanzar` | `{"resultado","mensajeCierre"}` | 200 · 400 |
| GET | `/api/siniestros/{id}/gestiones` | — | El historial del caso |
| POST | `/api/siniestros/{id}/gestiones` | `{"responsable","descripcion","visibleAsegurado"}` | 201 · 400 |
| GET | `/api/siniestros/{id}/evidencias` | — | Los archivos del caso |
| POST | `/api/siniestros/{id}/evidencias` | multipart/form-data: `archivo`, `tipoDocumento`, `subidoPor` | 201 · 400 |
| GET | `/api/siniestros/evidencias/{evidenciaId}/archivo` | — | El archivo · 404 |
| DELETE | `/api/siniestros/evidencias/{evidenciaId}` | — | 204 · 404 |

Las etapas avanzan en orden: `RECIBIDO` → `EVALUACION` → `LIQUIDACION` → `CERRADO`.
Para cerrar hay que mandar el resultado y el mensaje de cierre.

El número del caso (`SIN-26-004861`) se arma al registrarlo, a partir de cuántos casos hay en la colección.

La respuesta de un siniestro trae los nombres resueltos desde los otros servicios:

```json
{
  "id": "6afe14a2b1c0d87e5520934b",
  "numero": "SIN-26-004861",
  "aseguradoId": "6afe148b62e21d913e7b83f5",
  "aseguradoNombre": "Camila Rojas Fuentes",
  "companiaCodigo": "CORDILLERA",
  "companiaNombre": "Seguros Cordillera",
  "plazoDiasHabiles": 10,
  "tipoCodigo": "CHOQUE",
  "tipoNombre": "Vehículo · choque o colisión",
  "etapa": "EVALUACION",
  "liquidadorId": "6afe148b62e21d913e7b83fd",
  "liquidadorNombre": "Maria P. Lagos"
}
```

## Archivos subidos

- Las fotos de perfil se guardan en `uploads/avatares` dentro de `usuarios-service`, y las evidencias
  de los casos en `uploads/evidencias` dentro de `siniestros-service`. Las carpetas se crean solas y
  están ignoradas por Git.
- En Mongo queda solo el **nombre** del archivo, no el archivo mismo.
- Cada foto de perfil nueva recibe un nombre único (UUID) y la anterior se borra, así la app nunca
  muestra una foto vieja en caché.
- Probar en Postman: `POST http://localhost:8082/api/usuarios/{id}/avatar` → Body → **form-data** →
  key `archivo` de tipo **File** → elegir una imagen.

Ejemplo para probar en Postman — crear cuenta (`POST http://localhost:8082/api/usuarios`):

```json
{
  "nombre": "Ana Torres",
  "correo": "atorres@correo.cl",
  "contrasena": "secreto1",
  "rut": "16.555.444-3",
  "rolId": "1"
}
```

Si un servicio consultado está apagado, la respuesta es **503** indicando cuál falta:
`usuarios-service` lo dice del de roles, y `siniestros-service` del de catálogos o usuarios.

## Desde la app Android

- Emulador: las URL base son `http://10.0.2.2:8081/` a `http://10.0.2.2:8084/`
  (`10.0.2.2` = el `localhost` del computador; `localhost` dentro del emulador es el propio Android).
- Celular físico: usar la IP del computador en la misma red WiFi, por ejemplo `http://192.168.1.10:8082/`.
- Como es `http` (sin `s`), la app debe permitir tráfico sin cifrar (`usesCleartextTraffic`).
- En Windows puede hacer falta abrir los puertos en el firewall, o el emulador no alcanza al computador:

  ```powershell
  New-NetFirewallRule -DisplayName "Microservicios Seguros BPO" -Direction Inbound -Protocol TCP -LocalPort 8081-8084 -Action Allow
  ```
