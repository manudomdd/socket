package es.iescamas.socket;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Tests de Integración para el Servidor TCP Multihilo.
 * Cumple con los requisitos obligatorios:
 * - C1: Test de ruta normal (/nombre/Ana).
 * - C2: Test de ruta desconocida (404).
 * - C2: Test de concurrencia (2 clientes simultáneos).
 */
class HiloPorClienteServidorTest {

    private HiloPorClienteServidor server;
    private Thread serverThread;
    private int port;

    @BeforeEach
    void startServer() throws Exception {
        // 1. Buscamos un puerto libre automático para evitar choques
        try (ServerSocket tmp = new ServerSocket(0)) {
            port = tmp.getLocalPort();
        }

        // 2. Arrancamos tu servidor HiloPorClienteServidor en un hilo aparte
        server = new HiloPorClienteServidor(port);
        serverThread = new Thread(server, "test-server");
        serverThread.start();

        // 3. Esperamos a que el servidor esté listo (evita falsos fallos)
        waitUntilListening("127.0.0.1", port, 1000);
    }

    @AfterEach
    void stopServer() throws Exception {
        // 4. Paramos el servidor limpiamente tras cada test
        server.stop();
        serverThread.join(1000);
    }

    // --- TEST C1 (Obligatorio): RUTA NORMAL ---
    @Test
    @DisplayName("GET /nombre/Ana devuelve 200 OK y saludo personalizado")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @Tag("http")
    void shouldSayHelloFromNombreRoute() throws Exception {
        String response = httpGet("/nombre/Ana");

        // Assert 1: Código 200
        assertTrue(response.contains("200 OK"), "Debe devolver código 200 OK");
        // Assert 2: Contenido esperado (tu HTML High-End)
        assertTrue(response.contains("Ana"), "El HTML debe contener el nombre solicitado (Ana)");
    }

    // --- TEST C2 (Obligatorio): ERROR 404 ---
    @Test
    @DisplayName("GET /ruta-inventada devuelve 404 Not Found")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    @Tag("error-handling")
    void shouldReturn404ForUnknownRoute() throws Exception {
        String response = httpGet("/ruta-inventada");

        // Assert 1: Código 404
        assertTrue(response.contains("404 Not Found"), "Debe devolver código 404");
        // Assert 2: Mensaje de error
        assertTrue(response.contains("Ruta desconocida"), "Debe indicar 'Ruta desconocida' en el HTML");
    }

    // --- TEST C2 (Obligatorio): CONCURRENCIA ---
    @Test
    @DisplayName("Dos clientes simultáneos reciben respuesta correcta")
    @Tag("concurrency")
    void shouldHandleConcurrentRequests() throws ExecutionException, InterruptedException {
        // Lanzamos 2 peticiones en paralelo usando CompletableFuture
        CompletableFuture<String> cliente1 = CompletableFuture.supplyAsync(() -> {
            try { return httpGet("/nombre/ClienteUno"); } catch (Exception e) { return null; }
        });

        CompletableFuture<String> cliente2 = CompletableFuture.supplyAsync(() -> {
            try { return httpGet("/nombre/ClienteDos"); } catch (Exception e) { return null; }
        });

        // Esperamos a que ambos terminen
        CompletableFuture.allOf(cliente1, cliente2).join();

        String res1 = cliente1.get();
        String res2 = cliente2.get();

        assertNotNull(res1, "El cliente 1 debería recibir respuesta");
        assertNotNull(res2, "El cliente 2 debería recibir respuesta");

        // Comprobamos que no se han cruzado los datos
        assertTrue(res1.contains("ClienteUno"), "Cliente 1 debe recibir su propio nombre");
        assertTrue(res2.contains("ClienteDos"), "Cliente 2 debe recibir su propio nombre");
    }

    // --- UTILIDADES ---
    private String httpGet(String path) throws Exception {
        try (Socket s = new Socket("127.0.0.1", port);
             OutputStream out = s.getOutputStream();
             InputStream in = s.getInputStream()) {
            
            // Enviamos petición HTTP simple
            String req = "GET " + path + " HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
            out.write(req.getBytes(StandardCharsets.US_ASCII));
            out.flush();
            
            // Leemos toda la respuesta
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void waitUntilListening(String host, int port, long maxMs) throws Exception {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < maxMs) {
            try (Socket ignored = new Socket(host, port)) { return; } 
            catch (IOException e) { Thread.sleep(50); }
        }
        fail("El servidor no abrió el puerto a tiempo");
    }
}