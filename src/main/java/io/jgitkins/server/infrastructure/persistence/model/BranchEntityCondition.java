package io.jgitkins.server.infrastructure.persistence.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BranchEntityCondition {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public BranchEntityCondition() {
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

        public Criteria andRepositoryIdIsNull() {
            addCriterion("repository_id is null");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdIsNotNull() {
            addCriterion("repository_id is not null");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdEqualTo(Long value) {
            addCriterion("repository_id =", value, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdNotEqualTo(Long value) {
            addCriterion("repository_id <>", value, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdGreaterThan(Long value) {
            addCriterion("repository_id >", value, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdGreaterThanOrEqualTo(Long value) {
            addCriterion("repository_id >=", value, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdLessThan(Long value) {
            addCriterion("repository_id <", value, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdLessThanOrEqualTo(Long value) {
            addCriterion("repository_id <=", value, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdIn(List<Long> values) {
            addCriterion("repository_id in", values, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdNotIn(List<Long> values) {
            addCriterion("repository_id not in", values, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdBetween(Long value1, Long value2) {
            addCriterion("repository_id between", value1, value2, "repositoryId");
            return (Criteria) this;
        }

        public Criteria andRepositoryIdNotBetween(Long value1, Long value2) {
            addCriterion("repository_id not between", value1, value2, "repositoryId");
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

        public Criteria andIsLockedIsNull() {
            addCriterion("is_locked is null");
            return (Criteria) this;
        }

        public Criteria andIsLockedIsNotNull() {
            addCriterion("is_locked is not null");
            return (Criteria) this;
        }

        public Criteria andIsLockedEqualTo(Boolean value) {
            addCriterion("is_locked =", value, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedNotEqualTo(Boolean value) {
            addCriterion("is_locked <>", value, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedGreaterThan(Boolean value) {
            addCriterion("is_locked >", value, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_locked >=", value, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedLessThan(Boolean value) {
            addCriterion("is_locked <", value, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedLessThanOrEqualTo(Boolean value) {
            addCriterion("is_locked <=", value, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedIn(List<Boolean> values) {
            addCriterion("is_locked in", values, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedNotIn(List<Boolean> values) {
            addCriterion("is_locked not in", values, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedBetween(Boolean value1, Boolean value2) {
            addCriterion("is_locked between", value1, value2, "isLocked");
            return (Criteria) this;
        }

        public Criteria andIsLockedNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_locked not between", value1, value2, "isLocked");
            return (Criteria) this;
        }

        public Criteria andLockedByIsNull() {
            addCriterion("locked_by is null");
            return (Criteria) this;
        }

        public Criteria andLockedByIsNotNull() {
            addCriterion("locked_by is not null");
            return (Criteria) this;
        }

        public Criteria andLockedByEqualTo(Long value) {
            addCriterion("locked_by =", value, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByNotEqualTo(Long value) {
            addCriterion("locked_by <>", value, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByGreaterThan(Long value) {
            addCriterion("locked_by >", value, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByGreaterThanOrEqualTo(Long value) {
            addCriterion("locked_by >=", value, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByLessThan(Long value) {
            addCriterion("locked_by <", value, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByLessThanOrEqualTo(Long value) {
            addCriterion("locked_by <=", value, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByIn(List<Long> values) {
            addCriterion("locked_by in", values, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByNotIn(List<Long> values) {
            addCriterion("locked_by not in", values, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByBetween(Long value1, Long value2) {
            addCriterion("locked_by between", value1, value2, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedByNotBetween(Long value1, Long value2) {
            addCriterion("locked_by not between", value1, value2, "lockedBy");
            return (Criteria) this;
        }

        public Criteria andLockedAtIsNull() {
            addCriterion("locked_at is null");
            return (Criteria) this;
        }

        public Criteria andLockedAtIsNotNull() {
            addCriterion("locked_at is not null");
            return (Criteria) this;
        }

        public Criteria andLockedAtEqualTo(LocalDateTime value) {
            addCriterion("locked_at =", value, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtNotEqualTo(LocalDateTime value) {
            addCriterion("locked_at <>", value, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtGreaterThan(LocalDateTime value) {
            addCriterion("locked_at >", value, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("locked_at >=", value, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtLessThan(LocalDateTime value) {
            addCriterion("locked_at <", value, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("locked_at <=", value, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtIn(List<LocalDateTime> values) {
            addCriterion("locked_at in", values, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtNotIn(List<LocalDateTime> values) {
            addCriterion("locked_at not in", values, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("locked_at between", value1, value2, "lockedAt");
            return (Criteria) this;
        }

        public Criteria andLockedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("locked_at not between", value1, value2, "lockedAt");
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