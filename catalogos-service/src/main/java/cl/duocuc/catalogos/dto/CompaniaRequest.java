package cl.duocuc.catalogos.dto;

public record CompaniaRequest(
        String nombre,
        Integer plazoDiasHabiles,
        Boolean activa
) {
}
