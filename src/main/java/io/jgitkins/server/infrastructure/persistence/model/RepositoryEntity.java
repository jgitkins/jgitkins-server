package io.jgitkins.server.infrastructure.persistence.model;

import java.time.LocalDateTime;

public class RepositoryEntity {
    private Long id;

    private Long organizeId;

    private String name;

    private String path;

    private String defaultBranch;

    private String visibility;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String description;

    public Long getId() {
        return id;
    }

    public RepositoryEntity withId(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizeId() {
        return organizeId;
    }

    public RepositoryEntity withOrganizeId(Long organizeId) {
        this.setOrganizeId(organizeId);
        return this;
    }

    public void setOrganizeId(Long organizeId) {
        this.organizeId = organizeId;
    }

    public String getName() {
        return name;
    }

    public RepositoryEntity withName(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPath() {
        return path;
    }

    public RepositoryEntity withPath(String path) {
        this.setPath(path);
        return this;
    }

    public void setPath(String path) {
        this.path = path == null ? null : path.trim();
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public RepositoryEntity withDefaultBranch(String defaultBranch) {
        this.setDefaultBranch(defaultBranch);
        return this;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch == null ? null : defaultBranch.trim();
    }

    public String getVisibility() {
        return visibility;
    }

    public RepositoryEntity withVisibility(String visibility) {
        this.setVisibility(visibility);
        return this;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility == null ? null : visibility.trim();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public RepositoryEntity withCreatedAt(LocalDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public RepositoryEntity withUpdatedAt(LocalDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public RepositoryEntity withDescription(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }
}