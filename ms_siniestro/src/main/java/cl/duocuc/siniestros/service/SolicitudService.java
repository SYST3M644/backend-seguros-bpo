package cl.duocuc.siniestros.service;

import cl.duocuc.siniestros.dto.SolicitudRequest;
import cl.duocuc.siniestros.model.Siniestro;
import cl.duocuc.siniestros.model.Solicitud;
import cl.duocuc.siniestros.repository.SiniestroRepository;
import cl.duocuc.siniestros.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final String PENDIENTE = "PENDIENTE";
    private static final String RECIBIDA = "RECIBIDA";
    private static final String ANULADA = "ANULADA";

    private final SolicitudRepository solicitudRepository;
    private final SiniestroRepository siniestroRepository;
    private final AvisoService avisoService;

    public List<Solicitud> listar(String siniestroId) {
        return solicitudRepository.findBySiniestroIdOrderByIdDesc(siniestroId);
    }

    public Solicitud crear(String siniestroId, SolicitudRequest datos) {
        String tipo = texto(datos.tipoDocumento());
        if (tipo.isEmpty() || tipo.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Indica qué documento necesitas, hasta 100 caracteres");
        }
        Siniestro siniestro = buscarSiniestro(siniestroId);

        Solicitud solicitud = new Solicitud();
        solicitud.setSiniestroId(siniestroId);
        solicitud.setTipoDocumento(tipo);
        solicitud.setComentario(texto(datos.comentario()));
        solicitud.setSolicitadoPor(texto(datos.solicitadoPor()));
        solicitud.setFecha(LocalDateTime.now().format(FECHA_HORA));
        solicitud.setEstado(PENDIENTE);
        Solicitud guardada = solicitudRepository.save(solicitud);

        avisoService.crear(siniestro, siniestro.getAseguradoId(),
                "Te piden un documento: " + tipo);
        return guardada;
    }

    public Solicitud marcarRecibida(String id) {
        Solicitud solicitud = buscar(id);
        if (!PENDIENTE.equals(solicitud.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esta solicitud ya no está pendiente");
        }
        solicitud.setEstado(RECIBIDA);
        Solicitud guardada = solicitudRepository.save(solicitud);

        Siniestro siniestro = buscarSiniestro(solicitud.getSiniestroId());
        avisoService.crear(siniestro, siniestro.getLiquidadorId(),
                "El asegurado envió: " + solicitud.getTipoDocumento());
        return guardada;
    }

    public Solicitud anular(String id) {
        Solicitud solicitud = buscar(id);
        if (!PENDIENTE.equals(solicitud.getEstado())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Esta solicitud ya no está pendiente");
        }
        solicitud.setEstado(ANULADA);
        return solicitudRepository.save(solicitud);
    }

    private Solicitud buscar(String id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La solicitud no existe"));
    }

    private Siniestro buscarSiniestro(String id) {
        return siniestroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Siniestro no encontrado"));
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
