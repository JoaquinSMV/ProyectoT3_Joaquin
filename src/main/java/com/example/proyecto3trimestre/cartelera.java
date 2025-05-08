package com.example.proyecto3trimestre;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.*;
import java.text.SimpleDateFormat;

public class cartelera {

    @FXML
    private TextField busca;

    @FXML
    private Button Volver;

    @FXML
    private HBox contenedorPeliculas;

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER = "info";
    private static final String DB_PASS = "info";

    // Se asigna desde la escena anterior (login, por ejemplo)
    private int idUsuario;

    /** Llamar desde el controlador anterior: controller.setIdUsuario(idDelUsuarioLogueado); */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    @FXML
    public void initialize() {
        cargarcartelera();
    }

    private void cargarcartelera() {
        contenedorPeliculas.getChildren().clear();
        String sql = "SELECT * FROM ESPECTACULOS";

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Leer correctamente el ID del espectáculo
                int idEspectaculo = rs.getInt("ID_ESPECTACULO");
                String nombre = rs.getString("NOMBRE");
                Date fechaEspectaculo = rs.getDate("FECHA");
                double precioBase = rs.getDouble("PRECIO_BASE");
                double precioVip = rs.getDouble("PRECIO_VIP");

                ImageView img = new ImageView();
                try {
                    Image image = new Image(
                            getClass().getResourceAsStream("/Images/" + nombre.toUpperCase() + ".jpg")
                    );
                    img.setImage(image);
                    img.setFitHeight(192);
                    img.setFitWidth(181);
                    img.setPreserveRatio(true);
                } catch (Exception ex) {
                    System.out.println("Imagen no encontrada para: " + nombre);
                }

                Text titulo = new Text(nombre);
                Text precio = new Text("Base: " + precioBase + " €");
                Text precioV = new Text("Vip: "   + precioVip  + " €");
                Text fecha = new Text(new SimpleDateFormat("yyyy-MM-dd")
                        .format(fechaEspectaculo));

                Button btnAñadir = new Button("Añadir");
                btnAñadir.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("Cine.fxml")
                        );
                        Parent root = loader.load();

                        Cine controlador = loader.getController();
                        controlador.cargartitulo(nombre);
                        controlador.cargafecha(
                                new SimpleDateFormat("yyyy-MM-dd")
                                        .format(fechaEspectaculo)
                        );
                        // Pasamos el usuario y el espectáculo
                        controlador.setUsuarioYEspectaculo(idUsuario, idEspectaculo);
                        controlador.crearButacasDesdeBD();


                        Stage stage = (Stage)((Button)e.getSource())
                                .getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error al cambiar de escena");
                        alert.setHeaderText(null);
                        alert.setContentText(
                                "No se pudo cargar la pantalla de Cine."
                        );
                        alert.showAndWait();
                    }
                });

                VBox card = new VBox(10, img, titulo, precio, precioV, fecha, btnAñadir);
                card.setStyle(
                        "-fx-background-color: #eeeeee; " +
                                "-fx-padding: 10; " +
                                "-fx-border-color: black;"
                );
                card.setPrefWidth(200);

                contenedorPeliculas.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Error al cargar cartelera:\n" + e.getMessage()
            ).showAndWait();
        }
    }

    @FXML
    public void buscar(ActionEvent event) {
        contenedorPeliculas.getChildren().clear();
        String tituloBusqueda = busca.getText().trim();
        String sql = "SELECT * FROM ESPECTACULOS WHERE NOMBRE LIKE ?";

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS);
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, "%" + tituloBusqueda + "%");
            ResultSet rs = pst.executeQuery();

            boolean encontrado = false;
            while (rs.next()) {
                encontrado = true;
                int idEspectaculo = rs.getInt("ID_ESPECTACULO");
                String nombre = rs.getString("NOMBRE");
                Date fechaEspectaculo = rs.getDate("FECHA");
                double precioBase = rs.getDouble("PRECIO_BASE");
                double precioVip = rs.getDouble("PRECIO_VIP");

                ImageView img = new ImageView();
                try {
                    Image image = new Image(
                            getClass().getResourceAsStream("/Images/" + nombre.toUpperCase() + ".jpg")
                    );
                    img.setImage(image);
                    img.setFitHeight(192);
                    img.setFitWidth(181);
                    img.setPreserveRatio(true);
                } catch (Exception ex) {
                    System.out.println("Imagen no encontrada para: " + nombre);
                }

                Text tituloText = new Text(nombre);
                Text precio = new Text("Base: " + precioBase + " €");
                Text vip = new Text("Vip: " + precioVip + " €");
                Text fechaText = new Text(new SimpleDateFormat("yyyy-MM-dd")
                        .format(fechaEspectaculo));

                Button btnAñadir = new Button("Añadir");
                btnAñadir.setOnAction(e -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("Cine.fxml")
                        );
                        Parent root = loader.load();

                        Cine controlador = loader.getController();
                        controlador.cargartitulo(nombre);
                        controlador.cargafecha(
                                new SimpleDateFormat("yyyy-MM-dd")
                                        .format(fechaEspectaculo)
                        );
                        controlador.setUsuarioYEspectaculo(idUsuario, idEspectaculo);

                        Stage stage = (Stage)((Button)e.getSource())
                                .getScene().getWindow();
                        stage.setScene(new Scene(root));
                        stage.show();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        new Alert(Alert.AlertType.ERROR,
                                "No se pudo cargar la pantalla de Cine."
                        ).showAndWait();
                    }
                });

                VBox card = new VBox(10, img, tituloText, precio, vip, fechaText, btnAñadir);
                card.setStyle(
                        "-fx-background-color: #eeeeee; " +
                                "-fx-padding: 10; " +
                                "-fx-border-color: black;"
                );
                card.setPrefWidth(200);

                contenedorPeliculas.getChildren().add(card);
            }

            if (!encontrado) {
                new Alert(Alert.AlertType.INFORMATION,
                        "No se encontró ningún espectáculo con ese título."
                ).showAndWait();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                    "Error en la búsqueda:\n" + e.getMessage()
            ).showAndWait();
        }
    }
    public void cambiarEscena(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
            Stage stage = (Stage) Volver.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
