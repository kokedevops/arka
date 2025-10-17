package com.arka.arka.infrastructure.adapter.web;

import com.arka.arka.domain.port.in.ManageTemplatesUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controlador REST que expone los endpoints para la gestión de plantillas.
 * Actúa como un adaptador de entrada, traduciendo las peticiones HTTP
 * a llamadas al caso de uso del dominio.
 */
@RestController
public class TemplateController {

    private final ManageTemplatesUseCase manageTemplatesUseCase;

    /**
     * Constructor que inyecta el caso de uso de gestión de plantillas.
     * @param manageTemplatesUseCase El puerto de entrada del dominio.
     */
    public TemplateController(ManageTemplatesUseCase manageTemplatesUseCase) {
        this.manageTemplatesUseCase = manageTemplatesUseCase;
    }

    /**
     * Endpoint para obtener el contenido de una plantilla por su nombre.
     * @param templateName El nombre de la plantilla.
     * @return Una ResponseEntity con el contenido de la plantilla o un mensaje de error.
     */
    @GetMapping("/templates/{templateName}")
    public ResponseEntity<String> getTemplate(@PathVariable String templateName) {
        Optional<String> content = manageTemplatesUseCase.getTemplate(templateName);
        if (content.isEmpty()) {
            // Retorna 404 Not Found si la plantilla no se encuentra
            return new ResponseEntity<>("Error: Plantilla no encontrada o no se pudo cargar.", HttpStatus.NOT_FOUND);
        }
        // Retorna 200 OK con el contenido de la plantilla
        return new ResponseEntity<>(content.get(), HttpStatus.OK);
    }
}
