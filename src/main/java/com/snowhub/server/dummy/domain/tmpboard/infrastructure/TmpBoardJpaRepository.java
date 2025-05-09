package com.snowhub.server.dummy.domain.tmpboard.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TmpBoardJpaRepository extends JpaRepository<TmpBoard,Integer> {

	// 1. 사용자가 board/write를 조회시, 임시 게시글이 있다면, 가져온다.
	@Query(value = "SELECT * FROM tmpboard WHERE user_id = :userId ORDER BY id DESC LIMIT 1", nativeQuery = true)
	TmpBoard findLatestByUserId(@Param("userId") int userId);

	@Query(value = "SELECT id, title, content, category,  state, createDate FROM tmpboard WHERE user_id = :userId ORDER BY id DESC LIMIT 10", nativeQuery = true)
	List<Object[]> findTop10ByOrderByIdDesc(@Param("userId") int userId);

	@Query(value = "SELECT id, category, title, content, state, createDate FROM tmpboard WHERE id < :lastId AND user_id = :userId ORDER BY id DESC LIMIT 10", nativeQuery = true)
	List<Object[]> findNext10ByIdLessThan(@Param("userId") int userId, @Param("lastId") Integer lastId);

}
