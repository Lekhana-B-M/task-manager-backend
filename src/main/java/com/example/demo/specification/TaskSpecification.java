package com.example.demo.specification;

import com.example.demo.entity.Task;
import com.example.demo.entity.TaskPriority;
import com.example.demo.entity.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join; // If you need joins
import jakarta.persistence.criteria.Predicate;

public class TaskSpecification {

    public static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Task> hasPriority(TaskPriority priority) {
        return (root, query, cb) -> priority == null ? null : cb.equal(root.get("priority"), priority);
    }

    public static Specification<Task> isAssignedTo(Long assigneeId) {
        return (root, query, cb) -> assigneeId == null ? null : cb.equal(root.get("assignedTo").get("id"), assigneeId);
    }

    public static Specification<Task> belongsToBoard(Long boardId) {
        return (root, query, cb) -> cb.equal(root.get("board").get("id"), boardId);
    }
}