package cl.duocuc.siniestros.controller;

import cl.duocuc.siniestros.dto.ConsultaRequest;
import cl.duocuc.siniestros.dto.EvaluacionRequest;
import cl.duocuc.siniestros.dto.SolicitudRequest;
import cl.duocuc.siniestros.model.Aviso;
import cl.duocuc.siniestros.model.Consulta;
import cl.duocuc.siniestros.model.Evaluacion;
import cl.duocuc.siniestros.model.Solicitud;
import cl.duocuc.siniestros.service.AvisoService;
import cl.duocuc.siniestros.service.CasoExtraService;
import cl.duocuc.siniestros.service.SolicitudService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/siniestros")
@RequiredArgsConstructor
public class AvisoController {

    private final AvisoService avisoService;
    private final SolicitudService solicitudService;
    private final CasoExtraService casoExtraService;

    @GetMapping("/avisos")
    public List<Aviso> avisos(@RequestParam String usuarioId) {
        return avisoService.listar(usuarioId);
    }

    @GetMapping("/avisos/sin-leer")
    public long sinLeer(@RequestParam String usuarioId) {
        return avisoService.sinLeer(usuarioId);
    }

    @PutMapping("/avisos/{id}/leido")
    public Aviso marcarLeido(@PathVariable String id) {
        return avisoService.marcarLeido(id);
    }

    @GetMapping("/{id}/consultas")
    public List<Consulta> consultas(@PathVariable String id) {
        return casoExtraService.listarConsultas(id);
    }

    @PostMapping("/{id}/consultas")
    @ResponseStatus(HttpStatus.CREATED)
    public Consulta preguntar(@PathVariable String id, @RequestBody ConsultaRequest datos) {
        return casoExtraService.preguntar(id, datos);
    }

    @PutMapping("/consultas/{id}/responder")
    public Consulta responder(@PathVariable String id, @RequestBody ConsultaRequest datos) {
        return casoExtraService.responder(id, datos);
    }

    @GetMapping("/{id}/evaluacion")
    public Evaluacion evaluacion(@PathVariable String id) {
        return casoExtraService.obtenerEvaluacion(id);
    }

    @PutMapping("/{id}/evaluacion")
    public Evaluacion evaluar(@PathVariable String id, @RequestBody EvaluacionRequest datos) {
        return casoExtraService.evaluar(id, datos);
    }

    @GetMapping("/{id}/solicitudes")
    public List<Solicitud> solicitudes(@PathVariable String id) {
        return solicitudService.listar(id);
    }

    @PostMapping("/{id}/solicitudes")
    @ResponseStatus(HttpStatus.CREATED)
    public Solicitud solicitar(@PathVariable String id, @RequestBody SolicitudRequest datos) {
        return solicitudService.crear(id, datos);
    }

    @PutMapping("/solicitudes/{id}/recibida")
    public Solicitud marcarRecibida(@PathVariable String id) {
        return solicitudService.marcarRecibida(id);
    }

    @DeleteMapping("/solicitudes/{id}")
    public Solicitud anular(@PathVariable String id) {
        return solicitudService.anular(id);
    }
}
