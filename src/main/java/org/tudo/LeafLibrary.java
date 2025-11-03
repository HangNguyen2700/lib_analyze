package org.tudo;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "leaf_libraries",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"group_id", "artifact_id", "version", "maven_central_jar_uri"})
)
public class LeafLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "artifact_id")
    private String artifactId;

    @Column(name = "version")
    private String version;

    @Column(name = "maven_central_jar_uri")
    private String mavenCentralJarUri;

    public LeafLibrary() {
    }

    public LeafLibrary(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public LeafLibrary(String groupId, String artifactId, String version, String mavenCentralJarUri) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.mavenCentralJarUri = mavenCentralJarUri;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeafLibrary)) return false;
        LeafLibrary that = (LeafLibrary) o;
        return Objects.equals(getGroupId(), that.getGroupId()) && Objects.equals(getArtifactId(), that.getArtifactId()) && Objects.equals(getVersion(), that.getVersion()) && Objects.equals(getMavenCentralJarUri(), that.getMavenCentralJarUri());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupId(), getArtifactId(), getVersion(), getMavenCentralJarUri());
    }

    public String getCoordinate() {
        return this.groupId + ":" + this.artifactId + ":" + this.version;
    }
}
