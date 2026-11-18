package cl.duocuc.siniestros.dto;

public record RegistrarSiniestroRequest(
        String aseguradoId,
        String companiaCodigo,
        String tipoCodigo,
        String fechaOcurrencia,
        String comuna,
        String descripcion,
        Boolean huboTerceros
) {
}
