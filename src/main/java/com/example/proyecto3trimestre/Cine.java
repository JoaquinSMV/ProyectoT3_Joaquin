package com.example.proyecto3trimestre;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

public class Cine  {

    @FXML
    private Label Titulo;

    @FXML
    private Label Fecha;

    @FXML
    private Label IDComprados;

    @FXML
    private Button Volver;

    @FXML
    private GridPane gridButacas;

    @FXML
    private int idUsuario;
    @FXML
    private int idEspectaculo;

    private ArrayList<Integer> butacasSeleccionadas = new ArrayList<>();
    private ArrayList<Button> listaButacas = new ArrayList<>();

    private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
    private static final String DB_USER = "info";
    private static final String DB_PASS = "info";



    public void setUsuarioYEspectaculo(int idUsuario, int idEspectaculo) {
        this.idUsuario = SESION.getIdUsuario();
        this.idEspectaculo = idEspectaculo;
    }

    //Para que se cargue titulo de la pelicula
    public void cargartitulo(String nombre) {
        Titulo.setText(nombre);
    }

    //Para que se cargue fecha de la pelicula
    public void cargafecha(String fecha) {
        Fecha.setText(fecha);
    }






    //Esto crea de manera automatica butacas y les pone sus imagenes y estilos
    public void crearButacasDesdeBD() {
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
                butaca.setText("");
                butaca.setStyle("-fx-background-color: transparent;");

                // Comprobar si está reservada
                boolean ocupada = false;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COUNT(*) FROM reservas WHERE id_butaca = ? AND id_espectaculo = ?")) {
                    ps.setInt(1, idBD);
                    ps.setInt(2, idEspectaculo);
                    ResultSet rsReserva = ps.executeQuery();
                    if (rsReserva.next() && rsReserva.getInt(1) > 0) {
                        ocupada = true;
                    }
                }

                String imagen;
                if (ocupada) {
                    imagen = "Butaca_Ocupada.png";
                    butaca.setDisable(true);
                } else {
                    imagen = tipo.equalsIgnoreCase("VIP") ? "Butaca_Vip.png" : "Butaca_Verde.png";
                }

                ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/images/" + imagen)));
                imageView.setFitWidth(45);
                imageView.setFitHeight(45);
                butaca.setGraphic(imageView);

