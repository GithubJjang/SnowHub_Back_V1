package com.snowhub.server.dummy.domain.board.infrastructure;

import java.util.List;

import com.snowhub.server.dummy.domain.board.infrastructure.Board;
import com.snowhub.server.dummy.repository.custom.BoardRepoCustom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardJpaRepository extends JpaRepository<Board,Integer> {

    @Query(value = "SELECT COUNT(id) FROM board", nativeQuery = true)
    Long countAll();

    // 게시글 페이지네이션
    /*
    @Query(value = "SELECT * FROM board ORDER BY id DESC LIMIT 16 OFFSET :offset", nativeQuery = true)
    List<Board> findBoardsPaged(@Param("offset") int offset);

     */

    // 게시글 페이지네이션
    @Query(value = """
    SELECT b.* FROM board b
    INNER JOIN (
        SELECT id FROM board
        ORDER BY id DESC
        LIMIT 16 OFFSET :offset
    ) b_id ON b.id = b_id.id
    """, nativeQuery = true)
    List<Board> findBoardsPaged(@Param("offset") int offset);

    @Query(value = "SELECT COUNT(id) FROM board  WHERE category = :category", nativeQuery = true)
    Long countByCategory(@Param("category") String category);

    // userId와 boardId로 특정 user가 쓴 게시글 1건을 찾는다.
    @Query(value = "SELECT * FROM board WHERE user_id = :userId AND id = :boardId", nativeQuery = true)
    Board findSpecificBoard(@Param("userId") int userId, @Param("boardId") int boardId);

    @Query(value = "SELECT * FROM board WHERE user_id = :userId ORDER BY id DESC LIMIT 10", nativeQuery = true)
    List<Board> findTop10ByOrderByIdDesc(@Param("userId") int userId);

    @Query(value = "SELECT * FROM board WHERE id < :lastId AND user_id = :userId ORDER BY id DESC LIMIT 10", nativeQuery = true)
    List<Board> findNext10ByIdLessThan(@Param("userId") int userId ,@Param("lastId") Integer lastId);

}
