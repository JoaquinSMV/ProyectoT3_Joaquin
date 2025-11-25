package com.example.proyecto3trimestre;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.sql.*;

public class Registrarse {

    @FXML
    private TextField usuario; // Campo para el nombre de usuario

    @FXML
    private PasswordField contraseña; // Campo para la contraseña

    @FXML
    private TextField gmail; // TextField para mostrar mensajes de error o confirmación

    @FXML
    private Label texto; // Para los mensajitos

    // Método para registrar el usuario
    @FXML
    public void registrarUsuario(ActionEvent event) {
        String nombreUsuario = this.usuario.getText();  // Obtener el nombre de usuario del TextField
        String pass = this.contraseña.getText();  // Obtener la contraseña del PasswordField
        String email = this.gmail.getText();

        // Verificación de que los campos no estén vacíos
        if (nombreUsuario.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            mostrarAlerta(AlertType.WARNING, "Campos vacíos", "Por favor, completa todos los campos.");
            return;
        }

        // URL de la base de datos (ajusta esta configuración según tu base de datos)
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String dbUsuario = "info";
        String dbpass = "info";

        // SQL para insertar el nuevo usuario
        String sql = "INSERT INTO usuarios (nombre, contraseña, email) VALUES (?, ?, ?)";

        // Conexión a la base de datos y manejo de excepciones
        try (Connection con = DriverManager.getConnection(url, dbUsuario, dbpass);
             PreparedStatement pst = con.prepareStatement(sql)) {

            // Establecer los valores de los parámetros de la consulta
            pst.setString(1, nombreUsuario);  // Establecer el nombre de usuario
            pst.setString(2, pass);  // Establecer la contraseña
            pst.setString(3, email); //Establecer email

            // Ejecutar la consulta
            int filasAfectadas = pst.executeUpdate();

            if (filasAfectadas > 0) {
                texto.setText("Buenas " + nombreUsuario + " , ya puede volver... ");
            } else {
                texto.setText("Error al registrar el usuario");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            texto.setText("Usuario ya existente o error en la base de datos.");
        }
    }

    // Método para volver a la pantalla anterior (login)
    @FXML
    public void paAtras(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(AlertType.ERROR, "Error de navegación", "No se pudo cargar la pantalla de inicio.");
        }
    }

    // Método para mostrar alertas
    private void mostrarAlerta(AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }




}
