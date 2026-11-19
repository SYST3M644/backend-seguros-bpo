package cl.duocuc.catalogos.dto;

import java.util.List;

public record TipoSiniestroResponse(
        String id,
        String codigo,
        String nombre,
        boolean esVehiculo,
        List<String> documentos
) {
}
