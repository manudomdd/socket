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
 * Servidor TCP que atiende clientes mediante un hilo por conexión.
 * Sirve HTML básico y saluda al usuario si se indica su nombre en la URL.
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

                // MULTIHILO: Creamos un hilo nuevo por cada cliente
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
             // Usamos UTF-8 para leer bien los caracteres especiales del nombre
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

            // --- LÓGICA DE MEJORA: NOMBRE EN URL ---
            String nombre = "Invitado";
            // Si la ruta es mayor que 1 (ej: "/Pepe"), extraemos el nombre
            if (path.length() > 1) {
                String nombreRaw = path.substring(1); // Quitamos la barra "/"
                nombre = URLDecoder.decode(nombreRaw, StandardCharsets.UTF_8);
            }

            // Datos básicos para el HTML
            String clientIp = clientSocket.getInetAddress().getHostAddress();
            int clientPort = clientSocket.getPort();
            String remote = clientSocket.getRemoteSocketAddress().toString();
            long time = System.currentTimeMillis();
            String fecha = new SimpleDateFormat("dd/MM/yy HH:mm:ss").format(new Date(time));

            // HTML BÁSICO (Sin mejoras visuales high-end todavía)
            String body = "<html>"
                    + "<head>"
                    + "<link rel='icon' href='/favicon.ico'>"
                    + "<title>Programación de Servicios y Procesos</title>"
                    + "</head>"
                    + "<body style='background-color: coral;'>"
                    // Aquí inyectamos el nombre decodificado
                    + "<h1 style='color:white;'>¡Hola, " + nombre + "!</h1>"
                    + "<h3 style='color:blue;'>Servidor OK</h3>"
                    + "<p>Path original: " + path + "</p>"
                    + "<p>Server Time: " + fecha + "</p>"
                    + "<p>Hilo: " + Thread.currentThread().getName() + "</p>"
                    + "<p>Cliente IP: " + clientIp + "</p>"
                    + "<p>Cliente puerto: " + clientPort + "</p>"
                    + "<p>Remote: " + remote + "</p>"
                    + "</body></html>";

            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

            String headers =
                    "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + bodyBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n";

            out.write(headers.getBytes(StandardCharsets.US_ASCII));
            out.write(bodyBytes);
            out.flush();

            System.out.println("[" + Thread.currentThread().getName() + "] " + requestLine);
            System.out.println("[" + Thread.currentThread().getName() + "] Saludando a: " + nombre);
        }
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