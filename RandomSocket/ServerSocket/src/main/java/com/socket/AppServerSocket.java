package com.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Hello world!
 *
 */
public class AppServerSocket 
{
	private final static int PORT = 7777;
	private static int numGen;
    public static void main( String[] args ) 
    {
    	//Generar número
    	numGen = (new Random()).nextInt(10)+1;
    	
    	try {
    		ServerSocket srvSock = new ServerSocket(PORT);
    		System.out.println("ServerSocket en puerto: " + PORT);

    		//Abrimos el socket y escuchamos
    		Socket client = srvSock.accept();
    		mostrarInfoCliente(client);
    		
    		
    		//Para mandar datos al cliente >>>
    		PrintWriter salida = new PrintWriter(client.getOutputStream(), true);
    		//Para recibir datos al cliente <<<
    		BufferedReader entrada = new BufferedReader(new InputStreamReader(client.getInputStream()));
    		
    		String nombre = entrada.readLine(); 
    		System.out.println("Jugador identificado: " + nombre); 
    		salida.println(saludar(nombre));  
    		
    		int intentos = 0; 
    		String datoRec; 
    		
    		//leeremos todos los mensajes recibidos
    		//comprobamos si es el número mágico
    		while((datoRec = entrada.readLine())!= null) {
    			intentos++; 
    			String respuesta = checkNumero(datoRec, intentos);
    			
    			//Retornamos al cliente el resultado 
    			//de la comprobación
    			salida.println(respuesta);
    			
    			if (respuesta.startsWith("CORRECTO")) {
    				System.out.println("Juego terminado. Ganador: " + nombre); 
    				break; 
    			}
    		}
    		
    	}catch(IOException e) {
    		System.err.println("Problemas en el socket");
    		e.printStackTrace();
    		System.exit(1);
    	}
    }
	private static void mostrarInfoCliente(Socket client) {
			// Obtener la IP del cliente
			InetAddress clientAddress = client.getInetAddress();
			String clientIP = clientAddress.getHostAddress();
			String hostName = clientAddress.getHostName();
		
			System.out.println("IP:" + clientIP + ", HostName: "+ hostName);
	}
	private static String checkNumero(String datoRec, int intentos) {
		try {
			int numero = Integer.parseInt(datoRec);
			
			if (numero == numGen) {
				return "¡CORRECTO!. Has adivinado el numero en " + intentos + " intentos"; 
			} else if(numero > numGen) {
				return "<server>El número es mayor que el número mágico";
			} else {
				return "<server>El número es menor que el número mágico";
			}
			
			
		}catch(NumberFormatException e) {
			return "<Server>ERROR: " + datoRec + " no es un numero. Escribe un numero: ";
		}
		
	}


    private static String saludar(String nombre) {
        return "Hola " + nombre + " que comience el juego";
    }
}
    
    


