package cl.duocuc.siniestros.dto;

public record CompaniaDto(
        String id,
        String codigo,
        String nombre,
        boolean activa,
        Integer plazoDiasHabiles
) {
}