                int finalIdBD = idBD;
                butaca.setOnAction(e -> {
                    if (butacasSeleccionadas.contains(finalIdBD)) {
                        butacasSeleccionadas.remove((Integer) finalIdBD);
                        butaca.setStyle("-fx-background-color: transparent;");
                    } else {
                        if (butacasSeleccionadas.size() >= 4) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Límite alcanzado");
                            alert.setHeaderText(null);
                            alert.setContentText("No puedes seleccionar más de 4 butacas.");
                            alert.showAndWait();
                            return;
                        }
                        butacasSeleccionadas.add(finalIdBD);
                        butaca.setStyle("-fx-background-color: yellow;");
                    }
                    System.out.println("Butacas seleccionadas: " + butacasSeleccionadas);
                    calcularPrecioTotal();
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

    //Boton comprar para que una vez seleccionadas las butacas a comprar se compren por decirlo asi...
    public void Comprar(ActionEvent event) {
        if (butacasSeleccionadas.isEmpty()) {
            calcularPrecioTotal();
            System.out.println("No has seleccionado ninguna butaca.");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS)) {
            String insertSQL = "INSERT INTO reservas (id_reserva, id_espectaculo, id_butaca, estado, id_usuario) VALUES (reservas_seq.NEXTVAL, ?, ?, 'reservada', ?)";
            PreparedStatement ps = conn.prepareStatement(insertSQL);

            ArrayList<Integer> reservadasAhora = new ArrayList<>();

            for (int idButaca : butacasSeleccionadas) {
                String consulta = "SELECT COUNT(*) FROM reservas WHERE id_butaca = ? AND id_espectaculo = ?";
                PreparedStatement check = conn.prepareStatement(consulta);
                check.setInt(1, idButaca);
                check.setInt(2, idEspectaculo);
                ResultSet rs = check.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("La butaca " + idButaca + " ya está reservada.");
                    continue;
                }

                ps.setInt(1, idEspectaculo);
                ps.setInt(2, idButaca);
                ps.setInt(3, idUsuario);
                ps.addBatch();
                reservadasAhora.add(idButaca);
            }

            ps.executeBatch();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Compra realizada");
            alert.setHeaderText(null); // Esto quita el encabezado con la "X"
            alert.setContentText("Reserva completada con éxito");
            alert.showAndWait();
            actualizarButacasOcupadas(reservadasAhora);
            butacasSeleccionadas.clear();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Como el nombre indica actualiza las butacas de manera automatica , para cuando una vez se pulse y le den al boton comprar
    //con un array list y una busqueda del boton pulsado se envie que esta ocupado y se actualice la imagen y mas aparte
    //se transparente mas que no se pueda seleccionar de nuevo ...
    private void actualizarButacasOcupadas(ArrayList<Integer> butacasReservadas) {
        for (Button butaca : listaButacas) {
            String idBoton = butaca.getId();
            for (int idSeleccionada : butacasReservadas) {
                if (idBoton.equals(getNombreButaca(idSeleccionada))) {
                    ImageView nuevaImagen = new ImageView(new Image(getClass().getResourceAsStream("/images/Butaca_Ocupada.png")));
                    nuevaImagen.setFitWidth(45);
                    nuevaImagen.setFitHeight(45);
                    butaca.setGraphic(nuevaImagen);
                    butaca.setDisable(true);
                    butaca.setStyle("-fx-background-color: transparent;");
                }
            }
        }
    }

    //Con esto sabremos en que butaca se encuentra por decirlo asi el usuario
    private String getNombreButaca(int idButaca) {
        int fila = (idButaca - 1) / 6;
        int columna = (idButaca - 1) % 6 + 1;
        return "" + (char) ('A' + fila) + columna;
    }

    public void calcularPrecioTotal() {
        if (butacasSeleccionadas.isEmpty()) {
            IDComprados.setText("Seleccione butacas");
            return;
        }

        double precioBase = 0;
        double precioVIP = 0;
        double total = 0;

        try (Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASS)) {
            // Obtener precios del espectáculo
            String sqlPrecios = "SELECT precio_base, precio_vip FROM espectaculos WHERE id_espectaculo = ?";
            try (PreparedStatement psPrecios = conn.prepareStatement(sqlPrecios)) {
                psPrecios.setInt(1, idEspectaculo);
                try (ResultSet rsPrecios = psPrecios.executeQuery()) {
                    if (rsPrecios.next()) {
                        precioBase = rsPrecios.getDouble("precio_base");
                        precioVIP = rsPrecios.getDouble("precio_vip");
                    }
                }
            }

            // Calcular total
            String sqlTipo = "SELECT tipo FROM butacas WHERE id_butaca = ?";
            try (PreparedStatement psTipo = conn.prepareStatement(sqlTipo)) {
                for (int idButaca : butacasSeleccionadas) {
                    psTipo.setInt(1, idButaca);
                    try (ResultSet rsTipo = psTipo.executeQuery()) {
                        if (rsTipo.next()) {
                            String tipo = rsTipo.getString("tipo");
                            total += tipo.equalsIgnoreCase("VIP") ? precioVIP : precioBase;
                        }
                    }
                    psTipo.clearParameters();
                }
            }

            // Mostrar resultado formateado
            IDComprados.setText(String.format("Total: %.2f € (Butacas: %d)", total, butacasSeleccionadas.size()));

        } catch (SQLException e) {
            e.printStackTrace();
            IDComprados.setText("Error al calcular precio");
        }
    }


    public void cambiarEscena(ActionEvent event) {
        try {
            // Cargar el archivo FXML de registro
            Parent root = FXMLLoader.load(getClass().getResource("Cartelera.fxml"));
            Stage stage = (Stage) Volver.getScene().getWindow();  // Obtener la ventana actual
            stage.setScene(new Scene(root, 650, 520));  // Cambiar la escena
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initialize(URL url, ResourceBundle rb) {
        calcularPrecioTotal();
    }
}
