package org.tudo.analyses;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.io.FileUtils;
import org.tudo.Library;
import org.tudo.managers.LibraryPairManager;
import org.tudo.managers.LibraryPairManager_old;
import org.tudo.persistenceManagers.LibrariesPersistenceManager;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * Used for static analysis between leaf- & dependent-library with the help of OPAL.
 */
public class DependenceAnalysis {
    private LibrariesPersistenceManager librariesPersistenceManager;

    public DependenceAnalysis() {
        this.librariesPersistenceManager = new LibrariesPersistenceManager();
    }

    public void analyzeRandomLeaf() {
//        String leafCoordinates = "org.apache.tomcat:servlet-api:6.0.13";
        String leafCoordinates = "javax.servlet:servlet-api:2.2";
//        String leafCoordinates = "springframework:spring-core:1.1";
        Library leafLibrary = librariesPersistenceManager.getLibraryByCoordinates(leafCoordinates);
        if (leafLibrary == null) {
            System.err.println("analyzeDependency: leaf " + leafCoordinates + " not found");
        }
        File leafFile = downloadJar(leafLibrary.getMavenCentralJarUri());
        if (leafFile != null) {
            analyzeLeaf(leafLibrary, leafFile);
            this.deleteJar(leafFile);
        }
    }

    public void analyzeRandomLeafs() {
        Set<Library> leafLibraries = librariesPersistenceManager.getRandom(300);
        for (Library leafLibrary : leafLibraries) {
            File leafFile = downloadJar(leafLibrary.getMavenCentralJarUri());
            if (leafFile != null) {
                analyzeLeaf(leafLibrary, leafFile);
                this.deleteJar(leafFile);
            }
        }
    }

    /**
     * analyzes each leafLibrary with all its dependentLibrary, defines config for OPAL analysis and calls analyzeLibraryPair
     *
     * @param leafLibrary
     * @param leafFile
     */
    public void analyzeLeaf(Library leafLibrary, File leafFile) {
        List<Library> dependentLibraries = librariesPersistenceManager.getLibrariesByLeafCoordinates(leafLibrary.getCoordinate());
        if (dependentLibraries.isEmpty()) {
            System.out.println("analyzeLeaf: No Library depends on leaf " + leafLibrary.getCoordinate());
            return;
        }

        // Build a sane config for "treat as libraries"
        String cfg = ""
                + "org.opalj.br.analyses.cg { \n"
                + "  ClosedPackagesKey { analysis = org.opalj.br.analyses.cg.AllPackagesClosed }\n"
                + "  InitialEntryPointsKey { analysis = org.opalj.br.analyses.cg.LibraryEntryPointsFinder }\n"
                + "}\n";

        Config overrideConfig = ConfigFactory.parseString(cfg).withFallback(ConfigFactory.load());
        LibraryPairManager libraryPairManager = new LibraryPairManager();

        for (Library dependentLibrary : dependentLibraries) {
//            Library dependentLibrary = dependentLibraries.get(0);
            File dependentFile = downloadJar(dependentLibrary.getMavenCentralJarUri());
            if (dependentFile != null) {
                libraryPairManager.resetProject();
                libraryPairManager.initProject(leafFile, dependentFile, overrideConfig, leafLibrary.getCoordinate(), dependentLibrary.getCoordinate());
//        LibraryPairManager_old lM = new LibraryPairManager_old(leafFile, dependentFile, overrideConfig);
//        lM.analyzeLibraryPair();
                this.deleteJar(dependentFile);
            }
        }
    }

    private static File downloadJar(String uriStr) {
        if (uriStr == null || uriStr.isBlank()) {
            System.err.println("downloadJar: Library's Maven Central Jar Uri is null or blank");
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
            System.err.println("downloadJar: targetDir is null");
            return null;
        }
        if (!targetDir.exists()) {
//            Files.createDirectories(targetDir.toPath());
            System.err.println("downloadJar: targetDir dont exist");
            return null;
        } else if (!targetDir.isDirectory()) {
            System.err.println("downloadJar: targetDir is not a directory");
            return null;
        }

        URL url = null;
        try {
            url = new URL(uriStr);
        } catch (MalformedURLException e) {
            System.err.println("downloadJar: error at new URL");
            e.printStackTrace();
            return null;
        }
        File outFile = new File(targetDir, fileName);
//        System.out.println("fileName: " + fileName);

        // Download using Apache Commons IO
        try {
            FileUtils.copyURLToFile(url, outFile);
        } catch (IOException e) {
            System.err.println("downloadJar: error at copy URL to file");
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

    private void deleteJar(File file) {
        if (file == null) return;
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("deleteJar: could not delete file " + file.getAbsolutePath());
            }
        }
    }

}
