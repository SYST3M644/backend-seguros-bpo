package cl.duocuc.catalogos.dto;

import java.util.List;

public record TipoSiniestroRequest(
        String nombre,
        Boolean esVehiculo,
        List<String> documentos
) {
}
