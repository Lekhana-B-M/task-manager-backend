package com.example.demo.repository;

import com.example.demo.entity.Board;
import com.example.demo.entity.BoardMember;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import java.util.List;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    // 1. Find all "membership" records for a specific user 
    // (Helps list all boards a user is part of)
    List<BoardMember> findByUser(User user);

    // 2. Check if a specific user is already a member of a specific board
    // (Very useful for security checks)
    boolean existsByBoardAndUser(Board board, User user);
    
 // 3. ADD THIS METHOD HERE
    // This allows us to find the specific role (OWNER/MEMBER) for a user on a board
    Optional<BoardMember> findByBoardAndUser(Board board, User user);
}