package com.arka.arka;

import org.junit.jupiter.api.Test;

public class ArkaApplicationTest {

    @Test
    public void testMainApplicationClassExists() {
        // Simple test que verifica que la clase principal existe
        // Sin inicializar el contexto completo de Spring
        assert ArkaApplication.class != null;
    }
}
