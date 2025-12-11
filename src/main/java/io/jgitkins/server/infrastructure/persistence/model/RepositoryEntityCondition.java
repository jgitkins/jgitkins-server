package io.jgitkins.server.infrastructure.persistence.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RepositoryEntityCondition {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RepositoryEntityCondition() {
        oredCriteria = new ArrayList<>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdIsNull() {
            addCriterion("organize_id is null");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdIsNotNull() {
            addCriterion("organize_id is not null");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdEqualTo(Long value) {
            addCriterion("organize_id =", value, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdNotEqualTo(Long value) {
            addCriterion("organize_id <>", value, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdGreaterThan(Long value) {
            addCriterion("organize_id >", value, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdGreaterThanOrEqualTo(Long value) {
            addCriterion("organize_id >=", value, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdLessThan(Long value) {
            addCriterion("organize_id <", value, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdLessThanOrEqualTo(Long value) {
            addCriterion("organize_id <=", value, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdIn(List<Long> values) {
            addCriterion("organize_id in", values, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdNotIn(List<Long> values) {
            addCriterion("organize_id not in", values, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdBetween(Long value1, Long value2) {
            addCriterion("organize_id between", value1, value2, "organizeId");
            return (Criteria) this;
        }

        public Criteria andOrganizeIdNotBetween(Long value1, Long value2) {
            addCriterion("organize_id not between", value1, value2, "organizeId");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("name is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("name is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("name =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("name <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("name >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("name >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("name <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("name <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("name like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("name not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("name in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("name not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("name between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("name not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andPathIsNull() {
            addCriterion("path is null");
            return (Criteria) this;
        }

        public Criteria andPathIsNotNull() {
            addCriterion("path is not null");
            return (Criteria) this;
        }

        public Criteria andPathEqualTo(String value) {
            addCriterion("path =", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathNotEqualTo(String value) {
            addCriterion("path <>", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathGreaterThan(String value) {
            addCriterion("path >", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathGreaterThanOrEqualTo(String value) {
            addCriterion("path >=", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathLessThan(String value) {
            addCriterion("path <", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathLessThanOrEqualTo(String value) {
            addCriterion("path <=", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathLike(String value) {
            addCriterion("path like", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathNotLike(String value) {
            addCriterion("path not like", value, "path");
            return (Criteria) this;
        }

        public Criteria andPathIn(List<String> values) {
            addCriterion("path in", values, "path");
            return (Criteria) this;
        }

        public Criteria andPathNotIn(List<String> values) {
            addCriterion("path not in", values, "path");
            return (Criteria) this;
        }

        public Criteria andPathBetween(String value1, String value2) {
            addCriterion("path between", value1, value2, "path");
            return (Criteria) this;
        }

        public Criteria andPathNotBetween(String value1, String value2) {
            addCriterion("path not between", value1, value2, "path");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeIsNull() {
            addCriterion("repository_type is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeIsNotNull() {
            addCriterion("repository_type is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeEqualTo(String value) {
            addCriterion("repository_type =", value, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeNotEqualTo(String value) {
            addCriterion("repository_type <>", value, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeGreaterThan(String value) {
            addCriterion("repository_type >", value, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeGreaterThanOrEqualTo(String value) {
            addCriterion("repository_type >=", value, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeLessThan(String value) {
            addCriterion("repository_type <", value, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeLessThanOrEqualTo(String value) {
            addCriterion("repository_type <=", value, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeLike(String value) {
            addCriterion("repository_type like", value, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeNotLike(String value) {
            addCriterion("repository_type not like", value, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeIn(List<String> values) {
            addCriterion("repository_type in", values, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeNotIn(List<String> values) {
            addCriterion("repository_type not in", values, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeBetween(String value1, String value2) {
            addCriterion("repository_type between", value1, value2, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andRepositoryTypeNotBetween(String value1, String value2) {
            addCriterion("repository_type not between", value1, value2, "repositoryType");
            return (Criteria) this;
        }

        public Criteria andOwnerIdIsNull() {
            addCriterion("owner_id is null");
            return (Criteria) this;
        }

        public Criteria andOwnerIdIsNotNull() {
            addCriterion("owner_id is not null");
            return (Criteria) this;
        }

        public Criteria andOwnerIdEqualTo(Long value) {
            addCriterion("owner_id =", value, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdNotEqualTo(Long value) {
            addCriterion("owner_id <>", value, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdGreaterThan(Long value) {
            addCriterion("owner_id >", value, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdGreaterThanOrEqualTo(Long value) {
            addCriterion("owner_id >=", value, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdLessThan(Long value) {
            addCriterion("owner_id <", value, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdLessThanOrEqualTo(Long value) {
            addCriterion("owner_id <=", value, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdIn(List<Long> values) {
            addCriterion("owner_id in", values, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdNotIn(List<Long> values) {
            addCriterion("owner_id not in", values, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdBetween(Long value1, Long value2) {
            addCriterion("owner_id between", value1, value2, "ownerId");
            return (Criteria) this;
        }

        public Criteria andOwnerIdNotBetween(Long value1, Long value2) {
            addCriterion("owner_id not between", value1, value2, "ownerId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdIsNull() {
            addCriterion("credential_id is null");
            return (Criteria) this;
        }

        public Criteria andCredentialIdIsNotNull() {
            addCriterion("credential_id is not null");
            return (Criteria) this;
        }

        public Criteria andCredentialIdEqualTo(String value) {
            addCriterion("credential_id =", value, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdNotEqualTo(String value) {
            addCriterion("credential_id <>", value, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdGreaterThan(String value) {
            addCriterion("credential_id >", value, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdGreaterThanOrEqualTo(String value) {
            addCriterion("credential_id >=", value, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdLessThan(String value) {
            addCriterion("credential_id <", value, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdLessThanOrEqualTo(String value) {
            addCriterion("credential_id <=", value, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdLike(String value) {
            addCriterion("credential_id like", value, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdNotLike(String value) {
            addCriterion("credential_id not like", value, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdIn(List<String> values) {
            addCriterion("credential_id in", values, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdNotIn(List<String> values) {
            addCriterion("credential_id not in", values, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdBetween(String value1, String value2) {
            addCriterion("credential_id between", value1, value2, "credentialId");
            return (Criteria) this;
        }

        public Criteria andCredentialIdNotBetween(String value1, String value2) {
            addCriterion("credential_id not between", value1, value2, "credentialId");
            return (Criteria) this;
        }

        public Criteria andClonePathIsNull() {
            addCriterion("clone_path is null");
            return (Criteria) this;
        }

        public Criteria andClonePathIsNotNull() {
            addCriterion("clone_path is not null");
            return (Criteria) this;
        }

        public Criteria andClonePathEqualTo(String value) {
            addCriterion("clone_path =", value, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathNotEqualTo(String value) {
            addCriterion("clone_path <>", value, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathGreaterThan(String value) {
            addCriterion("clone_path >", value, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathGreaterThanOrEqualTo(String value) {
            addCriterion("clone_path >=", value, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathLessThan(String value) {
            addCriterion("clone_path <", value, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathLessThanOrEqualTo(String value) {
            addCriterion("clone_path <=", value, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathLike(String value) {
            addCriterion("clone_path like", value, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathNotLike(String value) {
            addCriterion("clone_path not like", value, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathIn(List<String> values) {
            addCriterion("clone_path in", values, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathNotIn(List<String> values) {
            addCriterion("clone_path not in", values, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathBetween(String value1, String value2) {
            addCriterion("clone_path between", value1, value2, "clonePath");
            return (Criteria) this;
        }

        public Criteria andClonePathNotBetween(String value1, String value2) {
            addCriterion("clone_path not between", value1, value2, "clonePath");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchIsNull() {
            addCriterion("default_branch is null");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchIsNotNull() {
            addCriterion("default_branch is not null");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchEqualTo(String value) {
            addCriterion("default_branch =", value, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchNotEqualTo(String value) {
            addCriterion("default_branch <>", value, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchGreaterThan(String value) {
            addCriterion("default_branch >", value, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchGreaterThanOrEqualTo(String value) {
            addCriterion("default_branch >=", value, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchLessThan(String value) {
            addCriterion("default_branch <", value, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchLessThanOrEqualTo(String value) {
            addCriterion("default_branch <=", value, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchLike(String value) {
            addCriterion("default_branch like", value, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchNotLike(String value) {
            addCriterion("default_branch not like", value, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchIn(List<String> values) {
            addCriterion("default_branch in", values, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchNotIn(List<String> values) {
            addCriterion("default_branch not in", values, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchBetween(String value1, String value2) {
            addCriterion("default_branch between", value1, value2, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andDefaultBranchNotBetween(String value1, String value2) {
            addCriterion("default_branch not between", value1, value2, "defaultBranch");
            return (Criteria) this;
        }

        public Criteria andVisibilityIsNull() {
            addCriterion("visibility is null");
            return (Criteria) this;
        }

        public Criteria andVisibilityIsNotNull() {
            addCriterion("visibility is not null");
            return (Criteria) this;
        }

        public Criteria andVisibilityEqualTo(String value) {
            addCriterion("visibility =", value, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityNotEqualTo(String value) {
            addCriterion("visibility <>", value, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityGreaterThan(String value) {
            addCriterion("visibility >", value, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityGreaterThanOrEqualTo(String value) {
            addCriterion("visibility >=", value, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityLessThan(String value) {
            addCriterion("visibility <", value, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityLessThanOrEqualTo(String value) {
            addCriterion("visibility <=", value, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityLike(String value) {
            addCriterion("visibility like", value, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityNotLike(String value) {
            addCriterion("visibility not like", value, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityIn(List<String> values) {
            addCriterion("visibility in", values, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityNotIn(List<String> values) {
            addCriterion("visibility not in", values, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityBetween(String value1, String value2) {
            addCriterion("visibility between", value1, value2, "visibility");
            return (Criteria) this;
        }

        public Criteria andVisibilityNotBetween(String value1, String value2) {
            addCriterion("visibility not between", value1, value2, "visibility");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtIsNull() {
            addCriterion("last_synced_at is null");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtIsNotNull() {
            addCriterion("last_synced_at is not null");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtEqualTo(LocalDateTime value) {
            addCriterion("last_synced_at =", value, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtNotEqualTo(LocalDateTime value) {
            addCriterion("last_synced_at <>", value, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtGreaterThan(LocalDateTime value) {
            addCriterion("last_synced_at >", value, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("last_synced_at >=", value, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtLessThan(LocalDateTime value) {
            addCriterion("last_synced_at <", value, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("last_synced_at <=", value, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtIn(List<LocalDateTime> values) {
            addCriterion("last_synced_at in", values, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtNotIn(List<LocalDateTime> values) {
            addCriterion("last_synced_at not in", values, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("last_synced_at between", value1, value2, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andLastSyncedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("last_synced_at not between", value1, value2, "lastSyncedAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNull() {
            addCriterion("created_at is null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIsNotNull() {
            addCriterion("created_at is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedAtEqualTo(LocalDateTime value) {
            addCriterion("created_at =", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotEqualTo(LocalDateTime value) {
            addCriterion("created_at <>", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThan(LocalDateTime value) {
            addCriterion("created_at >", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("created_at >=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThan(LocalDateTime value) {
            addCriterion("created_at <", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("created_at <=", value, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtIn(List<LocalDateTime> values) {
            addCriterion("created_at in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotIn(List<LocalDateTime> values) {
            addCriterion("created_at not in", values, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("created_at between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andCreatedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("created_at not between", value1, value2, "createdAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNull() {
            addCriterion("updated_at is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIsNotNull() {
            addCriterion("updated_at is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtEqualTo(LocalDateTime value) {
            addCriterion("updated_at =", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotEqualTo(LocalDateTime value) {
            addCriterion("updated_at <>", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThan(LocalDateTime value) {
            addCriterion("updated_at >", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("updated_at >=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThan(LocalDateTime value) {
            addCriterion("updated_at <", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("updated_at <=", value, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtIn(List<LocalDateTime> values) {
            addCriterion("updated_at in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotIn(List<LocalDateTime> values) {
            addCriterion("updated_at not in", values, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("updated_at between", value1, value2, "updatedAt");
            return (Criteria) this;
        }

        public Criteria andUpdatedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("updated_at not between", value1, value2, "updatedAt");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {
        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}