# 🚀 Práctica de Sockets: TCP en Java

Este proyecto implementa una comunicación básica Cliente-Servidor utilizando Sockets TCP en Java. Se han realizado dos ejercicios enfocados en la configuración de red en entornos virtualizados y en la gestión correcta de buffers de lectura.

---

## 📋 Estructura del Proyecto

El proyecto está dividido en ramas según el ejercicio:
* `main`: Código base.
* `e1_vm_ip_puerto`: Configuración de conexión VM ↔ Host.
* `e2_lectura_bytes_leidos`: Corrección de lectura de buffers.

---

## 🛠️ Ejercicio 1: Conexión VM (Servidor) ↔ Host (Cliente)

**Objetivo:** Establecer conexión entre un cliente situado en el sistema anfitrión (Pop!_OS) y un servidor alojado en una Máquina Virtual (Linux Lite).

### ⚙️ Configuración de Red
* **Modo de Red VirtualBox:** Adaptador Puente (Bridged Adapter).
* **Dirección IP de la VM:** 192.168.1.203
* **Puerto de Escucha:** 6000

### 📝 Cómo obtuve la IP
Para obtener la dirección IP de la máquina virtual, utilicé el siguiente comando en la terminal de Linux Lite:

```bash
hostname -I
