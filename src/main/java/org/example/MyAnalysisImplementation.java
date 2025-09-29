package org.example;

import org.tudo.sse.MavenCentralAnalysis;
import org.tudo.sse.model.Artifact;

public class MyAnalysisImplementation extends MavenCentralAnalysis {
    private long numberOfClassfiles;

    public MyAnalysisImplementation() {
        // index=true, pom=false, transitive=false, jar=true
        super();
        this.resolveJar = true;
        numberOfClassfiles = 0;
    }

    @Override
    public void analyzeArtifact(Artifact toAnalyze) {
        if(toAnalyze.getJarInformation() != null) {
            numberOfClassfiles += toAnalyze.getJarInformation().getNumClassFiles();
        }
        System.out.println("nummber of classfiles: " + numberOfClassfiles);
    }

    public long getNumberOfClassfiles() {
        return numberOfClassfiles;
    }
}
