package com.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class AppCliente {
    
    static final int PORT = 7777; // Asegúrate de que coincida con el servidor
    static final String IP = "192.168.1.204"; // Tu IP correcta

    public static void main(String[] args) {
        
        try (Socket socket = new Socket(IP, PORT);
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader entradaSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader entradaConsola = new BufferedReader(new InputStreamReader(System.in))) {
            
            // 1. PRIMER PASO: Saludo inicial (solo se hace una vez)
            System.out.print("<Cliente> Introduce tu nombre: "); 
            String nombre = entradaConsola.readLine();
            salida.println(nombre); // Enviamos el nombre al servidor

            String mensajeDelServidor;
            
            while ((mensajeDelServidor = entradaSocket.readLine()) != null) {
                
                System.out.println("<Servidor>: " + mensajeDelServidor);

                // Si el servidor nos corta la conexión o se despide, salimos
                if (mensajeDelServidor.contains("Fin") || mensajeDelServidor.contains("CORRECTO")) {
                    break;
                }

                // B. Solo ahora pedimos al usuario que escriba
                System.out.print("<Cliente> Escribe un numero:");
                String respuesta = entradaConsola.readLine();
                
                // C. Enviamos la respuesta al servidor
                salida.println(respuesta);
            }
            
        } catch (UnknownHostException ex) {
            System.err.println("No se encuentra el servidor.");
        } catch (IOException e) {
            System.err.println("Error de conexión: " + e.getMessage());
        }
    }
}
