package org.tudo;

import org.tudo.managers.LeafLibrariesPersistenceManager;
import org.tudo.sse.MavenCentralAnalysis;
import org.tudo.sse.model.Artifact;
import org.tudo.sse.model.ArtifactIdent;
import org.tudo.sse.model.pom.Dependency;
import org.tudo.sse.model.pom.PomInformation;
import org.tudo.sse.model.pom.RawPomFeatures;
import org.tudo.utils.PomUtils;

import java.util.*;


public class LeafLibrariesAnalysis extends MavenCentralAnalysis {
    private final LeafLibrariesPersistenceManager manager;
    private int librariesCounter;
    private int leafLibrariesCounter;
//    private final Set<ArtifactIdent> leafIds;
//    private final Set<LeafLibrary> leafLibraries;

    public LeafLibrariesAnalysis(LeafLibrariesPersistenceManager manager) {
        super();
        this.resolvePom = true;
        this.manager = manager;
        this.librariesCounter = 0;
        this.leafLibrariesCounter = 0;
//        this.leafIds = new HashSet<>();
//        this.leafLibraries = new HashSet<>();
    }

    @Override
    public void analyzeArtifact(Artifact artifact) {
//        librariesCounter++;
//        System.out.println("library: " + librariesCounter);

        if(artifact == null) return;

        ArtifactIdent id = artifact.getIdent();
        if(id == null) return;

        PomInformation pomInfo = artifact.getPomInformation();
        if (pomInfo == null) return;

        // Packaging filter: treat only "jar" (and OSGi "bundle") as libraries.
        String packaging = PomUtils.trimString(pomInfo.getRawPomFeatures().getPackaging());
        if (!packaging.equals("jar")) return;

        // Get all direct dependencies from the raw POM
        RawPomFeatures pomFeatures = pomInfo.getRawPomFeatures();
        if(pomFeatures == null) return;

        List<Dependency> dependencies = pomInfo.getRawPomFeatures().getDependencies();
        List<Dependency> actualDependencies = new ArrayList<>();
        for (Dependency dependency : dependencies) {
            if (PomUtils.isActualDependency(dependency)) {
                actualDependencies.add(dependency); // keep only deps that matter at runtime for consumers
            }
        }
        if (actualDependencies.isEmpty()) {
            leafLibrariesCounter++;
            System.out.println("leaf library: " + leafLibrariesCounter);
            LeafLibrary leafLibrary = new LeafLibrary(id.getGroupID(), id.getArtifactID(), id.getVersion(), id.getMavenCentralJarUri().toString());
//            leafIds.add(id);
//            leafLibraries.add(leafLibrary);
            manager.save(leafLibrary);
        }
    }

    public int getLibrariesCounterCounter() {
        return librariesCounter;
    }

    public int getLeafLibrariesCounter() {
        return leafLibrariesCounter;
    }

//    public Set<ArtifactIdent> getLeafIds() {
//        return leafIds;
//    }
//
//    public Set<LeafLibrary> getLeafLibraries() {
//        return leafLibraries;
//    }
}
