package org.tudo;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.tudo.managers.LeafLibrariesPersistenceManager;
import org.tudo.sse.MavenCentralAnalysis;
import org.tudo.sse.model.ArtifactIdent;


import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        LeafLibrariesAnalysis leafLibrariesAnalysis = new LeafLibrariesAnalysis();
        try {
            System.out.println("11111 Running LeafLibrariesAnalysis ...");
            leafLibrariesAnalysis.runAnalysis(args);
//            Set<ArtifactIdent> leafIds = leafLibrariesAnalysis.getLeafIds();
//            System.out.println("### number of leaf libraries found: "  + leafIds.size());
            Set<LeafLibrary> leafLibraries = leafLibrariesAnalysis.getLeafLibraries();
            System.out.println("### number of leaf libraries found: "  + leafLibraries.size());
            for (LeafLibrary lib : leafLibraries) {
                System.out.println(lib.getGroupId() + ":" + lib.getArtifactId() + ":" + lib.getBaseVersion());
            }
            LeafLibrariesPersistenceManager manager = new LeafLibrariesPersistenceManager();
            manager.saveLeafLibraries(leafLibraries);
//            LeafLibrary first = leafLibraries.iterator().next();
//            manager.saveLeafLibrary(first);

//            System.out.println("22222 Running DependentsOfLeafsAnalysis ...");
//            DependentLibrariesAnalysis dependentsAnalysis = new DependentLibrariesAnalysis(leafIds);
//            dependentsAnalysis.runAnalysis(args);

        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
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
}