package cl.duocuc.usuarios.dto;

public record ActualizarUsuarioRequest(
        String nombre,
        String correo,
        String rut,
        String direccion,
        String equipo,
        String rolId
) {
}
