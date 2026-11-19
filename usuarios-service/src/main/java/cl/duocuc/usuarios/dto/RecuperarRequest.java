package cl.duocuc.usuarios.dto;

public record RecuperarRequest(
        String correo,
        String respuesta,
        String nueva
) {
}
