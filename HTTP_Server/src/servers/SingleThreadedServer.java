package servers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleThreadedServer implements Runnable {

    protected int serverPort = 9001;
    protected ServerSocket serversocket = null;
    protected boolean isStopped;
    protected Thread runningThread = null;

    public SingleThreadedServer(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public void run() {
        synchronized (this) {
            this.runningThread = Thread.currentThread();
        }
        openServerSocket();
        while (!isStopped()) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serversocket.accept();
                processClientRequest(clientSocket);
            } catch (IOException e) {
                if (isStopped()) {
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
        }
    }

    private void processClientRequest(Socket clientSocket) throws IOException {
        InputStream input = clientSocket.getInputStream();
        OutputStream output = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String requestLine = reader.readLine();

        String responseContent = "<html><body>Ruta no encontrada</body></html>";

        if (requestLine != null && !requestLine.isEmpty()) {
            String[] parts = requestLine.split(" ");
            String path = parts[1]; 

            if (path.length() > 1) {
                String nombre = path.substring(1); 
                responseContent = "<html><body><h1>Hola " + nombre + "</h1></body></html>";
            }
        }

        String httpResponse = "HTTP/1.1 200 OK\r\n" +
                              "Content-Type: text/html; charset=UTF-8\r\n" +
                              "Content-Length: " + responseContent.length() + "\r\n" +
                              "\r\n" +
                              responseContent;

        output.write(httpResponse.getBytes());
        output.flush();
        clientSocket.close();
    }

    private boolean isStopped() {
        return isStopped;
    }

    private void openServerSocket() {
        try {
            this.serversocket = new ServerSocket(this.serverPort);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot open port " + serverPort);
        }
    }

    public synchronized void stop() {
        this.isStopped = true;
        try {
            this.serversocket.close();
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}