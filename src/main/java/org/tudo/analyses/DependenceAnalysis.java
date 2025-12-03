package org.tudo.analyses;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.tudo.Library;
import org.example.LibraryPairManager2;
import org.tudo.managers.LibraryPairManager;
import org.tudo.persistenceManagers.LibrariesPersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Used for static analysis between leaf- & dependent-library with the help of OPAL.
 */
public class DependenceAnalysis {
    private LibrariesPersistenceManager librariesPersistenceManager;

    public DependenceAnalysis() {
        this.librariesPersistenceManager = new LibrariesPersistenceManager();
    }

    public void analyzeDependency() {
        //TODO: get random n leaf library instead of 1 given leaf library coordinates string
//        String leafCoordinates = "org.apache.tomcat:servlet-api:6.0.13";
        String leafCoordinates = "javax.servlet:servlet-api:2.2";
        Library leafLibrary = librariesPersistenceManager.getLibraryByCoordinates(leafCoordinates);
        if(leafLibrary == null){
            System.out.println("analyzeDependency: leaf " + leafCoordinates + " not found" );
        }

        List<Library> dependentLibraries = librariesPersistenceManager.getLibrariesByLeafCoordinates(leafCoordinates);
        if(dependentLibraries.isEmpty()){
            System.out.println("analyzeDependency: No Library depends on leaf " + leafCoordinates);
            return;
        }
        Library firstDependentLibrary = dependentLibraries.get(0);

        //TODO: handle on list n dependentLibraries
        File leafFile = downloadJar(leafLibrary.getMavenCentralJarUri());
        File dependentFile = downloadJar(firstDependentLibrary.getMavenCentralJarUri());

        // Build a sane config for "treat as libraries"
        String cfg = ""
                + "org.opalj.br.analyses.cg { \n"
                + "  ClosedPackagesKey { analysis = org.opalj.br.analyses.cg.AllPackagesClosed }\n"
                + "  InitialEntryPointsKey { analysis = org.opalj.br.analyses.cg.LibraryEntryPointsFinder }\n"
                + "}\n";

        Config overrideConfig = ConfigFactory.parseString(cfg).withFallback(ConfigFactory.load());

        LibraryPairManager libraryPairManager = new LibraryPairManager(leafFile, dependentFile, overrideConfig);
        libraryPairManager.analyzeLibraryPair();
    }

    private static File downloadJar(String uriStr) {
        if (uriStr == null || uriStr.isBlank()) {
            System.out.println("downloadJar: Library's Maven Central Jar Uri is null or blank");
            return null;
        }
        // Derive a filename from the URL path (e.g., servlet-api-6.0.13.jar)
        String fileName = extractFileNameFromUri(uriStr);
        if (fileName.isEmpty()) {
            // Fallback in weird cases
            fileName = "downloaded-" + System.currentTimeMillis() + ".jar";
        }

        // Jar-File is downloaded to directory targetDir
        File targetDir = new File("src/main/resources/libraryFiles");
        if (targetDir == null) {
            System.out.println("downloadJar: targetDir is null");
            return null;
        }
        if (!targetDir.exists()) {
//            Files.createDirectories(targetDir.toPath());
            System.out.println("downloadJar: targetDir dont exist");
            return null;
        } else if (!targetDir.isDirectory()) {
            System.out.println("downloadJar: targetDir is not a directory");
            return null;
        }

        URL url = null;
        try {
            url = new URL(uriStr);
        } catch (MalformedURLException e) {
            System.out.println("downloadJar: error at new URL");
            e.printStackTrace();
            return null;
        }
        File outFile = new File(targetDir, fileName);
//        System.out.println("fileName: " + fileName);

        // Download using Apache Commons IO
        try {
            FileUtils.copyURLToFile(url, outFile);
        } catch (IOException e) {
            System.out.println("downloadJar: error at copy URL to file");
            e.printStackTrace();
            return null;
        }

        return outFile;
    }

    private static String extractFileNameFromUri(String uriStr) {
        int lastSlash = uriStr.lastIndexOf('/');
        if (lastSlash == -1 || lastSlash == uriStr.length() - 1) {
            return "";
        }
        return uriStr.substring(lastSlash + 1);
    }


}
