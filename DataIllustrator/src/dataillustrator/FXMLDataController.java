/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dataillustrator;

import javaserialport.IMqttDataHandler;
import javaserialport.MqttDataService;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
/**
 *
 * @author Arthur
 */
public class FXMLDataController implements Initializable, IMqttDataHandler{
        String beginOfTemp = "#";
        String beginOfHeartbeat = "@";
        String beginOfAccelero = "$";
        String beginOfXAxis = "<";
        String beginOfYAxis = ">";
        String endOfData = "]";  
        int xValue = 0;
        final int MINIMUM_HEARTBEAT = 50;
        final int MAXIMUM_HEARTBEAT = 150;
        private final int NUMBER_OF_ACCELERO_SERIES = 3;
        private boolean update = false;

    @FXML
    private Label label;
    @FXML
    private Label temperature;
    @FXML
    private Label heartbeat;
    @FXML
    private Label acceleroX;
    @FXML
    private Label acceleroY;
    @FXML
    private Label acceleroZ;
    @FXML
    private Button measure;
    @FXML
    private LineChart temperaturechart;
    private XYChart.Series temperatureValues;
    @FXML
    private LineChart heartbeatchart;
    private XYChart.Series heartbeatValues;
    @FXML
    private LineChart accelerochart;
    private XYChart.Series acceleroValues[];
    
    
    

   
    @FXML
    private void startMeasure(ActionEvent event) {
        update = true;
    }
    
    @FXML
    private void stopMeasure(ActionEvent event) {
        update = false;
    }
    
    
    private MqttDataService dataService;


    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        temperatureValues = new XYChart.Series();
        temperaturechart.getData().add(temperatureValues);
        heartbeatValues = new XYChart.Series();
        heartbeatchart.getData().add(heartbeatValues);
        acceleroValues = new XYChart.Series[NUMBER_OF_ACCELERO_SERIES];
        acceleroValues[0] = new XYChart.Series();
        acceleroValues[0].setName("X-value");
        accelerochart.getData().add(acceleroValues[0]);
        acceleroValues[1] = new XYChart.Series();
        acceleroValues[1].setName("Y-value");
        accelerochart.getData().add(acceleroValues[1]);
        acceleroValues[2] = new XYChart.Series();
        acceleroValues[2].setName("Z-value");
        accelerochart.getData().add(acceleroValues[2]);

        dataService = new MqttDataService();
        dataService.setMessageHandler(this);
        
        disconnectClientOnClose();
    }    

    @Override
    public void messageArrived(String channel, String message) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
       
        Platform.runLater(new Runnable () {
            @Override
            public void run() {
        
        
                int indexTemp = message.indexOf(beginOfTemp);
                int indexHeartbeat = message.indexOf(beginOfHeartbeat);
                int indexAccelero = message.indexOf(beginOfAccelero);
                int indexEnd = message.indexOf(endOfData);
                int indexY = message.indexOf(beginOfXAxis);
                int indexZ = message.indexOf(beginOfYAxis);
                
                if (indexTemp >= 0 && indexHeartbeat >= 0 && indexAccelero >= 0 && indexEnd >= 0 && indexY >= 0 && indexZ >= 0) {
                
                    String temp = message.substring(indexTemp+1, indexHeartbeat);
                    String hb = message.substring(indexHeartbeat+1, indexAccelero);
                    String acc = message.substring(indexAccelero+1, indexEnd);
                    String X = message.substring(indexAccelero+1, indexY);
                    String Y = message.substring(indexY+1, indexZ);
                    String Z = message.substring(indexZ+1, indexEnd);
                    
                    
                    double tempint = Double.parseDouble(temp);
                    int hbint = Integer.parseInt(hb);
                    float Xint = Float.parseFloat(X);
                    float Yint = Float.parseFloat(Y);
                    float Zint = Float.parseFloat(Z);
                    
                    
                    
                    System.out.println("Temperature: " + temp);
                    if (update) {
                     temperatureValues.getData().add(new XYChart.Data(xValue, tempint));
                     temperature.setText(temp + "°C");
                     
                    label.setText("Last measure: " + time);
                     
                    }
                    
                    
                    if (hbint < MAXIMUM_HEARTBEAT && hbint > MINIMUM_HEARTBEAT) {
                        System.out.println("Heartbeat: " + hbint);
                        if (update) {
                            heartbeatValues.getData().add(new XYChart.Data(xValue, hbint));
                            heartbeat.setText(hb + " BPM");
                            
                            label.setText("Last measure: " + time);
                        }
                        
                    } else {
                        System.out.println("No valid heartbeat!");
                    }
                    
                    
                    System.out.println("Accelero: " + acc);
                    System.out.print("X: " + X);
                    System.out.print("\t");
                    System.out.print("Y: " + Y);
                    System.out.print("\t");
                    System.out.println("Z: " + Z);
                    if (update) {
                    acceleroValues[0].getData().add(new XYChart.Data(xValue, Xint));
                    acceleroX.setText(X);
                    acceleroValues[1].getData().add(new XYChart.Data(xValue, Yint));
                    acceleroY.setText(Y);
                    acceleroValues[2].getData().add(new XYChart.Data(xValue, Zint));
                    acceleroZ.setText(Z);
                    
                    label.setText("Last measure: " + time);
                    xValue++;
                    } 
                    
                } else {
                    System.out.println("No valid data!");
                    
                
                }
                
            }
        });
        
    }
    
    
    public void disconnectClientOnClose() {
        // Source: https://stackoverflow.com/a/30910015
        measure.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                // scene is set for the first time. Now its the time to listen stage changes.
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        // stage is set. now is the right time to do whatever we need to the stage in the controller.
                        ((Stage) newWindow).setOnCloseRequest((event) -> {
                            dataService.disconnect();
                        });
                    }
                });
            }
        });
    }

}
