package org.tudo;

import org.tudo.sse.MavenCentralAnalysis;
import org.tudo.sse.model.Artifact;
import org.tudo.sse.model.ArtifactIdent;
import org.tudo.sse.model.pom.Dependency;
import org.tudo.sse.model.pom.PomInformation;
import org.tudo.sse.model.pom.RawPomFeatures;
import org.tudo.utils.PomUtils;

import java.sql.SQLOutput;
import java.util.*;

public class DependentLibrariesAnalysis extends MavenCentralAnalysis {
    private final Set<ArtifactIdent> leafArtifactIds;
    private final Map<ArtifactIdent, Set<ArtifactIdent>> mapDepentArtifactIds = new HashMap<>();

//    private final Map<String, List<ArtifactIdent>> gaToLeafs = new HashMap<>();
//    private final Map<ArtifactIdent, Set<ArtifactIdent>> dependents = new HashMap<>();

    public DependentLibrariesAnalysis (Set<ArtifactIdent> leafArtifactIds) {
        super();
        this.resolvePom = true;
        this.resolveJar = true; // keep jars handy for later OPAL runs
        this.leafArtifactIds = new HashSet<>(leafArtifactIds);
        if (!leafArtifactIds.isEmpty()) {
            for (ArtifactIdent artifactIdent : leafArtifactIds){
//                String ga = artifactIdent.getGroupID() + ":" + artifactIdent.getArtifactId();
//                gaToLeafs.computeIfAbsent(ga, k -> new ArrayList<>()).add(artifactIdent);
                mapDepentArtifactIds.computeIfAbsent(artifactIdent, k -> new HashSet<>());
            }
        }
    }
    @Override
    public void analyzeArtifact(Artifact artifact) {
//        if (artifact == null) return;
//
//        PomInformation pomInfo = artifact.getPomInformation();
//        if (pomInfo == null) return;
//
//        // Only consider library artifacts.
//        RawPomFeatures raw = pomInfo.getRawPomFeatures();
//        String packaging = PomUtils.trimString(raw.getPackaging());
//        if (!"jar".equals(packaging) && !"bundle".equals(packaging)) return;
//
//        List<Dependency> deps = raw.getDependencies();
//        if (deps == null || deps.isEmpty()) return;

        if(artifact == null) return;

        ArtifactIdent id = artifact.getIdent();
        if(id == null) return;

        PomInformation pomInfo = artifact.getPomInformation();
        if (pomInfo == null) return;

        // Packaging filter: treat only "jar" as libraries.
        String packaging = PomUtils.trimString(pomInfo.getRawPomFeatures().getPackaging());
        if (!packaging.equals("jar")) return;

        // Get all direct dependencies from the raw POM
        RawPomFeatures pomFeatures = pomInfo.getRawPomFeatures();
        if(pomFeatures == null) return;

        List<Dependency> dependencies = pomInfo.getRawPomFeatures().getDependencies();
        if(dependencies == null || dependencies.isEmpty()) return;

//        List<Dependency> actualDependencies = new ArrayList<>();
        boolean containsKey = false;
        boolean equal = false;
        for (Dependency dependency : dependencies) {
            containsKey = false;
            equal = false;
            if (PomUtils.isActualDependency(dependency)) {
                ArtifactIdent dependencyId = dependency.getIdent();
                if(dependencyId == null) continue;
                for (ArtifactIdent leafId : leafArtifactIds) {
                    System.out.println("depedency gav: " + dependencyId.getCoordinates() + ", leaf gav: " + leafId.getCoordinates());
                    if(leafId.getCoordinates().equals(dependencyId.getCoordinates())) {
                        equal = true;
                        System.out.println("equal");
                        break;
                    }
                }
                containsKey = mapDepentArtifactIds.containsKey(dependencyId);
                System.out.println("is map contains key: " + containsKey);
                if((!equal && containsKey) || (equal && !containsKey)) {
                    System.out.println("ACHTUNG!!!! equal: " + equal + ", containsKey: " + containsKey );
                }
            }
        }
//####
//        for (Dependency d : deps) {
//            if (!PomUtils.isMeaningfulDependency(d)) continue; // skip tests/BOMs/etc.
//            String ga = d.getGroupId() + ":" + d.getArtifactId();
//
//            List<ArtifactIdent> targets = gaToLeafs.get(ga);
//            if (targets == null) continue;
//
//            // Current artifact (B) depends on leaf A (by GA).
//            for (ArtifactIdent a : targets) {
//                dependents.computeIfAbsent(a, k -> new LinkedHashSet<>())
//                        .add(artifact.getIdentifier());
//            }
//        }
    }
}
