package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;
    private final ActivityLogService logService; 
    private final NotificationService notificationService;

    // Constructor Injection
    public BoardService(BoardRepository boardRepository, 
            BoardMemberRepository boardMemberRepository, 
            UserRepository userRepository,
            ActivityLogService logService,
            NotificationService notificationService) { // 2. Add to constructor
this.boardRepository = boardRepository;
this.boardMemberRepository = boardMemberRepository;
this.userRepository = userRepository;
this.logService = logService;
this.notificationService = notificationService;
}

    // 1. Helper Method: Get the current logged-in user from JWT
    private User getAuthenticatedUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // 2. Create Board Logic
    @Transactional // Ensures both Board and BoardMember are saved, or neither is.
    public Board createBoard(Board board) {
        User currentUser = getAuthenticatedUser();

        // Set the creator
        board.setCreatedBy(currentUser);
        
        // Save the board first
        Board savedBoard = boardRepository.save(board);

        // Automatically add creator as OWNER in board_members table
        BoardMember membership = new BoardMember();
        membership.setBoard(savedBoard);
        membership.setUser(currentUser);
        membership.setRole(BoardRole.OWNER); // Use the Enum we created
        
        boardMemberRepository.save(membership);
        logService.logActivity(currentUser, "CREATE_BOARD", "BOARD", 
                savedBoard.getId(), "Board Name: " + savedBoard.getName());

        return savedBoard;
    }

    // 3. Get all boards the user is a member of
    public List<Board> getMyBoards() {
        User currentUser = getAuthenticatedUser();
        
        // Find memberships and extract the boards from them
        return boardMemberRepository.findByUser(currentUser)
                .stream()
                .map(BoardMember::getBoard)
                .collect(Collectors.toList());
    }
    
 // Add this method to BoardService.java

    @Transactional
    public BoardMember addMemberToBoard(Long boardId, String targetEmail, BoardRole role) {
        // 1. Identify the person making the request (from JWT)
        User requester = getAuthenticatedUser();

        // 2. Find the Board
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        // 3. PERMISSION CHECK: Is the requester the OWNER?
        BoardMember requesterMembership = boardMemberRepository.findByBoardAndUser(board, requester)
                .orElseThrow(() -> new RuntimeException("You are not a member of this board"));

        if (requesterMembership.getRole() != BoardRole.OWNER) {
            throw new RuntimeException("Only the Board OWNER can add members!");
        }

        // 4. TARGET USER CHECK: Does the user being added exist?
        User targetUser = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new RuntimeException("User with email " + targetEmail + " not found"));

        // 5. DUPLICATE CHECK: Is the user already in the board?
        if (boardMemberRepository.existsByBoardAndUser(board, targetUser)) {
            throw new RuntimeException("This user is already a member of this board");
        }

        // ============================================================
        // STEP 6: ADD YOUR SNIPPET HERE
        // ============================================================
        BoardMember newMember = new BoardMember();
        newMember.setBoard(board);
        newMember.setUser(targetUser);
        newMember.setRole(role);
        
        // Save the membership to the database
        BoardMember savedMember = boardMemberRepository.save(newMember);
        
     // RULE: Create Notification for the new member
        notificationService.createNotification(
            targetUser, 
            "You have been added as a " + role + " to the board: " + board.getName()
        );

        // 7. RECORD ACTIVITY LOG (Using our new service)
        logService.logActivity(
            requester, 
            "ADD_MEMBER", 
            "BOARD", 
            boardId, 
            "Added user: " + targetEmail + " as " + role
        );

        return savedMember; // Return the saved object
    }
    
 // Add this method to BoardService.java

    @Transactional
    public void removeMemberFromBoard(Long boardId, Long userIdToRemove) {
        // 1. Identify the person making the request (from JWT)
        User requester = getAuthenticatedUser();

        // 2. Find the Board
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));

        // 3. Find the person to be removed
        User targetUser = userRepository.findById(userIdToRemove)
                .orElseThrow(() -> new RuntimeException("User to remove not found"));

        // 4. RULE 1: Only the OWNER can remove members
        BoardMember requesterMembership = boardMemberRepository.findByBoardAndUser(board, requester)
                .orElseThrow(() -> new RuntimeException("You are not a member of this board"));

        if (requesterMembership.getRole() != BoardRole.OWNER) {
            throw new RuntimeException("Only the Board OWNER can remove members!");
        }

        // 5. RULE 2: OWNER cannot remove themselves
        if (requester.getId().equals(userIdToRemove)) {
            throw new RuntimeException("You are the owner. You cannot remove yourself from the board!");
        }

        // 6. Find the target user's membership record
        BoardMember targetMembership = boardMemberRepository.findByBoardAndUser(board, targetUser)
                .orElseThrow(() -> new RuntimeException("This user is not a member of this board"));

        // 7. DELETE the membership record
        boardMemberRepository.delete(targetMembership);
    }
}