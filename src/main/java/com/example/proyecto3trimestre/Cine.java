package com.example.proyecto3trimestre;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Cine implements Initializable {

    @FXML
    private Label Titulo;

    @FXML
    private Label Fecha;

    @FXML
    private Button Volver;

    @FXML
    private Button Comprar;

    @FXML
    private GridPane gridButacas;

    @FXML
    private int idUsuario;
    @FXML
    private int idEspectaculo;

    private ArrayList<Integer> butacasSeleccionadas = new ArrayList<>();

    private ArrayList<Button> listaButacas = new ArrayList<>();

    // Datos conexión ORACLE
    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER = "info";
    private static final String DB_PASS = "info";

    public void setUsuarioYEspectaculo(int idUsuario, int idEspectaculo) {
        this.idUsuario = idUsuario;
        this.idEspectaculo = idEspectaculo;
    }

    public void cargartitulo(String nombre) {
        Titulo.setText(nombre);
    }

    public void cargafecha(String fecha) {
        Fecha.setText(fecha);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        crearButacasDesdeBD();
    }

    private void crearButacasDesdeBD() {
        int filas = 6;
        int columnas = 6;

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_butaca, tipo FROM butacas ORDER BY id_butaca ASC")) {

            int fila = 0;
            int columna = 0;

            while (rs.next()) {
                int idBD = rs.getInt("id_butaca");
                String tipo = rs.getString("tipo");

                String idFXML = "" + (char) ('A' + fila) + (columna + 1);
                Button butaca = new Button();
                butaca.setPrefSize(60, 60);
                butaca.setId(idFXML);
                butaca.setText(idFXML); // Mostrar A1, A2, etc.

                //Estilo del boton :)
                butaca.setStyle("-fx-background-color: transparent;");
                butaca.setText("");


                // Imagen según tipo
                String imagen = tipo.equalsIgnoreCase("VIP") ? "Butaca_Vip.png" : "Butaca_Verde.png";
                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/" + imagen)));
                imageView.setFitWidth(45);
                imageView.setFitHeight(45);
                butaca.setGraphic(imageView);

                // Acción al hacer clic
               int finalIdBD = idBD; // Necesario para usarlo en la lambda
                butaca.setOnAction(e -> {
                    if (butacasSeleccionadas.contains(finalIdBD)) {
                        butacasSeleccionadas.remove((Integer) finalIdBD); // Si ya está, se desmarca
                        butaca.setStyle("-fx-background-color: transparent;");
                    } else {
                        butacasSeleccionadas.add(finalIdBD); // Se selecciona
                        butaca.setStyle("-fx-background-color: yellow;"); // Color para indicar selección
                    }

                    System.out.println("Butacas seleccionadas: " + butacasSeleccionadas);
                });


                listaButacas.add(butaca);
                gridButacas.add(butaca, columna, fila);

                columna++;
                if (columna == columnas) {
                    columna = 0;
                    fila++;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public void Comprar (ActionEvent event) {
        if (butacasSeleccionadas.isEmpty()) {
            System.out.println("No has seleccionado ninguna butaca.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS)) {
            String insertSQL = "INSERT INTO reservas (id_reserva, id_espectaculo, id_butaca, estado, id_usuario) " +
                    "VALUES (reservas_seq.NEXTVAL, ?, ?, 'reservada', ?)";

            PreparedStatement ps = conn.prepareStatement(insertSQL);

            for (int idButaca : butacasSeleccionadas) {
                ps.setInt(1, idEspectaculo);
                ps.setInt(2, idButaca);
                ps.setInt(3, idUsuario);
                ps.addBatch();
            }

            ps.executeBatch();

            System.out.println("Reserva completada con éxito.");
            butacasSeleccionadas.clear();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void cambiarEscena(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Cartelera.fxml"));
            Stage stage = (Stage) Volver.getScene().getWindow();
            stage.setScene(new Scene(root, 650, 520));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
