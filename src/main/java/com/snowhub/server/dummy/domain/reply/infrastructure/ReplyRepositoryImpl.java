package com.snowhub.server.dummy.domain.reply.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.snowhub.server.dummy.common.exception.CustomException;
import com.snowhub.server.dummy.common.exception.ErrorCode;
import com.snowhub.server.dummy.domain.board.infrastructure.Board;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class ReplyRepositoryImpl implements ReplyRepository{

	private final ReplyJpaRepository replyJpaRepository;

	// 기본적인 CRUD
	@Override
	public void save(Reply reply) {
		replyJpaRepository.save(reply);

	}

	@Override
	public Reply findById(int id) {
		return replyJpaRepository.findById(id)
			.orElseThrow(
				()-> new CustomException(ErrorCode.REPLY_NOT_FOUND)
			);
	}

	// 커스텀 기능
	
	// 1.board와 관련된 모든 replies 가져오기
	@Override
	public List<Reply> findRepliesByBoard(Board board) {
		return replyJpaRepository.findAllByBoard(board);
	}

	
}
