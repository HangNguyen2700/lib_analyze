package org.tudo;

import org.tudo.analyses.DependenceAnalysis;
import org.tudo.analyses.DependentLibrariesAnalysis;
import org.tudo.analyses.LeafLibrariesAnalysis;
import org.tudo.analyses.LibrariesAnalysis;
import org.tudo.persistenceManagers.LeafLibrariesPersistenceManager;
import org.tudo.persistenceManagers.LibrariesPersistenceManager;
import org.tudo.sse.model.ArtifactIdent;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

//        System.out.println("11111 Running LeafLibrariesAnalysis ...");
//        runMarinAnalysis(); //done

//        System.out.println("22222 Running LibrariesAnalysis ...");
//        runMarinLibrariesAnalysis(); //done

        System.out.println("33333 Running OPALAnalysis ...");
        runOPALAnalysis(args);



    }

    /*
    * Run Marin analysis to:
    * - identify all leaf libraries on Maven Central
    * - store the found leaf libraries into DB
    * Result: 65244 leaf libraries found at 16.10.2025
    * */
    private static void runMarinAnalysis() {
        LeafLibrariesPersistenceManager manager = new LeafLibrariesPersistenceManager();
        LeafLibrariesAnalysis leafLibrariesAnalysis = new LeafLibrariesAnalysis(manager);

        String checkpoint = "lastIndexProcessed";
        // We always drive MARIN with -st (slice size) + -ip/--name (resume)
        final String[] marinArgs = new String[]{
                "--multi", "8",
                "--name", checkpoint,
                "-ip", checkpoint
        };

        try {
            leafLibrariesAnalysis.runAnalysis(marinArgs);
            System.out.println("MARIN analysis completed successfully");
        } catch (Exception e) {
            System.err.println("MARIN analysis terminated with exception: ");
            e.printStackTrace();
//        } finally {
//            manager.close();
        }
    }

    /*
     * Run Marin analysis to:
     * - identify and store all libraries on Maven Central into DB
     * Result: runtime c.a 13h 3.11.2025
     * - 302336 libraries (jar-package)
     * - 64566 leaf libraries
     * */
    private static void runMarinLibrariesAnalysis() {
        LibrariesPersistenceManager manager = new LibrariesPersistenceManager();
        LibrariesAnalysis librariesAnalysis = new LibrariesAnalysis(manager);

        String checkpoint = "lastIndexProcessed";
        final String[] marinArgs = new String[]{
                "--multi", "8",
                "--name", checkpoint,
                "-ip", checkpoint
        };

        try {
            librariesAnalysis.runAnalysis(marinArgs);
            System.out.println("MARIN analysis completed successfully");
        } catch (Exception e) {
            System.err.println("MARIN analysis terminated with exception: ");
            e.printStackTrace();
        }
    }

    // Reads the checkpoint file content (empty string if not present)
    private static String readCheckpoint(String pathString) {
        try {
            Path path = Paths.get(pathString);
            if (Files.exists(path)) return Files.readString(path);
        } catch (IOException ignored) {
        }
        return "";
    }

    /*
     * Run OPAL analysis to:
     * - get random n leaf libraries from DB
     * - load and map all ArtifactIdents, whose Artifact depends on that leaf library, to each leaf library
     *
     * */
    private static void runOPALAnalysis(String[] args) {
        DependenceAnalysis dependenceAnalysis = new DependenceAnalysis();
        dependenceAnalysis.analyzeDependency();
    }


    private static void runOPALAnalysis2(String[] args) {
        if (args.length < 2) {
            System.err.println("runOPALAnalysis: arguments missing");
            System.exit(2);
        }
        int n = 0;
        try {
            n = Integer.parseInt(args[1]);
            if (n <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.err.println("Invalid n: " + args[1] + " (Number of leaf libraries to be fetched must be a positive integer)");
            System.exit(2);
        }

//      Gets random leaf libraries
        LeafLibrariesPersistenceManager persistenceManager = new LeafLibrariesPersistenceManager();
        Set<LeafLibrary> leafLibraries = persistenceManager.getRandom(n);
//        for(LeafLibrary leafLibrary : leafLibraries) {
//            System.out.println(leafLibrary.getGroupId() + ":" + leafLibrary.getArtifactId() + ":" + leafLibrary.getVersion());
//        }
//        System.out.println(leafLibraries.size());

//        Loads and maps all ArtifactIdents to leaf libraries
        String checkpoint = "lastIndexProcessed";
        // We always drive MARIN with -st (slice size) + -ip/--name (resume)
        final String[] marinArgs = new String[]{
                "-st","0:1000",
                "--multi", "8",
                "--name", checkpoint,
                "-ip", checkpoint
        };
        DependentLibrariesAnalysis dependentLibrariesAnalysis = new DependentLibrariesAnalysis(leafLibraries);
        Map<LeafLibrary, Set<ArtifactIdent>> librariesMap = new HashMap<>();

//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            try {
//                System.err.println("\n[shutdown] Ctrl+C detected. Showing libraries map...");
//                // snapshot to avoid ConcurrentModificationException while other threads write
//                Map<LeafLibrary, Set<ArtifactIdent>> snapshot = deepSnapshot(librariesMap);
//                DependentLibrariesAnalysis.printLibrariesMap(librariesMap);
//                System.err.println("[shutdown] Done.");
//                System.err.flush();
//                System.out.flush();
//            } catch (Throwable t) {
//                t.printStackTrace();
//            }
//        }, "dump-on-shutdown"));

        try {
            dependentLibrariesAnalysis.runAnalysis(marinArgs);
            System.out.println("MARIN analysis for dependent libraries completed successfully");
        } catch (Exception e) {
            System.err.println("MARIN analysis for dependent libraries terminated with exception: ");
            e.printStackTrace();
        } finally {
            //finally: needs for testing locally, modify it when want to let the whole analysis run
            librariesMap = dependentLibrariesAnalysis.getLibrariesMap();
            DependentLibrariesAnalysis.printLibrariesMap(librariesMap);
        }


        //        if (args.length != 2) {
//            System.err.println("Usage: LibsCallGraphBuilder <A.jar> <B.jar>");
//            System.exit(1);
//        }
//        File aFile = new File(args[0]);
//        File bFile = new File(args[1]);

//
//        // 1) Build a sane config for "treat as libraries"
//        String cfg = ""
//                + "org.opalj.br.analyses.cg { \n"
//                + "  ClosedPackagesKey { analysis = org.opalj.br.analyses.cg.AllPackagesClosed }\n"
//                + "  InitialEntryPointsKey { analysis = org.opalj.br.analyses.cg.LibraryEntryPointsFinder }\n"
//                + "}\n";
//
//        Config overrideConfig = ConfigFactory.parseString(cfg).withFallback(ConfigFactory.load());
//
//        LibsManager libsManager = new LibsManager(aFile, bFile, overrideConfig);
//        libsManager.analyze();
    }

    //Snapshot helper (thread-safe copy of the map for printing)
    private static Map<LeafLibrary, Set<ArtifactIdent>> deepSnapshot(
            Map<LeafLibrary, Set<ArtifactIdent>> src) {
        Map<LeafLibrary, Set<ArtifactIdent>> copy = new java.util.HashMap<>(src.size());
        for (Map.Entry<LeafLibrary, Set<ArtifactIdent>> e : src.entrySet()) {
            copy.put(e.getKey(), new java.util.HashSet<>(e.getValue())); // copy each Set
        }
        return copy;
    }

}