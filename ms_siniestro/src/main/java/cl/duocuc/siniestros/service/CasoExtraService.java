package cl.duocuc.siniestros.service;

import cl.duocuc.siniestros.dto.ConsultaRequest;
import cl.duocuc.siniestros.dto.EvaluacionRequest;
import cl.duocuc.siniestros.model.Consulta;
import cl.duocuc.siniestros.model.Evaluacion;
import cl.duocuc.siniestros.model.Siniestro;
import cl.duocuc.siniestros.repository.ConsultaRepository;
import cl.duocuc.siniestros.repository.EvaluacionRepository;
import cl.duocuc.siniestros.repository.SiniestroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CasoExtraService {

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private final ConsultaRepository consultaRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final SiniestroRepository siniestroRepository;
    private final AvisoService avisoService;

    public List<Consulta> listarConsultas(String siniestroId) {
        return consultaRepository.findBySiniestroIdOrderByIdDesc(siniestroId);
    }

    public Consulta preguntar(String siniestroId, ConsultaRequest datos) {
        String pregunta = texto(datos.texto());
        if (pregunta.length() < 5) {
            throw invalido("Escribe tu consulta, mínimo 5 caracteres");
        }
        Siniestro siniestro = buscar(siniestroId);

        Consulta consulta = new Consulta();
        consulta.setSiniestroId(siniestroId);
        consulta.setPregunta(pregunta);
        consulta.setFechaPregunta(LocalDateTime.now().format(FECHA_HORA));
        Consulta guardada = consultaRepository.save(consulta);

        avisoService.crear(siniestro, siniestro.getLiquidadorId(),
                "El asegurado hizo una consulta");
        return guardada;
    }

    public Consulta responder(String id, ConsultaRequest datos) {
        String respuesta = texto(datos.texto());
        if (respuesta.length() < 5) {
            throw invalido("Escribe la respuesta, mínimo 5 caracteres");
        }
        Consulta consulta = consultaRepository.findById(id)
                .orElseThrow(() -> noEncontrado("La consulta no existe"));
        if (texto(consulta.getRespuesta()).length() > 0) {
            throw invalido("Esta consulta ya fue respondida");
        }
        consulta.setRespuesta(respuesta);
        consulta.setFechaRespuesta(LocalDateTime.now().format(FECHA_HORA));
        Consulta guardada = consultaRepository.save(consulta);

        Siniestro siniestro = buscar(consulta.getSiniestroId());
        avisoService.crear(siniestro, siniestro.getAseguradoId(),
                "Respondieron tu consulta");
        return guardada;
    }

    public Evaluacion obtenerEvaluacion(String siniestroId) {
        return evaluacionRepository.findBySiniestroId(siniestroId)
                .orElseThrow(() -> noEncontrado("Este caso aún no tiene evaluación"));
    }

    public Evaluacion evaluar(String siniestroId, EvaluacionRequest datos) {
        if (datos.nota() == null || datos.nota() < 1 || datos.nota() > 5) {
            throw invalido("La nota debe estar entre 1 y 5");
        }
        Siniestro siniestro = buscar(siniestroId);
        if (!"CERRADO".equals(siniestro.getEtapa())) {
            throw invalido("Solo se puede evaluar un caso cerrado");
        }

        Evaluacion evaluacion = evaluacionRepository.findBySiniestroId(siniestroId)
                .orElseGet(Evaluacion::new);
        evaluacion.setSiniestroId(siniestroId);
        evaluacion.setNota(datos.nota());
        evaluacion.setComentario(texto(datos.comentario()));
        evaluacion.setFecha(LocalDateTime.now().format(FECHA_HORA));
        return evaluacionRepository.save(evaluacion);
    }

    private Siniestro buscar(String id) {
        return siniestroRepository.findById(id)
                .orElseThrow(() -> noEncontrado("Siniestro no encontrado"));
    }

    private String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private ResponseStatusException invalido(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }

    private ResponseStatusException noEncontrado(String mensaje) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensaje);
    }
}
