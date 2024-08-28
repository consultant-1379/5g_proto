package com.ericsson.esc.escview;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;


import java.util.Arrays;

public class MainApp extends Application {
    private static int PODS = 8;
    private static BsfWorkerLoadModel loadModel;

    @Override
    public void start(Stage stage) {
        final NumberAxis xAxis = new NumberAxis();
        final CategoryAxis yAxis = new CategoryAxis();
        final BarChart<Number, String> bc = new BarChart<Number, String>(xAxis, yAxis);
        bc.setTitle("BSF PODs - Load Distribution");

        xAxis.setLabel("CPU Load");
        xAxis.setTickLabelRotation(90);
        yAxis.setLabel("POD");

        XYChart.Series series[] = new XYChart.Series[PODS];
        for (int pod=0; pod < PODS; pod++) {
            series[pod]  = new XYChart.Series();
            series[pod].setName(loadModel.getNameForPod(pod));
            series[pod].getData().add(new XYChart.Data(loadModel.getLoadForPod(pod), ""));
        }

        Timeline tl = new Timeline();
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(1500),
            new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent actionEvent) {
                    int pod = 0;

                    for (XYChart.Series<Number, String> series : bc.getData()) {
                        series.setName(loadModel.getNameForPod(pod));

                        for (XYChart.Data<Number, String> data : series.getData()) {
                            data.setXValue(loadModel.getLoadForPod(pod++));
                        }
                    }
                }
            }));
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();

        Scene scene = new Scene(bc, 800, 600);
        for (int pod=0; pod < PODS; pod++)
            bc.getData().add(series[pod]);
        // bc.getData().addAll(series1, series2, series3);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        // loadModel = new BsfWorkerLoadModel("10.210.52.30", 31788, PODS);
        loadModel = new BsfWorkerLoadModel(args[0], (int) Integer.parseInt(args[1]), PODS = (int) Integer.parseInt(args[2]));
        loadModel.start();

        launch(args);
    }
}



