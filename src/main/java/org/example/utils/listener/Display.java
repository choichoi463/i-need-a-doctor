package org.example.utils.listener;

public class Display {
    private final Sensor sensor;

    public Display(Sensor sensor) {
        this.sensor = sensor;
        sensor.addListener(v -> System.out.println("Display notified: " + v));
    }

    public void showCurrent() {
        System.out.println("Current value: " + sensor.getValue());
    }

    public boolean getValue() {
        return sensor.getValue();
    }
}