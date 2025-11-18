package org.tudo.analyses;

import org.tudo.LeafLibrary;
import org.tudo.sse.MavenCentralAnalysis;
import org.tudo.sse.model.Artifact;
import org.tudo.sse.model.ArtifactIdent;
import org.tudo.sse.model.pom.Dependency;
import org.tudo.sse.model.pom.PomInformation;
import org.tudo.sse.model.pom.RawPomFeatures;
import org.tudo.utils.PomUtils;

import java.util.*;

public class DependentLibrariesAnalysis extends MavenCentralAnalysis {
    private final Set<LeafLibrary> leafLibraries;
    private final Map<LeafLibrary, Set<ArtifactIdent>> librariesMap = new HashMap<>(); //Set<ArtifactIdent> prevents duplicates for ArtifactIdent

    public DependentLibrariesAnalysis (Set<LeafLibrary> leafLibraries) {
        super();
        this.resolvePom = true;
        this.resolveJar = true; // keep jars handy for later OPAL runs
        this.leafLibraries = leafLibraries;
        if (!leafLibraries.isEmpty()) {
            for (LeafLibrary leafLibrary : leafLibraries){
                librariesMap.computeIfAbsent(leafLibrary, k -> new HashSet<>());
            }
        }
    }

    /*
    *
    * */
    @Override
    public void analyzeArtifact(Artifact artifact) {
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

        // for all dependencies of an artifact
        for (Dependency dependency : dependencies) {
            if (PomUtils.isActualDependency(dependency)) {
                ArtifactIdent dependencyId = dependency.getIdent();
                if(dependencyId == null) continue;

                //check if depedency is one of (random found) leaf libraries
                //if true --> add to map with the correspondent leaf library
                for (LeafLibrary leafLibrary : leafLibraries) {
                    System.out.println("depedency gav: " + dependencyId.getCoordinates() + ", leaf gav: " + leafLibrary.getCoordinate());
                    if(leafLibrary.getCoordinate().equals(dependencyId.getCoordinates())) {
                        System.out.println("equal");
                        librariesMap.get(leafLibrary).add(id);
                    }
                }
            }
        }
    }

    public Map<LeafLibrary, Set<ArtifactIdent>> getLibrariesMap() {
        return librariesMap;
    }

    public static void printLibrariesMap(Map<LeafLibrary, Set<ArtifactIdent>> map) {
        if (map == null || map.isEmpty()) {
            System.out.println("LibrariesMap is empty");
            return;
        }
        map.entrySet().stream()
                // sort by library coordinates for stable, readable output
//                .sorted(Comparator.comparing(e -> libCoords(e.getKey())))
                .forEach(entry -> {
                    LeafLibrary lib = entry.getKey();
                    Set<ArtifactIdent> id = entry.getValue();
                    System.out.println(lib.getCoordinate());

                    if (id == null || id.isEmpty()) {
                        System.out.println("  (no dependent libraries)");
                    } else {
                        // print each artifact’s coordinates, sorted
                        id.stream()
                                .map(artifactIdent -> artifactIdent.getCoordinates())
//                                .sorted()
                                .forEach(coordinate -> System.out.println("  - " + coordinate));
                        System.out.println(id.size() + " dependent libraries");
                    }
                });
    }

}
