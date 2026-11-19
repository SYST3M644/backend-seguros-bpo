package cl.duocuc.usuarios.dto;

public record UsuarioResponse(
        String id,
        String nombre,
        String correo,
        String rut,
        String direccion,
        String equipo,
        String rolId,
        String rolNombre,
        String estado,
        String avatarUrl
) {
}
