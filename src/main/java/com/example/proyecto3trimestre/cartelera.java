package com.example.proyecto3trimestre;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class cartelera {

    @FXML
    private ImageView IMGP; // Imagen de la película

    @FXML
    private Text Titulo; // Título de la película

    @FXML
    private Text money; // Precio de la película

    @FXML
    private Text moneyVip; // Precio vip

    @FXML
    private Text fecha; // Fecha de estreno

    @FXML
    private TextField busca; // Campo para buscar películas

    @FXML
    private Button butacas; // Botón de cambio escenario

    @FXML
    private Label avisos; // Etiqueta para avisos o mensajes

    @FXML
    private HBox contenedorPeliculas;

    //Para estar conectados a la base de datos...

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER = "info";
    private static final String DB_PASS = "info";



    private void cargarcartelera () {

            contenedorPeliculas.getChildren().clear(); // Limpiar las pelis anteriores

            String sql = "SELECT * FROM ESPECTACULOS";

            try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS);
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                while (rs.next()) {
                    String nombre = rs.getString("NOMBRE");
                    Date fechaEspectaculo = rs.getDate("FECHA");
                    double precioBase = rs.getDouble("PRECIO_BASE");
                    double moneyVip = rs.getDouble("PRECIO_VIP");

                    // Crear los nodos nuevos
                    ImageView img = new ImageView();
                    try {
                        Image image = new Image(getClass().getResourceAsStream("/Images/" + nombre.toUpperCase() + ".jpg"));
                        img.setImage(image);
                        img.setFitHeight(192);
                        img.setFitWidth(181);
                        img.setPreserveRatio(true);
                    } catch (Exception ex) {
                        System.out.println("Imagen no encontrada para: " + nombre);
                    }

                    Text titulo = new Text(nombre);
                    Text precio = new Text("Base: " + precioBase + " €");
                    Text precioV = new Text("Vip: " + moneyVip + " €");
                    Text fecha = new Text(new SimpleDateFormat("yyyy-MM-dd").format(fechaEspectaculo));
                   //
                    Button btnAñadir = new Button("Añadir");

                    btnAñadir.setOnAction(e -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("Cine.fxml"));
                            Parent root = loader.load();

                            Cine controlador = loader.getController(); // usa Cine, no CineController
                            controlador.cargartitulo(nombre); // pasa el nombre que quieras
                            controlador.cargafecha(new SimpleDateFormat("yyyy-MM-dd").format(fechaEspectaculo));

                            Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Error al cambiar de escena");
                            alert.setHeaderText(null);
                            alert.setContentText("No se pudo cargar la pantalla de Cine.");
                            alert.showAndWait();
                        }
                    });




                    //
                    // Estilos y posiciones si hace falta (o usa VBox para ordenarlos verticalmente)
                    VBox card = new VBox(10, img, titulo, precio, precioV, fecha, btnAñadir);
                    card.setStyle("-fx-background-color: #eeeeee; -fx-padding: 10; -fx-border-color: black;");
                    card.setPrefWidth(200);

                    contenedorPeliculas.getChildren().add(card);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }





    public void buscar(ActionEvent event) throws SQLException {
        // Limpiar la cartelera antes de cargar los resultados de la búsqueda
        contenedorPeliculas.getChildren().clear();

        String titulo = busca.getText().trim(); // Obtener el texto de búsqueda

        String sql = "SELECT * FROM ESPECTACULOS WHERE NOMBRE LIKE ?"; // Utilizamos LIKE para que busque coincidencias parciales

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS);
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, "%" + titulo + "%"); // Usamos LIKE para que busque coincidencias parciales
            ResultSet rs = pst.executeQuery();

            boolean found = false; // Para verificar si encontramos alguna película

            while (rs.next()) {
                found = true;

                String nombre = rs.getString("NOMBRE");
                Date fechaEspectaculo = rs.getDate("FECHA");
                double precioBase = rs.getDouble("PRECIO_BASE");
                double precioVip = rs.getDouble("PRECIO_VIP");

                // Crear los nodos para mostrar la película
                ImageView img = new ImageView();
                try {
                    Image image = new Image(getClass().getResourceAsStream("/Images/" + nombre.toUpperCase() + ".jpg"));
                    img.setImage(image);
                    img.setFitHeight(192);
                    img.setFitWidth(181);
                    img.setPreserveRatio(true);
                } catch (Exception ex) {
                    System.out.println("Imagen no encontrada para: " + nombre);
                }

                Text tituloText = new Text(nombre);
                Text precio = new Text("Precio normal: " + precioBase + " €");
                Text vip = new Text("Precio VIP: " + precioVip + " €");
                Text fechaText = new Text(new SimpleDateFormat("yyyy-MM-dd").format(fechaEspectaculo));

                // Crear el card de la película
                VBox card = new VBox(10, img, tituloText, precio, vip, fechaText);
                card.setStyle("-fx-background-color: #eeeeee; -fx-padding: 10; -fx-border-color: black;");
                card.setPrefWidth(200);

                contenedorPeliculas.getChildren().add(card);
            }

            // Si no encontramos ninguna película con ese nombre, mostrar un mensaje
            if (!found) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sin resultados");
                alert.setHeaderText(null);
                alert.setContentText("No se encontró ningún espectáculo con ese título.");
                alert.showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo conectar a la base de datos");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void initialize() {
       cargarcartelera();
    }


}

