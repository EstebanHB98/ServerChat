package org.example;

import javax.swing.*;
import java.awt.*;

/*
Esta clase define la ventana de inicio de sesión (login) para el cliente del chat.

- Hereda de JFrame, por lo que es una ventana gráfica.
- Declara dos componentes principales:
    - nombreUsuarioField: un campo de texto donde el usuario escribe su nombre.
    - botonIngresar: un botón para intentar conectarse.

En el constructor:
1. Configura la ventana (título, tamaño, cierre, posición).
2. Crea un panel con un layout de cuadrícula para organizar los componentes.
3. Añade una etiqueta ("Nombre de usuario:"), el campo de texto y el botón al panel.
4. Añade el panel a la ventana principal.
5. Asocia una acción al botón:
   - Cuando se pulsa, toma el texto del campo.
   - Si no está vacío, cierra la ventana de login y abre la ventana principal del chat (`ChatFrame`) con el nombre ingresado.
   - Si está vacío, muestra un mensaje de advertencia.

En resumen: 
Esta clase muestra una ventana donde el usuario debe ingresar su nombre antes de acceder al chat. Valida que el campo no esté vacío y, si es correcto, abre la ventana del chat.
*/
public class LoginFrame extends JFrame {
    private JTextField nombreUsuario;
    private JButton botonIngresar;

    public LoginFrame() {
        setTitle("Chat Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel NombreLabel = new JLabel("Nombre de usuario:");
        nombreUsuario = new JTextField(20);
        botonIngresar = new JButton("Conectar");

        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(NombreLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(nombreUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(botonIngresar, gbc);

        add(panel);

        botonIngresar.addActionListener(e -> {
            String nombre = nombreUsuario.getText().trim();
            if (!nombre.isEmpty()) {
                ChatFrame chat = new ChatFrame(nombre);
                chat.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Por favor ingrese un nombre de usuario");
            }
        });
    }
}