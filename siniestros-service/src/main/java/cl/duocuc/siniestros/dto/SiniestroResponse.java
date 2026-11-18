package cl.duocuc.siniestros.dto;

public record SiniestroResponse(
        String id,
        String numero,
        String aseguradoId,
        String aseguradoNombre,
        String companiaCodigo,
        String companiaNombre,
        Integer plazoDiasHabiles,
        String tipoCodigo,
        String tipoNombre,
        String fechaOcurrencia,
        String fechaReporte,
        String comuna,
        String descripcion,
        boolean huboTerceros,
        String etapa,
        String liquidadorId,
        String liquidadorNombre,
        String resultadoCierre,
        String mensajeCierre,
        String ultimaActualizacion
) {
}
