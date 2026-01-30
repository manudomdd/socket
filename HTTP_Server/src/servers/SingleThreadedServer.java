package servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class SingleThreadedServer implements Runnable {

    protected int serverPort = 9001;
    protected ServerSocket serversocket = null;
    protected boolean isStopped = false;

    public SingleThreadedServer(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        openServerSocket();
        
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serversocket.accept();
                processClientRequest(clientSocket);
            } catch (IOException e) {
                if (isStopped()) {
                    System.out.println("Servidor detenido.");
                    return;
                }
                throw new RuntimeException("Error aceptando conexión", e);
            }
        }
    }

    private void processClientRequest(Socket clientSocket) throws IOException {
        InputStream input = clientSocket.getInputStream();
        OutputStream output = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        String requestLine = reader.readLine();
        String responseBody;
        String status = "200 OK";
        boolean isSuccess = false;

        if (requestLine != null && !requestLine.isEmpty()) {
            String[] parts = requestLine.split(" ");
            String path = parts[1];

            if (path.equals("/favicon.ico")) {
                 clientSocket.close();
                 return;
            }

            // --- MEJORA: ENDPOINT DE ESTADO DEL SISTEMA ---
            if (path.equalsIgnoreCase("/status")) {
                isSuccess = true;
                Runtime rt = Runtime.getRuntime();
                long totalMem = rt.totalMemory() / (1024 * 1024);
                long freeMem = rt.freeMemory() / (1024 * 1024);
                
                String stats = String.format(
                    "Estado del Sistema: <b>Operativo</b><br>" +
                    "Memoria en uso: <code>%d MB</code><br>" +
                    "Hilos activos: <code>%d</code><br>" +
                    "Arquitectura: <code>%s</code>",
                    (totalMem - freeMem),
                    Thread.activeCount(),
                    System.getProperty("os.arch")
                );
                
                responseBody = getHighEndTemplate(isSuccess, "Panel de Control", stats);

            } else if (path.length() > 1) {
                // Tu endpoint original de bienvenida
                String nombreRaw = path.substring(1);
                String nombre = URLDecoder.decode(nombreRaw, StandardCharsets.UTF_8);
                nombre = nombre.substring(0, 1).toUpperCase() + nombre.substring(1).toLowerCase();
                
                isSuccess = true;
                responseBody = getHighEndTemplate(isSuccess, "¡Bienvenido, " + nombre + "!", "Tu servidor Java está funcionando perfectamente.");
            } else {
                status = "404 Not Found";
                isSuccess = false;
                responseBody = getHighEndTemplate(isSuccess, "Ups, ruta desconocida", "Por favor, añade tu nombre al final de la URL.<br>Ejemplo: <code>/TuNombre</code><br>O revisa el <code>/status</code>");
            }
        } else {
            responseBody = "";
        }

        byte[] responseBytes = responseBody.getBytes(StandardCharsets.UTF_8);

        String httpHeaders = "HTTP/1.1 " + status + "\r\n" +
                             "Content-Type: text/html; charset=UTF-8\r\n" +
                             "Content-Length: " + responseBytes.length + "\r\n" +
                             "Connection: close\r\n" +
                             "\r\n"; 

        output.write(httpHeaders.getBytes(StandardCharsets.UTF_8));
        output.write(responseBytes);
        output.flush();
        clientSocket.close();
    }

    private String getHighEndTemplate(boolean success, String titulo, String mensaje) {
        String accentColor = success ? "#00f260" : "#ff4b1f"; 
        String gradient = success 
            ? "linear-gradient(-45deg, #667eea 0%, #764ba2 25%, #f093fb 50%, #4facfe 75%, #00f2fe 100%)"
            : "linear-gradient(-45deg, #0f0c29, #302b63, #24243e, #0f0c29)";

        String icon = success ? "✓" : "✕";
        String glowColor = success ? "0, 242, 96" : "255, 75, 31";

        return "<!DOCTYPE html>" +
               "<html lang='es'>" +
               "<head>" +
               "<meta charset='UTF-8'>" +
               "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
               "<title>Java Sockets Pro</title>" +
               "<link href='https://fonts.googleapis.com/css2?family=Inter:wght@300;500;700;900&family=JetBrains+Mono:wght@400;700&display=swap' rel='stylesheet'>" +
               "<style>" +
               "* { margin: 0; padding: 0; box-sizing: border-box; }" +
               "body { font-family: 'Inter', sans-serif; height: 100vh; overflow: hidden; display: flex; justify-content: center; align-items: center; background: " + gradient + "; background-size: 400% 400%; animation: gradientShift 20s ease infinite; position: relative; }" +
               "@keyframes gradientShift { 0%, 100% { background-position: 0% 50%; } 25% { background-position: 100% 50%; } 50% { background-position: 50% 100%; } 75% { background-position: 50% 0%; } }" +
               ".particles { position: absolute; width: 100%; height: 100%; overflow: hidden; pointer-events: none; }" +
               ".particle { position: absolute; width: 3px; height: 3px; background: rgba(255,255,255,0.6); border-radius: 50%; animation: float 15s infinite; box-shadow: 0 0 10px rgba(255,255,255,0.8); }" +
               "@keyframes float { 0% { transform: translateY(100vh) rotate(0deg); opacity: 0; } 10% { opacity: 1; } 90% { opacity: 1; } 100% { transform: translateY(-100vh) rotate(720deg); opacity: 0; } }" +
               ".particle:nth-child(1) { left: 10%; animation-delay: 0s; animation-duration: 12s; }" +
               ".particle:nth-child(2) { left: 25%; animation-delay: 2s; animation-duration: 15s; }" +
               ".particle:nth-child(3) { left: 40%; animation-delay: 4s; animation-duration: 18s; }" +
               ".particle:nth-child(4) { left: 55%; animation-delay: 1s; animation-duration: 14s; }" +
               ".particle:nth-child(5) { left: 70%; animation-delay: 3s; animation-duration: 16s; }" +
               ".particle:nth-child(6) { left: 85%; animation-delay: 5s; animation-duration: 13s; }" +
               ".particle:nth-child(7) { left: 15%; animation-delay: 6s; animation-duration: 17s; width: 5px; height: 5px; }" +
               ".particle:nth-child(8) { left: 60%; animation-delay: 7s; animation-duration: 11s; width: 4px; height: 4px; }" +
               ".glass-card { position: relative; background: rgba(255, 255, 255, 0.12); backdrop-filter: blur(25px); -webkit-backdrop-filter: blur(25px); border-radius: 30px; border: 1.5px solid rgba(255, 255, 255, 0.25); padding: 3.5rem 3rem; text-align: center; color: white; max-width: 520px; width: 90%; box-shadow: 0 20px 60px rgba(0,0,0,0.4), 0 0 80px rgba(" + glowColor + ", 0.15), inset 0 1px 0 rgba(255,255,255,0.3); transform: translateY(30px) rotateX(5deg); opacity: 0; animation: cardEntrance 1s cubic-bezier(0.34, 1.56, 0.64, 1) forwards; transform-style: preserve-3d; perspective: 1000px; transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1); }" +
               "@keyframes cardEntrance { to { transform: translateY(0) rotateX(0deg); opacity: 1; } }" +
               ".icon-box { position: relative; font-size: 5.5rem; margin-bottom: 1.5rem; color: " + accentColor + "; filter: drop-shadow(0 10px 25px rgba(" + glowColor + ", 0.5)); animation: iconPulse 2s ease-in-out infinite, iconRotate 1.2s ease-out; transform-origin: center; }" +
               "@keyframes iconPulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.08); } }" +
               "@keyframes iconRotate { 0% { transform: scale(0) rotate(-180deg); opacity: 0; } 100% { transform: scale(1) rotate(0deg); opacity: 1; } }" +
               "h1 { font-size: 2.6rem; margin: 0 0 1.2rem 0; font-weight: 900; letter-spacing: -1.5px; background: linear-gradient(135deg, #fff 0%, rgba(255,255,255,0.7) 100%); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text; filter: drop-shadow(0 4px 8px rgba(0,0,0,0.3)); }" +
               "p { font-size: 1.15rem; font-weight: 400; line-height: 1.7; color: rgba(255,255,255,0.92); }" +
               "code { font-family: 'JetBrains Mono', monospace; background: rgba(0,0,0,0.4); padding: 2px 8px; border-radius: 4px; color: #ffd700; }" +
               ".btn { display: inline-block; margin-top: 1.8rem; padding: 14px 32px; background: white; color: #333; text-decoration: none; border-radius: 50px; font-weight: 700; transition: all 0.3s; }" +
               ".btn:hover { transform: translateY(-3px); box-shadow: 0 10px 20px rgba(0,0,0,0.2); }" +
               ".footer { margin-top: 2.5rem; padding-top: 1.5rem; border-top: 1px solid rgba(255,255,255,0.15); font-size: 0.7rem; opacity: 0.7; text-transform: uppercase; letter-spacing: 3px; }" +
               ".status-badge { display: inline-block; margin-bottom: 1rem; padding: 6px 16px; background: rgba(" + glowColor + ", 0.2); border: 1px solid rgba(" + glowColor + ", 0.4); border-radius: 20px; font-size: 0.75rem; font-weight: 600; text-transform: uppercase; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "  <div class='particles'>" +
               "    <div class='particle'></div><div class='particle'></div><div class='particle'></div><div class='particle'></div>" +
               "  </div>" +
               "  <div class='glass-card'>" +
               "    <div class='status-badge'>" + (success ? "✓ System Online" : "⚠ Error") + "</div>" +
               "    <div class='icon-box'>" + icon + "</div>" +
               "    <h1>" + titulo + "</h1>" +
               "    <p>" + mensaje + "</p>" +
               "    <div class='footer'>Socket Server • Java Premium Edition</div>" +
               "  </div>" +
               "</body></html>";
    }

    public synchronized void stop() {
        this.isStopped = true;
        try { if (this.serversocket != null) this.serversocket.close(); } catch (IOException e) { e.printStackTrace(); }
    }
    
    private synchronized boolean isStopped() { return this.isStopped; }

    private void openServerSocket() {
        try { this.serversocket = new ServerSocket(this.serverPort); } catch (IOException ex) { throw new RuntimeException(ex); }
    }
}