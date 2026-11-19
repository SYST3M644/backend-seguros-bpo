package cl.duocuc.siniestros.dto;

public record CargaLiquidadorResponse(
        String liquidadorId,
        String liquidadorNombre,
        String equipo,
        long abiertos,
        long cerrados
) {
}
