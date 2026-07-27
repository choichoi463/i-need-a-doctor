package org.example.utils;

import org.example.doctor.DoctorType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class DoctorNameReader {

    public static List<String> loadNames(DoctorType doctorType) {
        Path path = Paths.get("src", "main", "resources", "doctors", doctorType.getDoctorsFileName());
        try {
            return Files.readAllLines(path).stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Cannot load doctor names from " + path, e);
        }
    }
}
