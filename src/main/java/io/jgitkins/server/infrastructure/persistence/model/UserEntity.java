package io.jgitkins.server.infrastructure.persistence.model;

public class UserEntity {
    private String user;

    private Long currentConnections;

    private Long totalConnections;

    public String getUser() {
        return user;
    }

    public UserEntity withUser(String user) {
        this.setUser(user);
        return this;
    }

    public void setUser(String user) {
        this.user = user == null ? null : user.trim();
    }

    public Long getCurrentConnections() {
        return currentConnections;
    }

    public UserEntity withCurrentConnections(Long currentConnections) {
        this.setCurrentConnections(currentConnections);
        return this;
    }

    public void setCurrentConnections(Long currentConnections) {
        this.currentConnections = currentConnections;
    }

    public Long getTotalConnections() {
        return totalConnections;
    }

    public UserEntity withTotalConnections(Long totalConnections) {
        this.setTotalConnections(totalConnections);
        return this;
    }

    public void setTotalConnections(Long totalConnections) {
        this.totalConnections = totalConnections;
    }
}