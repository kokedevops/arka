package com.arka.arka.domain.port.in;

import java.util.Optional;

/**
 * Puerto de entrada que define las operaciones de negocio relacionadas con la gestión de plantillas.
 * La capa de aplicación (ej. un controlador REST) interactúa con esta interfaz.
 */
public interface ManageTemplatesUseCase {
    /**
     * Obtiene el contenido de una plantilla específica.
     * @param templateName El nombre de la plantilla a obtener.
     * @return Un Optional que contiene el contenido de la plantilla si se encuentra, o vacío si no.
     */
    Optional<String> getTemplate(String templateName);
}
