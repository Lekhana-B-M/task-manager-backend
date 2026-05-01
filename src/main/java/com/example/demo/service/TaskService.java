package com.example.demo.service;

import com.example.demo.dto.UpdateTaskRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.example.demo.specification.TaskSpecification;
import org.springframework.data.jpa.domain.Specification;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ActivityLogService logService;
    private final NotificationService notificationService;

    // CORRECTED CONSTRUCTOR: Included activityLogRepository as a parameter
    public TaskService(TaskRepository taskRepository, 
                       BoardRepository boardRepository, 
                       BoardMemberRepository boardMemberRepository, 
                       UserRepository userRepository,
                       ActivityLogRepository activityLogRepository,
                       ActivityLogService logService,
                       NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.boardRepository = boardRepository;
        this.boardMemberRepository = boardMemberRepository;
        this.userRepository = userRepository;
        this.activityLogRepository = activityLogRepository;
        this.logService = logService;
        this.notificationService = notificationService;
    }

    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public Task createTask(Long boardId, Task task) {
        User currentUser = getAuthenticatedUser();

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found with ID: " + boardId));

        boolean isMember = boardMemberRepository.existsByBoardAndUser(board, currentUser);
        if (!isMember) {
            throw new RuntimeException("Access Denied: You must be a member of this board to create tasks.");
        }

        task.setBoard(board);      
        task.setCreatedBy(currentUser); 

        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        
        if (task.getPriority() == null) {
            task.setPriority(TaskPriority.MEDIUM);
        }

        // CORRECTED: Capture the result in savedTask before logging
        Task savedTask = taskRepository.save(task);
        
     // 3. USE LOG SERVICE (Much cleaner!)
        logService.logActivity(getAuthenticatedUser(), "CREATE_TASK", "TASK", 
                savedTask.getId(), "Title: " + savedTask.getTitle());
        
        // Log the activity
        ActivityLog log = new ActivityLog();
        log.setUser(currentUser);
        log.setAction("CREATED");
        log.setEntityType("TASK");
        log.setEntityId(savedTask.getId());
        log.setDetails("Task title: " + savedTask.getTitle());
        activityLogRepository.save(log);

        return savedTask;
    }
    
    @Transactional
    public Task assignTask(Long taskId, Long assigneeId) {
        User assigner = getAuthenticatedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Board board = task.getBoard();

        BoardMember assignerMembership = boardMemberRepository.findByBoardAndUser(board, assigner)
                .orElseThrow(() -> new RuntimeException("You are not a member of this board"));

        boolean isCreator = task.getCreatedBy().getId().equals(assigner.getId());
        boolean isBoardOwner = assignerMembership.getRole() == BoardRole.OWNER;

        if (!isCreator && !isBoardOwner) {
            throw new RuntimeException("Permission Denied: Only the task creator or board owner can assign tasks.");
        }

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new RuntimeException("Assignee user not found"));

        if (!boardMemberRepository.existsByBoardAndUser(board, assignee)) {
            throw new RuntimeException("Validation Error: This user is not a member of this board.");
        }

        task.setAssignedTo(assignee);
        Task updatedTask = taskRepository.save(task);
        
     // RULE: Create Notification for the assignee
        notificationService.createNotification(
            assignee, 
            "New Task Assigned: '" + updatedTask.getTitle() + "' on board " + updatedTask.getBoard().getName()
        );

        // LOGGING THE ASSIGNMENT
        ActivityLog log = new ActivityLog();
        log.setUser(assigner);
        log.setAction("ASSIGNED");
        log.setEntityType("TASK");
        log.setEntityId(updatedTask.getId());
        log.setDetails("Assigned to: " + assignee.getName());
        activityLogRepository.save(log);
        
     // Send alert to the person who was just assigned the task
        notificationService.sendNotification(
            assignee, 
            "You have been assigned a new task: " + task.getTitle()
        );

        return updatedTask;
    }
    
    @Transactional
    public Task updateTaskStatus(Long taskId, TaskStatus newStatus) {
        User currentUser = getAuthenticatedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        if (!boardMemberRepository.existsByBoardAndUser(task.getBoard(), currentUser)) {
            throw new RuntimeException("Access Denied: You are not a member of this board.");
        }

        TaskStatus currentStatus = task.getStatus();

        if (currentStatus == TaskStatus.TODO && newStatus != TaskStatus.IN_PROGRESS) {
            throw new RuntimeException("Invalid Workflow: From TODO, you must move to IN_PROGRESS.");
        } 
        
        if (currentStatus == TaskStatus.IN_PROGRESS && newStatus != TaskStatus.DONE) {
            throw new RuntimeException("Invalid Workflow: From IN_PROGRESS, you must move to DONE.");
        }

        if (currentStatus == TaskStatus.DONE) {
            throw new RuntimeException("Invalid Workflow: Task is already DONE.");
        }

        task.setStatus(newStatus);
        Task updatedTask = taskRepository.save(task);
        
     // 4. USE LOG SERVICE
        logService.logActivity(getAuthenticatedUser(), "STATUS_UPDATE", "TASK", 
                updatedTask.getId(), "New Status: " + newStatus);

        // LOGGING THE STATUS CHANGE
        ActivityLog log = new ActivityLog();
        log.setUser(currentUser);
        log.setAction("STATUS_UPDATE");
        log.setEntityType("TASK");
        log.setEntityId(updatedTask.getId());
        log.setDetails("Moved to " + newStatus);
        activityLogRepository.save(log);

        return updatedTask;
    }
    
    @Transactional
    public Task updateTaskDetails(Long taskId, UpdateTaskRequest request) {
        User currentUser = getAuthenticatedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        Board board = task.getBoard();

        BoardMember membership = boardMemberRepository.findByBoardAndUser(board, currentUser)
                .orElseThrow(() -> new RuntimeException("Access Denied: You are not a member of this board"));

        boolean isCreator = task.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAssignee = task.getAssignedTo() != null && 
                             task.getAssignedTo().getId().equals(currentUser.getId());
        boolean isBoardOwner = membership.getRole() == BoardRole.OWNER;

        if (!isCreator && !isAssignee && !isBoardOwner) {
            throw new RuntimeException("Permission Denied");
        }

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority());

        return taskRepository.save(task);
    }
    
    @Transactional
    public void deleteTask(Long taskId) {
        User currentUser = getAuthenticatedUser();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        
        Board board = task.getBoard();

        BoardMember membership = boardMemberRepository.findByBoardAndUser(board, currentUser)
                .orElseThrow(() -> new RuntimeException("Access Denied"));

        boolean isTaskCreator = task.getCreatedBy().getId().equals(currentUser.getId());
        boolean isBoardOwner = membership.getRole() == BoardRole.OWNER;

        if (!isTaskCreator && !isBoardOwner) {
            throw new RuntimeException("Permission Denied");
        }

        // LOGGING BEFORE DELETING (Because after delete, you can't get ID easily)
        ActivityLog log = new ActivityLog();
        log.setUser(currentUser);
        log.setAction("DELETED");
        log.setEntityType("TASK");
        log.setEntityId(task.getId());
        log.setDetails("Deleted task: " + task.getTitle());
        activityLogRepository.save(log);

        taskRepository.delete(task);
    }

    public Page<Task> getTasksWithFilters(Long boardId, TaskStatus status, TaskPriority priority, Long assigneeId, int page, int size) {
        User user = getAuthenticatedUser();
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        if (!boardMemberRepository.existsByBoardAndUser(board, user)) {
            throw new RuntimeException("Access Denied!");
        }

        Specification<Task> spec = Specification.where(TaskSpecification.belongsToBoard(boardId))
                .and(TaskSpecification.hasStatus(status))
                .and(TaskSpecification.hasPriority(priority))
                .and(TaskSpecification.isAssignedTo(assigneeId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return taskRepository.findAll(spec, pageable);
    }
}