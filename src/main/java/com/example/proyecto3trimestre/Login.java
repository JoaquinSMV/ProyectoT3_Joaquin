package com.example.proyecto3trimestre;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;
import java.io.IOException;
import java.sql.*;

public class Login {

    @FXML
    private Button Conectarse;

    @FXML
    private Label contraseñaI;

    @FXML
    private TextField Username;

    @FXML
    private PasswordField password;

    @FXML
    public void logusuario(ActionEvent evento) {
     String user = Username.getText();
     String pass = password.getText();

     if(verificarUser(user, pass)) {
         try {
             Parent root = FXMLLoader.load(getClass().getResource("Cartelera.fxml"));
             Stage stage = (Stage) ((Node) evento.getSource()).getScene().getWindow();
             stage.setScene(new Scene(root, 650, 520));
             stage.show();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }

     }

     else {contraseñaI.setText("No tienes usuario , registrate...");}
    }

    private boolean verificarUser(String user, String pass) {
        String url = "jdbc:oracle:thin:@localhost:1521:XE";
        String dbuser = "info";
        String dbpass = "info";
        boolean isAuthenticated = false;

        try {
            Connection conn = DriverManager.getConnection(url, dbuser, dbpass);
            String sql = "SELECT * FROM USUARIOS WHERE NOMBRE = ? AND CONTRASEÑA = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            // Verificar si existe el usuario
            if (rs.next()) {
                isAuthenticated = true;
                // Obtener el idUsuario
                int idUsuario = rs.getInt("ID_USUARIO");
                // Guardar el idUsuario en la clase Sesion
                SESION.setIdUsuario(idUsuario);  // Aquí se guarda el ID_USUARIO
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return isAuthenticated;
    }




    // Método para cambiar de escena a registro
    @FXML
    public void cambiarEscena(ActionEvent event) {
        try {
            // Cargar el archivo FXML de registro
            Parent root = FXMLLoader.load(getClass().getResource("registro.fxml"));
            Stage stage = (Stage) Conectarse.getScene().getWindow();  // Obtener la ventana actual
            stage.setScene(new Scene(root, 700, 500));  // Cambiar la escena
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
