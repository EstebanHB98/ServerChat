package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class Server {
    private static final int PORT = 5050;
    // HashSet es una colección que no permite elementos duplicados
    private static HashSet<PrintWriter> salidas = new HashSet<>();
    private static HashSet<String> usuarios = new HashSet<>();
    private static ServerFrame serverFrame;

    public static void main(String[] args) throws Exception {
        // Iniciar la interfaz gráfica del servidor en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            serverFrame = new ServerFrame(PORT);
            serverFrame.setVisible(true);
        });

        System.out.println("Servidor iniciado...");
        // Crear el socket del servidor en el puerto especificado
        ServerSocket servidor = new ServerSocket(PORT);

        try {
            // Bucle infinito para aceptar conexiones de clientes
            while (true) {
                // Por cada cliente que se conecta, se crea un hilo ClientHandler
                new ClientHandler(servidor.accept()).start();
            }
        } finally {
            // Al cerrar el servidor, se libera el puerto
            servidor.close();
        }
    }

    private static class ClientHandler extends Thread {
        private String nombreUsuario;
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Procesar el nombreUsuario
                nombreUsuario = in.readLine();
                if (nombreUsuario == null || usuarios.contains(nombreUsuario)) {
                    return;
                }
                synchronized (usuarios) {
                    usuarios.add(nombreUsuario);
                    sendListaUsuarios();
                    updateListaUsuarios();
                }
                salidas.add(out);

                // Notificar a todos que un nuevo usuario se ha conectado
                broadcast("SERVIDOR: "+ nombreUsuario + " se ha unido al chat");

                // Mostrar en la interfaz gráfica
                appendMensajeToFrame("SERVIDOR: " + nombreUsuario + " se ha unido al chat");

                // Procesar mensajes
                String mensaje;
                while ((mensaje = in.readLine()) != null) {
                    broadcast(nombreUsuario + ": " + mensaje);
                    appendMensajeToFrame(nombreUsuario + ": " + mensaje);
                }

            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (nombreUsuario != null) {
                    usuarios.remove(nombreUsuario);
                    sendListaUsuarios();
                    updateListaUsuarios();
                }
                if (out != null) {
                    salidas.remove(out);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
                broadcast("SERVIDOR: " + nombreUsuario + " ha salido del chat");
                appendMensajeToFrame("SERVIDOR: " + nombreUsuario + " ha salido del chat");
            }
        }

        private void broadcast(String message) {
            for (PrintWriter salida : salidas) {
                salida.println(message);
            }
        }

        private void sendListaUsuarios() {
            String userList = "Conectados: " + String.join(",", usuarios);
            for (PrintWriter salida : salidas) {
                salida.println(userList);
            }
        }

        private void updateListaUsuarios() {
            if (serverFrame != null) {
                SwingUtilities.invokeLater(() -> serverFrame.setUsuarios(new ArrayList<>(usuarios)));
            }
        }

        private void appendMensajeToFrame(String msg) {
            if (serverFrame != null) {
                SwingUtilities.invokeLater(() -> serverFrame.appendMensaje(msg));
            }
        }
    }

    // Interfaz gráfica del servidor
    static class ServerFrame extends JFrame {
        private DefaultListModel<String> ModeloListaUsuarios;
        private JTextArea messageArea;
        private JLabel portLabel;
        // Añadidos para login embebido
        private JTextField loginNombreUsuario;
        private JButton loginBotonConectar;

        public ServerFrame(int port) {
            setTitle("Servidor de Chat");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(700, 450);
            setLocationRelativeTo(null);

            // Colores y bordes personalizados
            Color fondo = new Color(245, 245, 245);
            Color panelColor = new Color(250, 250, 250);
            Color borderColor = new Color(200, 200, 200);

            // Panel superior con puerto y login
            JPanel topPanel = new JPanel(new BorderLayout(10, 0));
            topPanel.setBackground(fondo);
            topPanel.setBorder(BorderFactory.createEmptyBorder(18, 18, 12, 18));

            portLabel = new JLabel("   Puerto en uso: " + port);
            portLabel.setFont(portLabel.getFont().deriveFont( Font.BOLD, 13f));
            portLabel.setForeground(new Color(60, 60, 60));

            // Panel de login embebido
            JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            loginPanel.setOpaque(false);
            loginNombreUsuario = new JTextField(12);
            loginNombreUsuario.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor, 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            loginBotonConectar = new JButton("Conectar");
            loginBotonConectar.setBackground(new Color(0, 132, 255));
            loginBotonConectar.setForeground(Color.WHITE);
            loginBotonConectar.setFocusPainted(false);
            loginBotonConectar.setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
            loginPanel.add(new JLabel("           Nombre de usuario:"));
            loginPanel.add(loginNombreUsuario);
            loginPanel.add(loginBotonConectar);

            topPanel.add(portLabel, BorderLayout.WEST);
            topPanel.add(loginPanel, BorderLayout.CENTER);

            ModeloListaUsuarios = new DefaultListModel<>();
            JList<String> userList = new JList<>(ModeloListaUsuarios);
            userList.setBackground(panelColor);
            userList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            userList.setFont(userList.getFont().deriveFont(Font.PLAIN, 14f));
            JScrollPane userScroll = new JScrollPane(userList);
            userScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                "Usuarios conectados", 0, 0, userList.getFont().deriveFont(Font.BOLD, 13f)));
            userScroll.setBackground(panelColor);

            messageArea = new JTextArea();
            messageArea.setEditable(false);
            messageArea.setBackground(panelColor);
            messageArea.setFont(messageArea.getFont().deriveFont(Font.PLAIN, 14f));
            messageArea.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
            JScrollPane messageScroll = new JScrollPane(messageArea);
            messageScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(borderColor, 1, true),
                "Mensajes", 0, 0, messageArea.getFont().deriveFont(Font.BOLD, 13f)));
            messageScroll.setBackground(panelColor);

            // Panel izquierdo con padding y bordes redondeados
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setBackground(fondo);
            leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 18, 18, 10));
            leftPanel.add(userScroll, BorderLayout.CENTER);
            leftPanel.setPreferredSize(new Dimension(210, 0));

            // Panel central con padding y bordes redondeados
            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setBackground(fondo);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 18));
            centerPanel.add(messageScroll, BorderLayout.CENTER);

            setLayout(new BorderLayout());
            add(topPanel, BorderLayout.NORTH);
            add(leftPanel, BorderLayout.WEST);
            add(centerPanel, BorderLayout.CENTER);

            // Acción del botón conectar: abre ChatFrame con el nombre ingresado
            loginBotonConectar.addActionListener(e -> {
                String nombre = loginNombreUsuario.getText().trim();
                if (!nombre.isEmpty()) {
                    ChatFrame chat = new ChatFrame(nombre);
                    chat.setVisible(true);
                    loginNombreUsuario.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Por favor ingrese un nombre de usuario");
                }
            });
        }

        public void setUsuarios(List<String> usuarios) {
            ModeloListaUsuarios.clear();
            for (String usuario : usuarios) {
                ModeloListaUsuarios.addElement(usuario);
            }
        }

        public void appendMensaje(String msg) {
            messageArea.append(msg + "\n");
        }
    }
}