package cl.duocuc.siniestros.dto;

import java.util.List;

public record TipoSiniestroDto(
        String id,
        String codigo,
        String nombre,
        boolean esVehiculo,
        List<String> documentos
) {
}
