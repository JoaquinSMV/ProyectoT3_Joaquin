package com.example.proyecto3trimestre;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.fxml.FXMLLoader;
import java.sql.*;


public class MAIN extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
    Parent root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
    primaryStage.setTitle("LOGIN");
    primaryStage.setScene(new Scene(root, 700, 500));
    primaryStage.show();
    }



}
