# 🛠️ Ejercicio 2: Lectura Correcta de Bytes

Este ejercicio se centra en la gestión correcta de los buffers de entrada en Sockets TCP (Java). El objetivo es evitar la aparición de caracteres basura o símbolos extraños cuando el mensaje recibido es más corto que el tamaño del buffer reservado.

---

## 📋 Descripción del Problema

Al reservar un buffer de tamaño fijo (ej. 25 bytes) y recibir un mensaje más corto (ej. "Hola", 4 bytes), si convertimos el array completo a String, obtendremos el mensaje seguido de 21 bytes vacíos (nulos) o basura de memoria.

### 📊 Tabla de Diagnóstico y Solución

| Problema | Causa Técnica | Solución Implementada |
| :--- | :--- | :--- |
| **Basura al final del mensaje** | Se convierte el array `byte[]` entero a String, incluyendo las posiciones que no se han rellenado con datos nuevos. | Capturar el valor de retorno de `read()`: <br> `int leidos = is.read(buffer);` <br> Crear el String solo con esos bytes: <br> `new String(buffer, 0, leidos);` |
| **Mensaje cortado** | El buffer definido es más pequeño que el mensaje que envía el cliente (ej. Buffer de 10 bytes para un mensaje de 50). | No se soluciona solo con un `read` simple. Se requiere leer en bucle (`while`) o aumentar el tamaño del buffer. |

---

## 💻 Código de la Solución

El cambio principal se encuentra en la clase `ServidorSocketStream.java`. En lugar de ignorar cuántos bytes llegan, los contamos y procesamos solo la parte útil:

```java
byte[] buffer = new byte[25]; // Buffer grande
int bytesLeidos = is.read(buffer); // Devuelve cuántos bytes reales llegaron

if (bytesLeidos != -1) {
    // Constructor que toma: (buffer, offset_inicio, cantidad_a_leer)
    String mensaje = new String(buffer, 0, bytesLeidos); 
    System.out.println("Mensaje recibido limpio: " + mensaje);
}
