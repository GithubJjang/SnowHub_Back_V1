package com.snowhub.server.dummy.domain.board.infrastructure;

import java.util.List;

import org.springframework.data.repository.query.Param;

public interface BoardRepository {

	// 기본적인 CRUD
	void save(Board board);

	Board findById(int boardId);

	List<Board> findBoardsPaged(@Param("offset") int offset);


	// 커스텀 기능

	Long countAll();

	Long countByCategory(@Param("category") String category);

	//List<Board> pagination(String category,int page);

	Board findSpecificBoard(int userId, int boardId);

	List<Board> findTop10ByOrderByIdDesc(int userId);

	List<Board> findNext10ByIdLessThan(int userId,Integer lastId);


}
