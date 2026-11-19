package cl.duocuc.usuarios.service;

import cl.duocuc.usuarios.client.RolClient;
import cl.duocuc.usuarios.dto.ActualizarUsuarioRequest;
import cl.duocuc.usuarios.dto.CambiarContrasenaRequest;
import cl.duocuc.usuarios.dto.LoginRequest;
import cl.duocuc.usuarios.dto.LoginResponse;
import cl.duocuc.usuarios.dto.PreguntaResponse;
import cl.duocuc.usuarios.dto.RecuperarRequest;
import cl.duocuc.usuarios.dto.RegistroRequest;
import cl.duocuc.usuarios.dto.RolDto;
import cl.duocuc.usuarios.dto.UsuarioResponse;
import cl.duocuc.usuarios.model.Usuario;
import cl.duocuc.usuarios.model.Avatar;
import cl.duocuc.usuarios.repository.AvatarRepository;
import cl.duocuc.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String ROL_ASEGURADO = "1";
    private static final String PENDIENTE = "PENDIENTE";
    private static final String APROBADO = "APROBADO";
    private static final String RECHAZADO = "RECHAZADO";

    private final UsuarioRepository usuarioRepository;
    private final RolClient rolClient;
    private final ImagenService imagenService;
    private final AvatarRepository avatarRepository;
    private final TokenService tokenService;

    public List<UsuarioResponse> listar(String buscar) {
        List<Usuario> usuarios = (buscar == null || buscar.isBlank())
                ? usuarioRepository.findAllByOrderByNombreAsc()
                : usuarioRepository.findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCaseOrderByNombreAsc(buscar, buscar);

        Map<String, String> roles = rolClient.listar().stream()
                .collect(Collectors.toMap(RolDto::id, RolDto::nombre));

        return usuarios.stream()
                .map(usuario -> aRespuesta(usuario, roles.getOrDefault(usuario.getRolId(), "")))
                .toList();
    }

    public UsuarioResponse obtener(String id) {
        return aRespuesta(buscar(id));
    }

    public UsuarioResponse registrar(RegistroRequest datos) {
        if (vacio(datos.nombre()) || vacio(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre y el correo son obligatorios");
        }
        if (datos.contrasena() == null || datos.contrasena().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener mínimo 6 caracteres");
        }
        if (usuarioRepository.existsByCorreo(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }

        String rolId = datos.rolId() != null ? datos.rolId() : ROL_ASEGURADO;
        validarRol(rolId);

        Usuario nuevo = new Usuario();
        nuevo.setNombre(datos.nombre());
        nuevo.setCorreo(datos.correo());
        nuevo.setContrasena(datos.contrasena());
        nuevo.setRut(datos.rut() == null ? "" : datos.rut().trim());
        nuevo.setDireccion(texto(datos.direccion()));
        nuevo.setEquipo(datos.equipo());
        nuevo.setRolId(rolId);
        nuevo.setPregunta(texto(datos.pregunta()));
        nuevo.setRespuesta(normalizar(datos.respuesta()));
        nuevo.setEstado(ROL_ASEGURADO.equals(rolId) ? PENDIENTE : APROBADO);
        return aRespuesta(usuarioRepository.save(nuevo));
    }

    public PreguntaResponse preguntaDe(String correo) {
        Usuario usuario = usuarioRepository.findByCorreo(texto(correo))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay una cuenta con ese correo"));
        if (vacio(usuario.getPregunta())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Esta cuenta no tiene pregunta de recuperación");
        }
        return new PreguntaResponse(usuario.getCorreo(), usuario.getPregunta());
    }

    public void recuperar(RecuperarRequest datos) {
        if (datos.nueva() == null || datos.nueva().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener mínimo 6 caracteres");
        }
        Usuario usuario = usuarioRepository.findByCorreo(texto(datos.correo()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No hay una cuenta con ese correo"));
        if (vacio(usuario.getRespuesta())
                || !usuario.getRespuesta().equals(normalizar(datos.respuesta()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "La respuesta no coincide");
        }
        usuario.setContrasena(datos.nueva());
        usuarioRepository.save(usuario);
    }

    private String normalizar(String valor) {
        return texto(valor).toLowerCase();
    }

    public LoginResponse iniciarSesion(LoginRequest datos) {
        Usuario usuario = usuarioRepository.findByCorreo(datos.correo())
                .filter(u -> u.getContrasena().equals(datos.contrasena()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (PENDIENTE.equals(usuario.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Tu cuenta todavía no está aprobada por el analista");
        }
        if (RECHAZADO.equals(usuario.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El analista rechazó esta cuenta");
        }
        return new LoginResponse(aRespuesta(usuario), tokenService.generar(usuario));
    }

    public UsuarioResponse actualizar(String id, ActualizarUsuarioRequest datos) {
        Usuario usuario = buscar(id);

        if (vacio(datos.nombre()) || vacio(datos.correo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre y el correo son obligatorios");
        }
        if (usuarioRepository.existsByCorreoAndIdNot(datos.correo(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
        }
        if (datos.rolId() != null) {
            validarRol(datos.rolId());
            usuario.setRolId(datos.rolId());
        }

        usuario.setNombre(datos.nombre());
        usuario.setCorreo(datos.correo());
        usuario.setRut(datos.rut() == null ? "" : datos.rut().trim());
        usuario.setEquipo(datos.equipo());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public void cambiarContrasena(String id, CambiarContrasenaRequest datos) {
        Usuario usuario = buscar(id);

        if (!usuario.getContrasena().equals(datos.actual())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La contraseña actual es incorrecta");
        }
        if (datos.nueva() == null || datos.nueva().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener mínimo 6 caracteres");
        }

        usuario.setContrasena(datos.nueva());
        usuarioRepository.save(usuario);
    }

    public List<UsuarioResponse> pendientes() {
        return usuarioRepository.findByRolIdAndEstadoOrderByNombreAsc(ROL_ASEGURADO, PENDIENTE)
                .stream()
                .map(this::aRespuesta)
                .toList();
    }

    public UsuarioResponse aprobar(String id) {
        return cambiarEstado(id, APROBADO);
    }

    public UsuarioResponse rechazar(String id) {
        return cambiarEstado(id, RECHAZADO);
    }

    private UsuarioResponse cambiarEstado(String id, String estado) {
        Usuario usuario = buscar(id);
        if (!ROL_ASEGURADO.equals(usuario.getRolId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Solo se aprueban cuentas de asegurado");
        }
        usuario.setEstado(estado);
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public UsuarioResponse subirAvatar(String id, MultipartFile archivo) {
        Usuario usuario = buscar(id);

        Avatar imagen = new Avatar();
        imagen.setId(usuario.getId());
        imagen.setContenido(imagenService.leer(archivo));
        avatarRepository.save(imagen);

        usuario.setAvatar(UUID.randomUUID().toString());
        return aRespuesta(usuarioRepository.save(usuario));
    }

    public byte[] obtenerAvatar(String id) {
        return avatarRepository.findById(buscar(id).getId())
                .map(Avatar::getContenido)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El usuario no tiene foto"));
    }

    public void eliminar(String id) {
        usuarioRepository.delete(buscar(id));
        avatarRepository.deleteById(id);
    }

    private Usuario buscar(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void validarRol(String rolId) {
        if (rolClient.obtener(rolId) == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol indicado no existe");
        }
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        RolDto rol = rolClient.obtener(usuario.getRolId());
        return aRespuesta(usuario, rol != null ? rol.nombre() : "");
    }

    private UsuarioResponse aRespuesta(Usuario usuario, String rolNombre) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRut(),
                usuario.getDireccion(),
                usuario.getEquipo(),
                usuario.getRolId(),
                rolNombre,
                usuario.getEstado() == null ? APROBADO : usuario.getEstado(),
                urlAvatar(usuario)
        );
    }

    private String urlAvatar(Usuario usuario) {
        if (usuario.getAvatar() == null) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/usuarios/" + usuario.getId() + "/avatar")
                .queryParam("v", usuario.getAvatar())
                .toUriString();
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
