# Proyecto Chat en Java

## Resumen Detallado

Este proyecto implementa un sistema de chat cliente-servidor en Java, utilizando sockets para la comunicación y Swing para las interfaces gráficas. A continuación se explica paso a paso el funcionamiento del código:

### 1. Inicio del Servidor

- El servidor se inicia ejecutando la clase `Server`.
- Se crea una ventana (`ServerFrame`) que muestra:
  - El puerto en uso.
  - La lista de usuarios conectados.
  - Todos los mensajes enviados y recibidos.
- El servidor abre un `ServerSocket` en el puerto 5050 y queda a la espera de conexiones de clientes.

### 2. Conexión de Clientes

- Cada cliente ejecuta la clase `Client`, que abre una ventana de login (`LoginFrame`).
- El usuario ingresa su nombre y, al hacer clic en "Conectar", se abre la ventana principal del chat (`ChatFrame`).
- El cliente se conecta al servidor usando un socket TCP y envía su nombre de usuario.

### 3. Gestión de Usuarios en el Servidor

- Cuando un cliente se conecta, el servidor recibe el nombre de usuario.
- Si el nombre es válido y no está repetido, se agrega a la lista de usuarios conectados.
- El servidor actualiza la lista de usuarios en la interfaz gráfica y notifica a todos los clientes sobre el nuevo usuario.

### 4. Envío y Recepción de Mensajes

- Cada vez que un cliente envía un mensaje, este se transmite al servidor.
- El servidor reenvía (broadcast) el mensaje a todos los clientes conectados.
- Tanto el servidor como todos los clientes muestran el mensaje en sus respectivas áreas de chat.

### 5. Desconexión de Usuarios

- Si un cliente se desconecta, el servidor elimina su nombre de la lista de usuarios.
- Se actualiza la interfaz gráfica del servidor y se notifica a los demás clientes que el usuario ha salido del chat.

### 6. Componentes Principales del Código

- **Server.java**:  
  - Gestiona las conexiones, usuarios y mensajes.
  - Contiene la clase interna `ServerFrame` para la interfaz gráfica del servidor.
- **Client.java**:  
  - Punto de entrada para el cliente. Lanza la ventana de login.
- **LoginFrame.java**:  
  - Ventana donde el usuario ingresa su nombre antes de conectarse al chat.
- **ChatFrame.java**:  
  - Ventana principal del chat para el usuario.
  - Permite enviar y recibir mensajes en tiempo real.

### 7. Flujo de Datos

1. El cliente se conecta y envía su nombre al servidor.
2. El servidor valida y agrega el usuario, notificando a todos los clientes.
3. Los mensajes enviados por cualquier cliente son retransmitidos por el servidor a todos los demás.
4. El servidor y los clientes actualizan sus interfaces gráficas en tiempo real.

### 8. Tecnologías Utilizadas

- Java SE
- Swing (interfaces gráficas)
- Sockets TCP/IP

---

Este proyecto es ideal para aprender sobre programación concurrente, manejo de sockets y desarrollo de interfaces gráficas en Java, mostrando cómo se puede construir una aplicación de chat funcional y visualmente amigable.
