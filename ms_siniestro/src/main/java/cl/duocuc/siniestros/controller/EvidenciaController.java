package cl.duocuc.siniestros.controller;

import cl.duocuc.siniestros.dto.EvidenciaResponse;
import cl.duocuc.siniestros.service.EvidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/siniestros")
@RequiredArgsConstructor
public class EvidenciaController {

    private final EvidenciaService evidenciaService;

    @GetMapping("/{id}/evidencias")
    public List<EvidenciaResponse> listar(@PathVariable String id) {
        return evidenciaService.listar(id);
    }

    @PostMapping("/{id}/evidencias")
    @ResponseStatus(HttpStatus.CREATED)
    public EvidenciaResponse guardar(
            @PathVariable String id,
            @RequestParam String tipoDocumento,
            @RequestParam(required = false) String subidoPor,
            @RequestParam MultipartFile archivo) {
        return evidenciaService.guardar(id, tipoDocumento, subidoPor, archivo);
    }

    @GetMapping("/evidencias/{evidenciaId}/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable String evidenciaId) {
        byte[] contenido = evidenciaService.leerArchivo(evidenciaId);
        String mime = evidenciaService.mimeDe(evidenciaId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mime))
                .body(contenido);
    }

    @DeleteMapping("/evidencias/{evidenciaId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String evidenciaId) {
        evidenciaService.eliminar(evidenciaId);
    }
}
