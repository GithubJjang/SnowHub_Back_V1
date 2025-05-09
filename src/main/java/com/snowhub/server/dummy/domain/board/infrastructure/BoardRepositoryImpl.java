package com.snowhub.server.dummy.domain.board.infrastructure;

import static com.snowhub.server.dummy.domain.board.infrastructure.QBoard.*;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.querydsl.core.types.dsl.BooleanExpression;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class BoardRepositoryImpl implements BoardRepository{
	private final BoardJpaRepository boardJpaRepository;

	// EntityManager에 의존적이기 때문에 직접 생성자 주입을 해야만 한다.
	/*
	private JPAQueryFactory queryFactory;

	public BoardRepositoryImpl(BoardJpaRepository boardJpaRepository, EntityManager em){
		this.boardJpaRepository = boardJpaRepository;
		this.queryFactory = new JPAQueryFactory(em);
	}

	 */

	@Override
	public void save(Board board) {
		boardJpaRepository.save(board);
	}

	@Override
	public Board findById(int boardId) {
		return boardJpaRepository.findById(boardId)
			.orElseThrow(
				()->new RuntimeException("There is no board : "+boardId)
			);
	}

	@Override
	public List<Board> findBoardsPaged(int offset) {
		return boardJpaRepository.findBoardsPaged(16*offset);
	}

	@Override
	public Long countAll() {
		return boardJpaRepository.countAll();
	}

	@Override
	public Long countByCategory(String category) {
		return boardJpaRepository.countByCategory(category);
	}

	@Override
	public Board findSpecificBoard(int userId, int boardId) {
		return boardJpaRepository.findSpecificBoard(userId,boardId);
	}

	@Override
	public List<Board> findTop10ByOrderByIdDesc(int userId) {
		return boardJpaRepository.findTop10ByOrderByIdDesc(userId);
	}

	@Override
	public List<Board> findNext10ByIdLessThan(int userId, Integer lastId) {
		return boardJpaRepository.findNext10ByIdLessThan(userId,lastId);
	}

	private BooleanExpression categoryuEq(String categoryCond) {

		return categoryCond.equals("all") ? null : board.category.eq(categoryCond);
	}
}
