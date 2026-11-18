package cl.duocuc.siniestros.dto;

public record UsuarioDto(
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
