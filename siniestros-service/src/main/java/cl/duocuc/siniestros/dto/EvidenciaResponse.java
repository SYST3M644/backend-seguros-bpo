package cl.duocuc.siniestros.dto;

public record EvidenciaResponse(
        String id,
        String siniestroId,
        String tipoDocumento,
        String nombreArchivo,
        String mime,
        Integer tamanoBytes,
        String fechaRegistro,
        String subidoPor,
        String archivoUrl
) {
}
