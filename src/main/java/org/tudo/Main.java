package org.tudo;

import org.tudo.managers.LeafLibrariesPersistenceManager;
import org.tudo.sse.model.ArtifactIdent;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("11111 Running LeafLibrariesAnalysis ...");
        runMarinAnalysis();
    }

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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            manager.close();
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

    private static void runOPALAnalysis() {
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

}