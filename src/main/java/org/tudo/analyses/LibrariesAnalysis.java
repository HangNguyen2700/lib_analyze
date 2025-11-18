package org.tudo.analyses;

import org.tudo.Library;
import org.tudo.persistenceManagers.LibrariesPersistenceManager;
import org.tudo.sse.MavenCentralAnalysis;
import org.tudo.sse.model.Artifact;
import org.tudo.sse.model.ArtifactIdent;
import org.tudo.sse.model.pom.Dependency;
import org.tudo.sse.model.pom.PomInformation;
import org.tudo.sse.model.pom.RawPomFeatures;
import org.tudo.utils.PomUtils;

import java.util.List;

public class LibrariesAnalysis extends MavenCentralAnalysis {
    private final LibrariesPersistenceManager manager;

    public LibrariesAnalysis(LibrariesPersistenceManager manager) {
        super();
        this.resolvePom = true;
        this.manager = manager;
    }

    @Override
    public void analyzeArtifact(Artifact artifact) {
        if (artifact == null) return;

        ArtifactIdent id = artifact.getIdent();
        if (id == null) return;

        PomInformation pomInfo = artifact.getPomInformation();
        if (pomInfo == null) return;

        // Packaging filter: treat only "jar" as libraries.
        String packaging = PomUtils.trimString(pomInfo.getRawPomFeatures().getPackaging());
        if (!packaging.equals("jar")) return;

        // Get all direct dependencies from the raw POM
        RawPomFeatures pomFeatures = pomInfo.getRawPomFeatures();
        if (pomFeatures == null) return;

//        List<Dependency> dependencies = pomInfo.getRawPomFeatures().getDependencies();
        List<Dependency> dependencies = pomInfo.getResolvedDependencies();
        String actualDependencyCoordinates = "";
        for (Dependency dependency : dependencies) {
            if (PomUtils.isActualDependency(dependency)) {
                actualDependencyCoordinates += (dependency.getIdent().getCoordinates() + ", ");
            }
        }
        System.out.println("actualDependencyCoordinates: " + actualDependencyCoordinates);
        System.out.println("is leaf? " + actualDependencyCoordinates.isEmpty());
        if (actualDependencyCoordinates.isEmpty()) {
            System.out.println("saving leaf library");
            Library library = new Library(id.getGroupID(), id.getArtifactID(), id.getVersion(), id.getMavenCentralJarUri().toString(), true, "");
            manager.save(library);
        } else {
            System.out.println("saving library");
            Library library = new Library(id.getGroupID(), id.getArtifactID(), id.getVersion(), id.getMavenCentralJarUri().toString(), false, actualDependencyCoordinates);
            manager.save(library);
        }
    }
}
