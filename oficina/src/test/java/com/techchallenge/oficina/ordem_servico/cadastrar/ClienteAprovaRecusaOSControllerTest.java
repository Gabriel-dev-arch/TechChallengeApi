package com.techchallenge.oficina.ordem_servico.cadastrar;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ClienteAprovaRecusaOSControllerTest {

    @Test
    void deveAprovarOSDoCliente() {
        ClienteAprovaRecusaOSService service = mock(ClienteAprovaRecusaOSService.class);
        ClienteAprovaRecusaOSController controller = new ClienteAprovaRecusaOSController(service);
        UUID id = UUID.randomUUID();

        ResponseEntity<?> response = controller.incluiPecasInsumosOS(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).aprovaOS(id);
    }

    @Test
    void deveRecusarOSDoCliente() {
        ClienteAprovaRecusaOSService service = mock(ClienteAprovaRecusaOSService.class);
        ClienteAprovaRecusaOSController controller = new ClienteAprovaRecusaOSController(service);
        UUID id = UUID.randomUUID();

        ResponseEntity<?> response = controller.recusar(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(service).recusaOS(id);
    }
}
