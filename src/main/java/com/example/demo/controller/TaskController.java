package com.example.demo.controller;

import com.example.demo.entity.Task;
import com.example.demo.entity.TaskPriority;
import com.example.demo.entity.TaskStatus;
import com.example.demo.service.TaskService;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dto.AssignTaskRequest;
import com.example.demo.dto.UpdateStatusRequest;
import com.example.demo.dto.UpdateTaskRequest;
//import java.util.List;
import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;



@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // URL: POST http://localhost:8080/api/tasks/{boardId}
    @PostMapping("/{boardId}")
    public Task createTask(@PathVariable Long boardId, @RequestBody Task task) {
        return taskService.createTask(boardId, task);
    }
    
 // URL: PUT http://localhost:8080/api/tasks/{taskId}/assign
    @PutMapping("/{taskId}/assign")
    public Task assignTask(@PathVariable Long taskId, @RequestBody AssignTaskRequest request) {
        return taskService.assignTask(taskId, request.getAssigneeId());
    }
    
 // URL: PATCH http://localhost:8080/api/tasks/{taskId}/status
    @PatchMapping("/{taskId}/status")
    public Task updateStatus(@PathVariable Long taskId, @RequestBody UpdateStatusRequest request) {
        return taskService.updateTaskStatus(taskId, request.getNewStatus());
    }
    
 // URL: PUT http://localhost:8080/api/tasks/{taskId}
    @PutMapping("/{taskId}")
    public Task updateTask(@PathVariable Long taskId, @RequestBody UpdateTaskRequest request) {
        return taskService.updateTaskDetails(taskId, request);
    }
    
 // Add this to TaskController.java

 // URL: DELETE http://localhost:8080/api/tasks/{taskId}
 @DeleteMapping("/{taskId}")
 public String deleteTask(@PathVariable Long taskId) {
     taskService.deleteTask(taskId);
     return "Task deleted successfully!";
 }
 
// @GetMapping("/board/{boardId}")
// public List<Task> getTasksByBoard(@PathVariable Long boardId) {
//     return taskService.getTasksByBoard(boardId);
// }
 
// @GetMapping("/board/{boardId}")
// public Page<Task> getTasks(
//         @PathVariable Long boardId,
//         @RequestParam(defaultValue = "0") int page,  // Default to first page
//         @RequestParam(defaultValue = "10") int size  // Default to 10 items per page
// ) {
//     return taskService.getTasksByBoard(boardId, page, size);
// }
// 
 @GetMapping("/board/{boardId}")
 public Page<Task> getTasks(
         @PathVariable Long boardId,
         @RequestParam(required = false) TaskStatus status,
         @RequestParam(required = false) TaskPriority priority,
         @RequestParam(required = false) Long assigneeId,
         @RequestParam(defaultValue = "0") int page,
         @RequestParam(defaultValue = "10") int size
 ) {
     return taskService.getTasksWithFilters(boardId, status, priority, assigneeId, page, size);
 }
}
