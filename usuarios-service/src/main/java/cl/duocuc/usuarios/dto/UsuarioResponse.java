package cl.duocuc.usuarios.dto;

public record UsuarioResponse(
        String id,
        String nombre,
        String correo,
        String rut,
        String equipo,
        String rolId,
        String rolNombre,
        String avatarUrl
) {
}
