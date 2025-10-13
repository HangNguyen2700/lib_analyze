package org.tudo.utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SystemPropertiesLoader {
    /**
     * Loads key=value pairs from system.properties into JVM System properties,
     * unless already set via -D flags or environment → system mapping elsewhere.
     */
    public static void load() {
        // If DB_URL is already present (e.g., via -D), we’ll keep it.
        if (System.getProperty("DB_URL") != null) return;

        // Common places to look (first hit wins)
//        Path[] candidates = new Path[] {
//                Path.of("system.properties"),
//                Path.of("config/system.properties"),
//                Path.of("/app/system.properties")
//        };

        Path p = Path.of("system.properties");


//        for (Path p : candidates) {
            if (Files.exists(p)) {
                try (InputStream in = new FileInputStream(p.toFile())) {
                    Properties props = new Properties();
                    props.load(in);
                    props.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
                    System.out.println("[boot] Loaded system.properties from: " + p.toAbsolutePath());
                } catch (Exception e) {
                    System.err.println("[boot] Failed loading " + p + " : " + e.getMessage());
                }
//                return; // stop after first successful file
            }
//        }

        System.out.println("[boot] system.properties not found; proceeding without it.");
    }
}
