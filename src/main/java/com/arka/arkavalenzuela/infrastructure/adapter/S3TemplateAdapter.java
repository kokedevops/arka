package com.arka.arkavalenzuela.infrastructure.adapter;

import com.arka.arkavalenzuela.domain.port.out.TemplateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException; // Importar para manejar plantillas no encontradas
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Adaptador de infraestructura que implementa el puerto TemplateRepository
 * utilizando el SDK de AWS S3 para obtener el contenido de las plantillas.
 */
@Component
public class S3TemplateAdapter implements TemplateRepository {

    private final S3Client s3Client;

    // El nombre del bucket se inyecta desde las propiedades de la aplicación
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    /**
     * Constructor que inicializa el cliente S3.
     * La región de AWS se inyecta desde las propiedades de la aplicación.
     * @param awsRegion La región de AWS.
     */
    public S3TemplateAdapter(@Value("${aws.region}") String awsRegion) {
        this.s3Client = S3Client.builder()
                .region(Region.of(awsRegion)) // Configura la región del cliente S3
                .build();
    }

    /**
     * Implementación del método para obtener el contenido de una plantilla desde S3.
     * @param templateName El nombre (clave) de la plantilla en el bucket S3.
     * @return Un Optional que contiene el contenido de la plantilla como String si se encuentra,
     * o un Optional vacío si la plantilla no existe o hay un error.
     */
    @Override
    public Optional<String> getTemplateContent(String templateName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(templateName)
                .build();

        try (ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest)) {
            // Lee todos los bytes del InputStream y los convierte a String usando UTF-8
            return Optional.of(new String(s3Object.readAllBytes(), StandardCharsets.UTF_8));
        } catch (NoSuchKeyException e) {
            // La plantilla no fue encontrada en S3
            System.err.println("La plantilla '" + templateName + "' no fue encontrada en el bucket '" + bucketName + "'.");
            return Optional.empty();
        } catch (IOException e) {
            // Error de I/O al leer el contenido del objeto
            System.err.println("Error de I/O al obtener la plantilla '" + templateName + "' de S3: " + e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            // Captura cualquier otra excepción inesperada durante la operación S3
            System.err.println("Error inesperado al obtener la plantilla '" + templateName + "' de S3: " + e.getMessage());
            return Optional.empty();
        }
    }
}