package org.tudo;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "libraries",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"group_id", "artifact_id", "version", "maven_central_jar_uri"})
)
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", columnDefinition = "TEXT")
    private String groupId;

    @Column(name = "artifact_id", columnDefinition = "TEXT")
    private String artifactId;

    @Column(name = "version", columnDefinition = "TEXT")
    private String version;

    @Column(name = "maven_central_jar_uri", columnDefinition = "TEXT")
    private String mavenCentralJarUri;

    @Column(name = "is_leaf")
    private boolean isLeaf;

    @Column(name = "dependency_coordinates", columnDefinition = "TEXT")
    private String dependencyCoordinates;

    public Library() {
    }

    public Library(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public Library(String groupId, String artifactId, String version, String mavenCentralJarUri, boolean isLeaf, String dependencyCoordinates) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.mavenCentralJarUri = mavenCentralJarUri;
        this.isLeaf = isLeaf;
        this.dependencyCoordinates = dependencyCoordinates;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getMavenCentralJarUri() {
        return mavenCentralJarUri;
    }

    public void setMavenCentralJarUri(String mavenCentralJarUri) {
        this.mavenCentralJarUri = mavenCentralJarUri;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public String getDependencyCoordinates() {
        return dependencyCoordinates;
    }

    public void setDependencyCoordinates(String dependencyCoordinates) {
        this.dependencyCoordinates = dependencyCoordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return isLeaf() == library.isLeaf() && Objects.equals(getId(), library.getId()) && Objects.equals(getGroupId(), library.getGroupId()) && Objects.equals(getArtifactId(), library.getArtifactId()) && Objects.equals(getVersion(), library.getVersion()) && Objects.equals(getMavenCentralJarUri(), library.getMavenCentralJarUri()) && Objects.equals(getDependencyCoordinates(), library.getDependencyCoordinates());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getGroupId(), getArtifactId(), getVersion(), getMavenCentralJarUri(), isLeaf(), getDependencyCoordinates());
    }

    public String getCoordinate() {
        return this.groupId + ":" + this.artifactId + ":" + this.version;
    }
}


