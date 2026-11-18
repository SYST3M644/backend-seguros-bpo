# Seguros BPO — Microservicios

Cuatro microservicios Spring Boot 4.1 (JDK 21, JPA, MySQL) que usa la app Seguros BPO.

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

## Lombok

Las entidades usan `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` y `@Builder`
(no `@Data`: su `equals`/`hashCode` da problemas con las entidades de JPA), y los servicios, controladores
y configuraciones usan `@RequiredArgsConstructor` para inyectar dependencias. Los DTOs son `record` de Java 21.

En IntelliJ IDEA el soporte de Lombok viene incluido. Si aparecen errores como `cannot find symbol getNombre()`,
revisar **Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing**.

## Cómo levantarlos

1. Iniciar MySQL (Laragon, XAMPP o MySQL Server) en el puerto 3306, usuario `root` sin contraseña.
   Si tu MySQL tiene otra clave, cámbiala en `src/main/resources/application.properties`.
   Las bases de datos se crean solas (`createDatabaseIfNotExist=true`).
2. Levantar en este orden, porque unos consultan a otros:

   1. `roles-service` (`RolesServiceApplication`)
   2. `usuarios-service` (`UsuariosServiceApplication`)
   3. `catalogos-service` (`CatalogosServiceApplication`)
   4. `siniestros-service` (`SiniestrosServiceApplication`)

Al iniciar por primera vez se cargan los datos del caso:

- Roles: 1 Asegurado, 2 Liquidador, 3 Analista
- 8 asegurados (`@correo.cl`), 4 liquidadores y 1 analista (`@segurosbpo.cl`), todos con contraseña `123456`
- 5 compañías de seguros y 6 tipos de siniestro

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
  "id": 1,
  "nombre": "Camila Rojas Fuentes",
  "correo": "crojas@correo.cl",
  "rut": "15.842.371-5",
  "equipo": null,
  "rolId": 1,
  "rolNombre": "Asegurado",
  "avatarUrl": null
}
```

`avatarUrl` es `null` mientras el usuario no suba una foto. Al subirla queda, por ejemplo,
`"http://10.0.2.2:8082/api/usuarios/1/avatar?v=3f2a...e1.jpg"` (el host es el mismo con el que se hizo la petición).
El parámetro `v` cambia con cada foto nueva, así la app no muestra una versión antigua guardada en caché.

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
| GET | `/api/siniestros?aseguradoId=1` | — | Los casos de ese asegurado |
| GET | `/api/siniestros?liquidadorId=9` | — | Los casos de ese liquidador |
| GET | `/api/siniestros?sinAsignar=true` | — | Los casos recibidos sin liquidador |
| GET | `/api/siniestros/{id}` | — | Un caso · 404 |
| POST | `/api/siniestros` | `{"aseguradoId","companiaCodigo","tipoCodigo","fechaOcurrencia","comuna","descripcion","huboTerceros"}` | 201 · 400 |
| PUT | `/api/siniestros/{id}/asignar` | `{"liquidadorId"}` | 200 · 400 si el área no corresponde |
| PUT | `/api/siniestros/{id}/avanzar` | `{"resultado","mensajeCierre"}` | 200 · 400 |
| GET | `/api/siniestros/{id}/gestiones` | — | El historial del caso |
| POST | `/api/siniestros/{id}/gestiones` | `{"responsable","descripcion","visibleAsegurado"}` | 201 · 400 |

Las etapas avanzan en orden: `RECIBIDO` → `EVALUACION` → `LIQUIDACION` → `CERRADO`.
Para cerrar hay que mandar el resultado y el mensaje de cierre.

La respuesta de un siniestro trae los nombres resueltos desde los otros servicios:

```json
{
  "id": 1,
  "numero": "SIN-26-004861",
  "aseguradoId": 1,
  "aseguradoNombre": "Camila Rojas Fuentes",
  "companiaCodigo": "CORDILLERA",
  "companiaNombre": "Seguros Cordillera",
  "plazoDiasHabiles": 10,
  "tipoCodigo": "CHOQUE",
  "tipoNombre": "Vehículo · choque o colisión",
  "etapa": "EVALUACION",
  "liquidadorId": 9,
  "liquidadorNombre": "Maria P. Lagos"
}
```

## Fotos de perfil

- Las imágenes se guardan como archivo en la carpeta `uploads/avatares` del proyecto `usuarios-service`
  (se crea sola). En MySQL solo queda el **nombre** del archivo (columna `avatar`).
- Cada foto nueva recibe un nombre único (UUID) y la anterior se borra, así la app nunca muestra una foto vieja en caché.
- Probar en Postman: `POST http://localhost:8082/api/usuarios/1/avatar` → Body → **form-data** →
  key `archivo` de tipo **File** → elegir una imagen.

Ejemplo para probar en Postman — crear cuenta (`POST http://localhost:8082/api/usuarios`):

```json
{
  "nombre": "Ana Torres",
  "correo": "atorres@correo.cl",
  "contrasena": "secreto1",
  "rut": "16.555.444-3",
  "rolId": 1
}
```

Si un servicio consultado está apagado, la respuesta es **503** indicando cuál falta:
`usuarios-service` lo dice del de roles, y `siniestros-service` del de catálogos o usuarios.

## Desde la app Android

- Emulador: las URL base son `http://10.0.2.2:8082/` y `http://10.0.2.2:8084/`
  (`10.0.2.2` = el `localhost` del computador).
- Celular físico: usar la IP del computador en la misma red WiFi, por ejemplo `http://192.168.1.10:8082/`.
- Como es `http` (sin `s`), la app debe permitir tráfico sin cifrar (`usesCleartextTraffic`).
