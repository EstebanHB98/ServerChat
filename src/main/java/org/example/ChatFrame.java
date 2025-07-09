package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatFrame extends JFrame {
    private ChatBubblePanel chatPanel;
    private JTextField MensajeField;
    private PrintWriter writer;
    private String nombreUsuario;

    public ChatFrame(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
        setTitle("Chat - " + nombreUsuario);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));


        chatPanel = new ChatBubblePanel();
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(230, 221, 212));

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBackground(new Color(245, 245, 245));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        MensajeField = new JTextField();
        MensajeField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));

        JButton botonEnviar = new JButton("Enviar");
        botonEnviar.setBackground(new Color(0, 132, 255));
        botonEnviar.setForeground(Color.WHITE);
        botonEnviar.setFocusPainted(false);
        botonEnviar.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        bottomPanel.add(MensajeField, BorderLayout.CENTER);
        bottomPanel.add(botonEnviar, BorderLayout.EAST);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);

        ConectarServidor();

        botonEnviar.addActionListener(e -> enviarMensaje());
        MensajeField.addActionListener(e -> enviarMensaje());
    }

    private void ConectarServidor() {
        try {
            Socket socket = new Socket("localhost", 5050);
            writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(nombreUsuario);

            new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    String message;
                    while ((message = reader.readLine()) != null) {
                        String finalMessage = message;
                        SwingUtilities.invokeLater(() -> {
                            boolean enviadoPorMi = finalMessage.startsWith(nombreUsuario + ":");
                            chatPanel.addBubble(finalMessage, enviadoPorMi);
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enviarMensaje() {
        String mensaje = MensajeField.getText().trim();
        if (!mensaje.isEmpty()) {
            // Agregar hora al mensaje enviado por el usuario
            String hora = new SimpleDateFormat("HH:mm").format(new Date());
            writer.println(mensaje + "||" + hora); // Usamos "||" como separador para poder extraer la hora
            MensajeField.setText("");
        }
    }

    // Panel personalizado para mostrar burbujas de chat usando Graphics
    static class ChatBubblePanel extends JPanel implements Scrollable {
        private static class Bubble {
            String mensaje;
            String hora;
            boolean enviadoPorMi;
            int tipo; // 0=mensaje normal, 1=mensaje servidor, 2=lista usuarios
            String nombreUsuario;

            Bubble(String mensajeCompleto, boolean enviadoPorMi) {
                this.enviadoPorMi = enviadoPorMi;

                // Extraer la hora si el mensaje tiene el formato "mensaje||hora"
                if (mensajeCompleto.contains("||")) {
                    String[] partes = mensajeCompleto.split("\\|\\|");
                    this.mensaje = partes[0];
                    this.hora = partes[1];
                } else {
                    this.mensaje = mensajeCompleto;
                    this.hora = "";
                }

                // Determinar el tipo de mensaje
                if (this.mensaje.startsWith("SERVIDOR:")) {
                    this.tipo = 1; // Mensaje del servidor
                } else if (this.mensaje.startsWith("Conectados:")) {
                    this.tipo = 2; // Lista de usuarios conectados
                } else {
                    this.tipo = 0; // Mensaje normal
                }

                // Extraer nombre de usuario si es mensaje normal
                if (this.tipo == 0 && this.mensaje.contains(":")) {
                    int idx = this.mensaje.indexOf(":");
                    this.nombreUsuario = this.mensaje.substring(0, idx).trim();
                    this.mensaje = this.mensaje.substring(idx + 1).trim();
                } else {
                    this.nombreUsuario = "";
                }
            }
        }

        private final List<Bubble> bubbles = new ArrayList<>();

        public ChatBubblePanel() {
            // Establecer el color de fondo del panel
            setBackground(new Color(230, 221, 212));
        }

        @Override
        public Dimension getPreferredSize() {
            int width = 400;
            Container parent = getParent();
            if (parent instanceof JViewport) {
                width = parent.getWidth();
            }
            int height = 10;

            // Crear una imagen temporal para medir texto
            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();

            // Configurar las fuentes
            Font fontNormal = getFont().deriveFont(Font.PLAIN, 14f);
            Font fontServidor = getFont().deriveFont(Font.ITALIC, 12f);
            Font fontHora = getFont().deriveFont(Font.ITALIC, 10f);
            Font fontNombre = getFont().deriveFont(Font.BOLD, 12f);

            FontMetrics fmNormal = g2.getFontMetrics(fontNormal);
            FontMetrics fmServidor = g2.getFontMetrics(fontServidor);
            FontMetrics fmHora = g2.getFontMetrics(fontHora);
            FontMetrics fmNombre = g2.getFontMetrics(fontNombre);

            // Calcular altura total basada en el contenido
            for (Bubble b : bubbles) {
                if (b.tipo > 0) { // Mensaje del servidor o lista de usuarios
                    height += fmServidor.getHeight() + 15; // Más espacio para mensajes del sistema
                } else { // Mensaje normal con burbuja
                    int bubbleHeight = fmNormal.getHeight() + 5; // Mensaje
                    if (!b.hora.isEmpty()) {
                        bubbleHeight += fmHora.getHeight() + 5; // Altura extra para la hora
                    }
                    // Añadir altura para el nombre de usuario
                    bubbleHeight += fmNormal.getHeight();
                    height += bubbleHeight + 25; // Altura para burbuja
                }
            }

            g2.dispose();
            return new Dimension(width, height);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return new Dimension(400, 300);
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 50;
        }

        public void addBubble(String mensaje, boolean enviadoPorMi) {
            bubbles.add(new Bubble(mensaje, enviadoPorMi));
            revalidate();
            repaint();
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
            if (scrollPane != null) {
                SwingUtilities.invokeLater(() -> {
                    scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
                });
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int y = 10;
            int width = getWidth();

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Definir fuentes
            Font fontNormal = getFont().deriveFont(Font.PLAIN, 14f);
            Font fontServidor = getFont().deriveFont(Font.ITALIC, 12f);
            Font fontHora = getFont().deriveFont(Font.ITALIC, 10f);
            Font fontNombre = getFont().deriveFont(Font.BOLD, 12f);

            FontMetrics fmNormal = g2.getFontMetrics(fontNormal);
            FontMetrics fmServidor = g2.getFontMetrics(fontServidor);
            FontMetrics fmHora = g2.getFontMetrics(fontHora);
            FontMetrics fmNombre = g2.getFontMetrics(fontNombre);

            for (Bubble b : bubbles) {
                String mensaje = b.mensaje;

                // CASO 1 y 2: Mensajes del servidor o lista de usuarios conectados
                if (b.tipo > 0) {
                    g2.setFont(fontServidor);
                    g2.setColor(new Color(100, 100, 100)); // Gris más oscuro

                    // Centrar el texto
                    int textWidth = fmServidor.stringWidth(mensaje);
                    int x = (width - textWidth) / 2;

                    // Dibujar con fondo semitransparente para mensajes del sistema
                    Color bgColor = b.tipo == 1 ?
                            new Color(240, 240, 255, 100) : // Azul claro para servidor
                            new Color(240, 255, 240, 100);  // Verde claro para usuarios

                    int padding = 8;
                    g2.setColor(bgColor);
                    g2.fillRect(x - padding, y, textWidth + padding * 2, fmServidor.getHeight() + padding);

                    // Dibujar el texto
                    g2.setColor(new Color(80, 80, 80));
                    g2.drawString(mensaje, x, y + fmServidor.getAscent() + padding / 2);

                    // Actualizar posición Y para el siguiente mensaje
                    y += fmServidor.getHeight() + 15;
                }
                // CASO 0: Mensajes normales con burbuja
                else {
                    boolean enviadoPorMi = b.enviadoPorMi;
                    g2.setFont(fontNormal);

                    // Calcular dimensiones de la burbuja
                    int messageWidth = fmNormal.stringWidth(mensaje);
                    int horaWidth = 0;
                    if (!b.hora.isEmpty()) {
                        horaWidth = fmHora.stringWidth(b.hora);
                    }
                    int nombreWidth = 0;
                    if (!enviadoPorMi && b.nombreUsuario != null) {
                        nombreWidth = fmNombre.stringWidth(b.nombreUsuario);
                    } else if (enviadoPorMi) {
                        nombreWidth = fmNombre.stringWidth("Yo");
                    }

                    // Calcular el ancho total de la burbuja (el mayor entre mensaje y hora)
                    int textWidth = Math.max(Math.max(messageWidth, horaWidth), nombreWidth);
                    int bubbleWidth = Math.min(textWidth + 30, width - 60);

                    // Calcular altura de la burbuja
                    int bubbleHeight = fmNormal.getHeight() + 10; // Altura base para el mensaje
                    if (!b.hora.isEmpty()) {
                        bubbleHeight += fmHora.getHeight() + 5; // Agregar espacio para la hora
                    }
                    bubbleHeight += fmNombre.getHeight(); // Espacio para el nombre

                    // Posición X según quién envió el mensaje
                    int x = enviadoPorMi ? width - bubbleWidth - 20 : 20;

                    // Colores según quién envió el mensaje
                    Color bubbleColor = enviadoPorMi ? new Color(220, 248, 198) : Color.WHITE;
                    Color borderColor = enviadoPorMi ? new Color(180, 230, 160) : new Color(200, 200, 200);

                    // Dibujar burbuja
                    g2.setColor(bubbleColor);
                    g2.fillRoundRect(x, y, bubbleWidth, bubbleHeight, 20, 20);

                    g2.setColor(borderColor);
                    g2.drawRoundRect(x, y, bubbleWidth, bubbleHeight, 20, 20);

                    // Dibujar nombre de usuario
                    g2.setFont(fontNombre);
                    g2.setColor(new Color(80, 80, 80));
                    if (enviadoPorMi) {
                        String yo = "Yo";
                        int yoWidth = fmNombre.stringWidth(yo);
                        g2.drawString(yo, x + bubbleWidth - yoWidth - 10, y + fmNombre.getAscent() + 2);
                    } else if (b.nombreUsuario != null && !b.nombreUsuario.isEmpty()) {
                        g2.drawString(b.nombreUsuario, x + 10, y + fmNombre.getAscent() + 2);
                    }

                    // Dibujar mensaje
                    g2.setFont(fontNormal);
                    g2.setColor(new Color(33, 33, 33));
                    int mensajeY = y + fmNombre.getHeight() + fmNormal.getAscent() + 5;
                    g2.drawString(mensaje, x + 15, mensajeY);

                    // Dibujar hora si existe
                    if (!b.hora.isEmpty()) {
                        g2.setFont(fontHora);
                        g2.setColor(new Color(120, 120, 120)); // Color gris para la hora
                        int horaX = x + bubbleWidth - horaWidth - 10; // Alineado a la derecha
                        int horaY = mensajeY + fmHora.getHeight() + 2;
                        g2.drawString(b.hora, horaX, horaY);
                    }

                    // Actualizar posición Y para el siguiente mensaje
                    y += bubbleHeight + 10;
                }
            }
        }
    }
}