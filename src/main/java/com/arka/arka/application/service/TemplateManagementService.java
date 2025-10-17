package com.arka.arka.application.service;


import com.arka.arka.domain.port.in.ManageTemplatesUseCase;
import com.arka.arka.domain.port.out.TemplateRepository;
import java.util.Optional;

/**
 * Implementación del caso de uso ManageTemplatesUseCase.
 * Contiene la lógica de negocio para la gestión de plantillas,
 * utilizando el puerto de salida TemplateRepository.
 */
public class TemplateManagementService implements ManageTemplatesUseCase {

    private final TemplateRepository templateRepository;

    /**
     * Constructor que inyecta el repositorio de plantillas.
     * @param templateRepository El puerto de salida para acceder a las plantillas.
     */
    public TemplateManagementService(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    /**
     * Obtiene el contenido de una plantilla a través del repositorio.
     * @param templateName El nombre de la plantilla.
     * @return Un Optional con el contenido de la plantilla.
     */
    @Override
    public Optional<String> getTemplate(String templateName) {
        return templateRepository.getTemplateContent(templateName);
    }
}
