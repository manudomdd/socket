package es.iescamas.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Servidor TCP Multihilo Final.
 * Incluye: Hilo por cliente, HTML High-End, detección de nombre y endpoint /status.
 */
public class HiloPorClienteServidor implements Runnable {

    protected int serverPort = 9001;
    protected ServerSocket serversocket = null;
    protected boolean isStopped;
    protected Thread runningThread = null;

    public HiloPorClienteServidor(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }

        openServerSocket();

        while (!isStopped()) {
            try {
                Socket clientSocket = this.serversocket.accept();

                // MULTIHILO: Cada cliente se procesa en paralelo
                new Thread(() -> {
                    try {
                        processClientRequest(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, "client-" + clientSocket.getPort()).start();

            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Server stopped.");
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }
        System.out.println("Server Stopped");
    }

    private void processClientRequest(Socket clientSocket) throws IOException {
        try (clientSocket;
             InputStream in = clientSocket.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = br.readLine();
            if (requestLine == null || requestLine.isBlank()) return;

            String path = "/";
            if (requestLine.startsWith("GET ")) {
                int start = 4;
                int end = requestLine.indexOf(' ', start);
                if (end > start) 
                    path = requestLine.substring(start, end);
            }

            if ("/favicon.ico".equals(path)) {
                serveFavicon(out);
                return;
            }

            // --- LÓGICA DE ENDPOINTS (STATUS + NOMBRE) ---
            String responseBody;
            String status = "200 OK";
            String remote = clientSocket.getRemoteSocketAddress().toString();

            if (path.equalsIgnoreCase("/status")) {
                // MEJORA 2: Endpoint de Estado del Sistema
                Runtime rt = Runtime.getRuntime();
                long totalMem = rt.totalMemory() / (1024 * 1024);
                long freeMem = rt.freeMemory() / (1024 * 1024);
                long usedMem = totalMem - freeMem;

                String stats = String.format(
                    "Estado: <b>Operativo</b><br>" +
                    "Memoria usada: <code>%d MB</code><br>" +
                    "Hilos activos: <code>%d</code><br>" +
                    "ID Hilo actual: <code>%s</code><br>" +
                    "Tu IP: <code>%s</code>",
                    usedMem,
                    Thread.activeCount(), // Evidencia de concurrencia
                    Thread.currentThread().getName(),
                    remote
                );
                
                responseBody = getHighEndTemplate(true, "Monitor del Sistema", stats);

            } else if (path.length() > 1) {
                // MEJORA 1: Detección de nombre en URL
                String nombreRaw = path.substring(1);
                String nombre = URLDecoder.decode(nombreRaw, StandardCharsets.UTF_8);
                
                String info = "Petición procesada correctamente.<br>Conexión desde: " + remote;
                responseBody = getHighEndTemplate(true, "¡Hola, " + nombre + "!", info);

            } else {
                // Ruta raíz o desconocida (404)
                status = "404 Not Found";
                responseBody = getHighEndTemplate(false, "404 - Ruta desconocida", 
                    "Por favor, añade tu nombre a la URL (ej: /Pepe) o visita <code>/status</code>");
            }

            // Enviar respuesta
            byte[] bodyBytes = responseBody.getBytes(StandardCharsets.UTF_8);
            String headers =
                    "HTTP/1.1 " + status + "\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + bodyBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(bodyBytes);
            out.flush();

            // Log en consola
            System.out.println("[" + Thread.currentThread().getName() + "] Path: " + path + " | Status: " + status);
        }
    }

    /**
     * Plantilla HTML High-End con CSS y animaciones.
     */
    private String getHighEndTemplate(boolean success, String titulo, String mensaje) {
        String accentColor = success ? "#00f260" : "#ff4b1f"; 
        String gradient = success 
            ? "linear-gradient(-45deg, #667eea 0%, #764ba2 25%, #f093fb 50%, #4facfe 75%, #00f2fe 100%)"
            : "linear-gradient(-45deg, #0f0c29, #302b63, #24243e, #0f0c29)";
        String icon = success ? "✓" : "✕";
        String glowColor = success ? "0, 242, 96" : "255, 75, 31";

        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'>" +
               "<title>Java MultiThread Server</title>" +
               "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@400;900&family=JetBrains+Mono&display=swap' rel='stylesheet'>" +
               "<style>" +
               "body { font-family: 'Inter', sans-serif; height: 100vh; margin: 0; display: flex; justify-content: center; align-items: center; background: " + gradient + "; background-size: 400% 400%; animation: g 15s ease infinite; color: white; overflow: hidden; }" +
               "@keyframes g { 0%, 100% { background-position: 0% 50%; } 50% { background-position: 100% 50%; } }" +
               ".card { background: rgba(255, 255, 255, 0.1); backdrop-filter: blur(20px); border-radius: 30px; border: 1px solid rgba(255, 255, 255, 0.2); padding: 3rem; text-align: center; box-shadow: 0 20px 50px rgba(0,0,0,0.3); max-width: 500px; width: 90%; }" +
               ".icon { font-size: 5rem; color: " + accentColor + "; text-shadow: 0 0 20px rgba(" + glowColor + ", 0.6); margin-bottom: 1rem; }" +
               "h1 { font-weight: 900; margin-bottom: 1rem; letter-spacing: -1px; }" +
               "p { line-height: 1.6; opacity: 0.9; font-size: 1.1rem; }" +
               "code { font-family: 'JetBrains Mono'; background: rgba(0,0,0,0.3); padding: 2px 6px; border-radius: 5px; color: #ffd700; }" +
               ".footer { margin-top: 2rem; font-size: 0.7rem; opacity: 0.5; letter-spacing: 2px; text-transform: uppercase; border-top: 1px solid rgba(255,255,255,0.1); padding-top: 1rem; }" +
               "</style></head><body><div class='card'>" +
               "<div class='icon'>" + icon + "</div>" +
               "<h1>" + titulo + "</h1>" +
               "<p>" + mensaje + "</p>" +
               "<div class='footer'>Thread-per-Client Architecture • Java 2026</div>" +
               "</div></body></html>";
    }

    private void serveFavicon(OutputStream out) throws IOException {
        try (InputStream iconStream = HiloPorClienteServidor.class.getResourceAsStream("/favicon.ico")) {
            if (iconStream == null) {
                out.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
                return;
            }
            byte[] iconBytes = iconStream.readAllBytes();
            String headers = "HTTP/1.1 200 OK\r\nContent-Type: image/x-icon\r\nContent-Length: " + iconBytes.length + "\r\n\r\n";
            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(iconBytes);
            out.flush();
        }
    }

    private void openServerSocket() {
        try { this.serversocket = new ServerSocket(this.serverPort); } 
        catch (IOException ex) { throw new RuntimeException("Cannot open port " + serverPort, ex); }
    }

    private synchronized boolean isStopped() { return isStopped; }

    public synchronized void stop() {
        this.isStopped = true;
        try { if (this.serversocket != null) this.serversocket.close(); } 
        catch (IOException e) { System.err.println(e); }
    }
}