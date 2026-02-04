package es.iescamas.socket;

/**
 * Clase principal de entrada a la aplicación (Entry Point).
 * <p>
 * Esta clase se encarga de configurar, iniciar y detener el servidor
 * {@link HiloPorClienteServidor} tras un tiempo de ejecución predefinido.
 * </p>
 *
 * @author Manuel Dominguez
 * @version 1.0
 * @since 2026-01
 */
public class Main {
		
	/** Puerto TCP donde escuchará el servidor. */
	final static int PORT = 9001; // He ajustado esto a 9001 para coincidir con tu servidor
	
	/** * Tiempo de ejecución del servidor en segundos. 
	 * 100000 segundos = aprox 27 horas.
	 */
	final static int TIME = 100000; 
	
	/**
	 * Método principal que arranca la aplicación.
	 * * @param args Argumentos de la línea de comandos (no utilizados en esta versión).
	 */
	public static void main(String[] args) {
		// Creamos el servidor indicando el puerto.
		HiloPorClienteServidor server = new HiloPorClienteServidor(PORT);
		
		// Arrancamos el servidor en un hilo aparte para no bloquear el main.
		new Thread(server, "Hilo-Servidor-principal").start();
		
		System.out.println("Servidor iniciado en http://localhost:" + PORT);
		System.out.println("El servidor se detendrá automáticamente en " + TIME + " segundos.");
	
		try {
			// El hilo principal duerme mientras el servidor trabaja en segundo plano.
			// TIME está en segundos, multiplicamos por 1000 para pasar a milisegundos.
			Thread.sleep(TIME * 1000L);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			System.err.print("El hilo principal fue interrumpido: " + ex.getMessage());
		}
		
		System.out.println("Tiempo agotado. Deteniendo servidor...");
		server.stop();
	}

}