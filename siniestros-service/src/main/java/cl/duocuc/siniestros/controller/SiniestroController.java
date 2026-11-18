package cl.duocuc.siniestros.controller;

import cl.duocuc.siniestros.dto.AsignarRequest;
import cl.duocuc.siniestros.dto.AvanzarRequest;
import cl.duocuc.siniestros.dto.GestionRequest;
import cl.duocuc.siniestros.dto.RegistrarSiniestroRequest;
import cl.duocuc.siniestros.dto.SiniestroResponse;
import cl.duocuc.siniestros.model.Gestion;
import cl.duocuc.siniestros.service.SiniestroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class SiniestroController {

    private final SiniestroService siniestroService;

    @GetMapping
    public List<SiniestroResponse> listar(
            @RequestParam(required = false) String aseguradoId,
            @RequestParam(required = false) String liquidadorId,
            @RequestParam(required = false, defaultValue = "false") boolean sinAsignar) {
        if (sinAsignar) {
            return siniestroService.listarSinAsignar();
        }
        if (aseguradoId != null) {
            return siniestroService.listarPorAsegurado(aseguradoId);
        }
        if (liquidadorId != null) {
            return siniestroService.listarPorLiquidador(liquidadorId);
        }
        return List.of();
    }

    @GetMapping("/{id}")
    public SiniestroResponse obtener(@PathVariable String id) {
        return siniestroService.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SiniestroResponse registrar(@RequestBody RegistrarSiniestroRequest datos) {
        return siniestroService.registrar(datos);
    }

    @PutMapping("/{id}/asignar")
    public SiniestroResponse asignar(@PathVariable String id, @RequestBody AsignarRequest datos) {
        return siniestroService.asignar(id, datos);
    }

    @PutMapping("/{id}/avanzar")
    public SiniestroResponse avanzar(@PathVariable String id, @RequestBody AvanzarRequest datos) {
        return siniestroService.avanzar(id, datos);
    }

    @GetMapping("/{id}/gestiones")
    public List<Gestion> listarGestiones(@PathVariable String id) {
        return siniestroService.listarGestiones(id);
    }

    @PostMapping("/{id}/gestiones")
    @ResponseStatus(HttpStatus.CREATED)
    public Gestion agregarGestion(@PathVariable String id, @RequestBody GestionRequest datos) {
        return siniestroService.agregarGestion(id, datos);
    }
}
