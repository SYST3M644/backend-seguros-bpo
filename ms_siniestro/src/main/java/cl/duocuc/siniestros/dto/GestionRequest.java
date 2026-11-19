package cl.duocuc.siniestros.dto;

public record GestionRequest(
        String responsable,
        String descripcion,
        Boolean visibleAsegurado
) {
}
