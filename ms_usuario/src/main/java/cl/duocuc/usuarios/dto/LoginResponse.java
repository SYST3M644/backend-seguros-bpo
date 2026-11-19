package cl.duocuc.usuarios.dto;

public record LoginResponse(
        UsuarioResponse usuario,
        String token
) {
}
