package io.jgitkins.server.infrastructure.persistence.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobHistoryEntityCondition {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public JobHistoryEntityCondition() {
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

        public Criteria andJobIdIsNull() {
            addCriterion("job_id is null");
            return (Criteria) this;
        }

        public Criteria andJobIdIsNotNull() {
            addCriterion("job_id is not null");
            return (Criteria) this;
        }

        public Criteria andJobIdEqualTo(Long value) {
            addCriterion("job_id =", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdNotEqualTo(Long value) {
            addCriterion("job_id <>", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdGreaterThan(Long value) {
            addCriterion("job_id >", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdGreaterThanOrEqualTo(Long value) {
            addCriterion("job_id >=", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdLessThan(Long value) {
            addCriterion("job_id <", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdLessThanOrEqualTo(Long value) {
            addCriterion("job_id <=", value, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdIn(List<Long> values) {
            addCriterion("job_id in", values, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdNotIn(List<Long> values) {
            addCriterion("job_id not in", values, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdBetween(Long value1, Long value2) {
            addCriterion("job_id between", value1, value2, "jobId");
            return (Criteria) this;
        }

        public Criteria andJobIdNotBetween(Long value1, Long value2) {
            addCriterion("job_id not between", value1, value2, "jobId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdIsNull() {
            addCriterion("runner_id is null");
            return (Criteria) this;
        }

        public Criteria andRunnerIdIsNotNull() {
            addCriterion("runner_id is not null");
            return (Criteria) this;
        }

        public Criteria andRunnerIdEqualTo(Long value) {
            addCriterion("runner_id =", value, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdNotEqualTo(Long value) {
            addCriterion("runner_id <>", value, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdGreaterThan(Long value) {
            addCriterion("runner_id >", value, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdGreaterThanOrEqualTo(Long value) {
            addCriterion("runner_id >=", value, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdLessThan(Long value) {
            addCriterion("runner_id <", value, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdLessThanOrEqualTo(Long value) {
            addCriterion("runner_id <=", value, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdIn(List<Long> values) {
            addCriterion("runner_id in", values, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdNotIn(List<Long> values) {
            addCriterion("runner_id not in", values, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdBetween(Long value1, Long value2) {
            addCriterion("runner_id between", value1, value2, "runnerId");
            return (Criteria) this;
        }

        public Criteria andRunnerIdNotBetween(Long value1, Long value2) {
            addCriterion("runner_id not between", value1, value2, "runnerId");
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

        public Criteria andLogPathIsNull() {
            addCriterion("log_path is null");
            return (Criteria) this;
        }

        public Criteria andLogPathIsNotNull() {
            addCriterion("log_path is not null");
            return (Criteria) this;
        }

        public Criteria andLogPathEqualTo(String value) {
            addCriterion("log_path =", value, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathNotEqualTo(String value) {
            addCriterion("log_path <>", value, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathGreaterThan(String value) {
            addCriterion("log_path >", value, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathGreaterThanOrEqualTo(String value) {
            addCriterion("log_path >=", value, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathLessThan(String value) {
            addCriterion("log_path <", value, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathLessThanOrEqualTo(String value) {
            addCriterion("log_path <=", value, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathLike(String value) {
            addCriterion("log_path like", value, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathNotLike(String value) {
            addCriterion("log_path not like", value, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathIn(List<String> values) {
            addCriterion("log_path in", values, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathNotIn(List<String> values) {
            addCriterion("log_path not in", values, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathBetween(String value1, String value2) {
            addCriterion("log_path between", value1, value2, "logPath");
            return (Criteria) this;
        }

        public Criteria andLogPathNotBetween(String value1, String value2) {
            addCriterion("log_path not between", value1, value2, "logPath");
            return (Criteria) this;
        }

        public Criteria andStartedAtIsNull() {
            addCriterion("started_at is null");
            return (Criteria) this;
        }

        public Criteria andStartedAtIsNotNull() {
            addCriterion("started_at is not null");
            return (Criteria) this;
        }

        public Criteria andStartedAtEqualTo(LocalDateTime value) {
            addCriterion("started_at =", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtNotEqualTo(LocalDateTime value) {
            addCriterion("started_at <>", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtGreaterThan(LocalDateTime value) {
            addCriterion("started_at >", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("started_at >=", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtLessThan(LocalDateTime value) {
            addCriterion("started_at <", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("started_at <=", value, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtIn(List<LocalDateTime> values) {
            addCriterion("started_at in", values, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtNotIn(List<LocalDateTime> values) {
            addCriterion("started_at not in", values, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("started_at between", value1, value2, "startedAt");
            return (Criteria) this;
        }

        public Criteria andStartedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("started_at not between", value1, value2, "startedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtIsNull() {
            addCriterion("finished_at is null");
            return (Criteria) this;
        }

        public Criteria andFinishedAtIsNotNull() {
            addCriterion("finished_at is not null");
            return (Criteria) this;
        }

        public Criteria andFinishedAtEqualTo(LocalDateTime value) {
            addCriterion("finished_at =", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtNotEqualTo(LocalDateTime value) {
            addCriterion("finished_at <>", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtGreaterThan(LocalDateTime value) {
            addCriterion("finished_at >", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtGreaterThanOrEqualTo(LocalDateTime value) {
            addCriterion("finished_at >=", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtLessThan(LocalDateTime value) {
            addCriterion("finished_at <", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtLessThanOrEqualTo(LocalDateTime value) {
            addCriterion("finished_at <=", value, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtIn(List<LocalDateTime> values) {
            addCriterion("finished_at in", values, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtNotIn(List<LocalDateTime> values) {
            addCriterion("finished_at not in", values, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("finished_at between", value1, value2, "finishedAt");
            return (Criteria) this;
        }

        public Criteria andFinishedAtNotBetween(LocalDateTime value1, LocalDateTime value2) {
            addCriterion("finished_at not between", value1, value2, "finishedAt");
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