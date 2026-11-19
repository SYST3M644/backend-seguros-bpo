package cl.duocuc.siniestros.dto;

public record SolicitudRequest(
        String tipoDocumento,
        String comentario,
        String solicitadoPor
) {
}
