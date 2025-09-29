package org.tudo;

import org.tudo.sse.MavenCentralAnalysis;
import org.tudo.sse.model.Artifact;
import org.tudo.sse.model.ArtifactIdent;
import org.tudo.sse.model.pom.Dependency;
import org.tudo.sse.model.pom.PomInformation;
import org.tudo.sse.model.pom.RawPomFeatures;
import org.tudo.utils.PomUtils;

import java.util.*;


public class LeafLibrariesAnalysis extends MavenCentralAnalysis {
    private final Set<String> leafCoords;
    private final Set<ArtifactIdent> leafIds;
    private final Set<LeafLibrary> leafLibraries;
    private int i;

    public LeafLibrariesAnalysis() {
//        super(false, true, false, false);
        super();
        this.resolvePom = true;
        this.leafCoords = new HashSet<>();
        this.leafIds = new HashSet<>();
        this.leafLibraries = new HashSet<>();
        this.i = 0;
    }

    @Override
    public void analyzeArtifact(Artifact artifact) {
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
//            leafIds.add(id);
            LeafLibrary leafLibrary = new LeafLibrary(id.getGroupID(), id.getArtifactID(), id.getVersion());
            leafLibraries.add(leafLibrary);

//            String gav = id.getCoordinates();
//            leafCoords.add(gav);
//            i++;
//            System.out.println(i + " : " + gav);
        }
    }

    public Set<String> getLeafCoords() {
        return leafCoords;
    }

    public Set<ArtifactIdent> getLeafIds() {
        return leafIds;
    }

    public Set<LeafLibrary> getLeafLibraries() {
        return leafLibraries;
    }
}
