package cl.duocuc.catalogos.controller;

import cl.duocuc.catalogos.dto.CompaniaRequest;
import cl.duocuc.catalogos.dto.TipoSiniestroRequest;
import cl.duocuc.catalogos.dto.TipoSiniestroResponse;
import cl.duocuc.catalogos.model.Compania;
import cl.duocuc.catalogos.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogoService;

    @GetMapping("/companias")
    public List<Compania> listarCompanias() {
        return catalogoService.listarCompanias();
    }

    @GetMapping("/companias/{codigo}")
    public Compania obtenerCompania(@PathVariable String codigo) {
        return catalogoService.obtenerCompania(codigo);
    }

    @PostMapping("/companias")
    @ResponseStatus(HttpStatus.CREATED)
    public Compania crearCompania(@RequestBody CompaniaRequest datos) {
        return catalogoService.crearCompania(datos);
    }

    @GetMapping("/tipos-siniestro")
    public List<TipoSiniestroResponse> listarTipos() {
        return catalogoService.listarTipos();
    }

    @GetMapping("/tipos-siniestro/{codigo}")
    public TipoSiniestroResponse obtenerTipo(@PathVariable String codigo) {
        return catalogoService.obtenerTipo(codigo);
    }

    @PostMapping("/tipos-siniestro")
    @ResponseStatus(HttpStatus.CREATED)
    public TipoSiniestroResponse crearTipo(@RequestBody TipoSiniestroRequest datos) {
        return catalogoService.crearTipo(datos);
    }
}
