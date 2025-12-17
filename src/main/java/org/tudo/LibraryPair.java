package org.tudo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "library_pairs")
public class LibraryPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leaf_coordinates")
    private String leafCoordinates;

    @Column(name = "dependent_coordinates")
    private String dependentCoordinate;

    @Column(name = "leaf_methods")
    private int leafMethods;

    @Column(name = "dependent_methods")
    private int dependentMethods;

    @Column(name = "callgraph_edges")
    private int callGraphEdges;

    @Column(name = "unused_leaf_methods")
    private int unusedLeafMethods;

    @Column(name = "bloated_leaf")
    private boolean bloatedLeaf;

    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;

    public LibraryPair() {
    }

    public LibraryPair(String leafCoordinates, String dependentCoordinate, int leafMethods, int dependentMethods, int callGraphEdges, int unusedLeafMethods, boolean bloatedLeaf) {
        this.leafCoordinates = leafCoordinates;
        this.dependentCoordinate = dependentCoordinate;
        this.leafMethods = leafMethods;
        this.dependentMethods = dependentMethods;
        this.callGraphEdges = callGraphEdges;
        this.unusedLeafMethods = unusedLeafMethods;
        this.bloatedLeaf = bloatedLeaf;
    }

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLeafCoordinates() {
        return leafCoordinates;
    }

    public void setLeafCoordinates(String leafCoordinates) {
        this.leafCoordinates = leafCoordinates;
    }

    public String getDependentCoordinate() {
        return dependentCoordinate;
    }

    public void setDependentCoordinate(String dependentCoordinate) {
        this.dependentCoordinate = dependentCoordinate;
    }

    public int getLeafMethods() {
        return leafMethods;
    }

    public void setLeafMethods(int leafMethods) {
        this.leafMethods = leafMethods;
    }

    public int getDependentMethods() {
        return dependentMethods;
    }

    public void setDependentMethods(int dependentMethods) {
        this.dependentMethods = dependentMethods;
    }

    public int getCallGraphEdges() {
        return callGraphEdges;
    }

    public void setCallGraphEdges(int callGraphEdges) {
        this.callGraphEdges = callGraphEdges;
    }

    public int getUnusedLeafMethods() {
        return unusedLeafMethods;
    }

    public void setUnusedLeafMethods(int unusedLeafMethods) {
        this.unusedLeafMethods = unusedLeafMethods;
    }

    public boolean isBloatedLeaf() {
        return bloatedLeaf;
    }

    public void setBloatedLeaf(boolean bloatedLeaf) {
        this.bloatedLeaf = bloatedLeaf;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }
}
