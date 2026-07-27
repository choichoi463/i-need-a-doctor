package org.example.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.java.Log;
import org.example.Luxmed;
import org.example.doctor.DoctorType;
import org.example.utils.DoctorNameReader;

import java.util.List;

@Log
public class ConfigApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Label doctorTypeLabel = new Label("Doctor type:");
        ComboBox<DoctorType> doctorTypeCombo = new ComboBox<>();
        doctorTypeCombo.getItems().setAll(DoctorType.values());
        doctorTypeCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(DoctorType doctorType) {
                return doctorType == null ? "" : doctorType.getDisplayName();
            }

            @Override
            public DoctorType fromString(String string) {
                return null;
            }
        });

        Label doctorNameLabel = new Label("Doctor name:");
        ComboBox<String> doctorNameCombo = new ComboBox<>();
        doctorNameCombo.setEditable(true);

        doctorTypeCombo.valueProperty().addListener((obs, oldType, newType) -> {
            doctorNameCombo.getItems().clear();
            if (newType != null) {
                List<String> names = DoctorNameReader.loadNames(newType);
                doctorNameCombo.getItems().setAll(names);
                if (!names.isEmpty()) {
                    doctorNameCombo.setValue(names.get(0));
                }
            }
        });

        Button startButton = new Button("Start");
        Label statusLabel = new Label();
        startButton.setOnAction(event -> {
            DoctorType selectedType = doctorTypeCombo.getValue();
            String selectedName = doctorNameCombo.getEditor().getText();

            if (selectedType == null || selectedName == null || selectedName.isBlank()) {
                statusLabel.setText("Please select a doctor type and name.");
                return;
            }

            startButton.setDisable(true);
            log.info("Starting Luxmed automation for " + selectedType.getDisplayName() + " / " + selectedName);

            // Non-daemon: Application.launch() returns once this stage closes, and the JavaFX
            // platform's implicit exit would otherwise let the JVM terminate before the loop runs.
            Thread automationThread = new Thread(() -> Luxmed.startLoop(selectedType, selectedName));
            automationThread.start();

            stage.close();
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(12);
        grid.add(doctorTypeLabel, 0, 0);
        grid.add(doctorTypeCombo, 1, 0);
        grid.add(doctorNameLabel, 0, 1);
        grid.add(doctorNameCombo, 1, 1);
        grid.add(startButton, 1, 2);
        grid.add(statusLabel, 0, 3, 2, 1);

        stage.setTitle("Luxmed booking setup");
        stage.setScene(new Scene(grid, 400, 220));
        stage.show();
    }
}
