package cl.duocuc.usuarios.dto;

public record RegistroRequest(
        String nombre,
        String correo,
        String contrasena,
        String rut,
        String direccion,
        String equipo,
        String rolId,
        String pregunta,
        String respuesta
) {
}
