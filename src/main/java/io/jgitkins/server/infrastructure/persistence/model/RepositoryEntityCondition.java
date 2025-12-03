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