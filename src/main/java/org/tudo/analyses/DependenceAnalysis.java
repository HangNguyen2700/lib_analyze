package org.tudo.analyses;

import org.tudo.Library;
import org.tudo.persistenceManagers.LibrariesPersistenceManager;

import java.util.List;

public class DependenceAnalysis {
    private LibrariesPersistenceManager librariesPersistenceManager;

    public DependenceAnalysis() {
        this.librariesPersistenceManager = new LibrariesPersistenceManager();
    }



    public void analyzeDependency() {
        //TODO: get random n leaf library instead of 1 given leaf library coordinates string
        List<Library> dependentLibrary = librariesPersistenceManager.getLibrariesByDependency("org.apache.tomcat:servlet-api:6.0.13");
        Library firstDependentLibrary = dependentLibrary.get(0);

    }
}
