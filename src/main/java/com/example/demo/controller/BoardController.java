package com.example.demo.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Board;
import com.example.demo.entity.BoardMember;
import com.example.demo.dto.AddMemberRequest;
import com.example.demo.service.BoardService;

@RestController
@RequestMapping("/api/boards") // All Board APIs start with this URL
public class BoardController {

    private final BoardService boardService;

    // Constructor Injection
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 1. API to Create a New Board
    // URL: POST http://localhost:8080/api/boards
    @PostMapping
    public Board createBoard(@RequestBody Board board) {
        return boardService.createBoard(board);
    }

    // 2. API to Get All Boards the Logged-in User belongs to
    // URL: GET http://localhost:8080/api/boards
    @GetMapping
    public List<Board> getMyBoards() {
        return boardService.getMyBoards();
    }

    // 3. API to Add a Member to a Board
    // URL: POST http://localhost:8080/api/boards/{boardId}/members
    @PostMapping("/{boardId}/members")
    public BoardMember addMember(@PathVariable Long boardId, @RequestBody AddMemberRequest request) {
        return boardService.addMemberToBoard(boardId, request.getEmail(), request.getRole());
    }
    
 // URL: DELETE http://localhost:8080/api/boards/{boardId}/members/{userId}
    @DeleteMapping("/{boardId}/members/{userId}")
    public String removeMember(@PathVariable Long boardId, @PathVariable Long userId) {
        boardService.removeMemberFromBoard(boardId, userId);
        return "Member removed successfully from the board!";
    }
}