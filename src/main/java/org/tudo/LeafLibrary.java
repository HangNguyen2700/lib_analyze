package org.tudo;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "leaf_libraries")
public class LeafLibrary {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="group_id")
    private String groupId;

    @Column(name="artifact_id")
    private String artifactId;

    @Column(name="base_version")
    private String baseVersion;

    @Column(name="resolved_ts_version")
    private String resolvedTimestampedVersion;

    @Column(name="classifier") // null = no classifier
    private String classifier;

    @Column(name="link_to_jar")
    private String linkToJar;

//    @Column(name="packaging", nullable=false, length=50)
//    private String packaging = "jar";


//    @Column(name="is_snapshot", nullable=false)
//    private boolean snapshot;

    public LeafLibrary() {
    }

    public LeafLibrary(String groupId, String artifactId, String baseVersion) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.baseVersion = baseVersion;
    }

    public LeafLibrary(Long id, String groupId, String artifactId, String baseVersion, String resolvedTimestampedVersion, String classifier, String linkToJar) {
        this.id = id;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.baseVersion = baseVersion;
        this.resolvedTimestampedVersion = resolvedTimestampedVersion;
        this.classifier = classifier;
        this.linkToJar = linkToJar;
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

    public String getBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(String baseVersion) {
        this.baseVersion = baseVersion;
    }

    public String getResolvedTimestampedVersion() {
        return resolvedTimestampedVersion;
    }

    public void setResolvedTimestampedVersion(String resolvedTimestampedVersion) {
        this.resolvedTimestampedVersion = resolvedTimestampedVersion;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getLinkToJar() {
        return linkToJar;
    }

    public void setLinkToJar(String linkToJar) {
        this.linkToJar = linkToJar;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LeafLibrary)) return false;
        LeafLibrary that = (LeafLibrary) o;
        return Objects.equals(getGroupId(), that.getGroupId()) && Objects.equals(getArtifactId(), that.getArtifactId()) && Objects.equals(getBaseVersion(), that.getBaseVersion()) && Objects.equals(getResolvedTimestampedVersion(), that.getResolvedTimestampedVersion()) && Objects.equals(getClassifier(), that.getClassifier()) && Objects.equals(getLinkToJar(), that.getLinkToJar());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupId(), getArtifactId(), getBaseVersion(), getResolvedTimestampedVersion(), getClassifier(), getLinkToJar());
    }
}
