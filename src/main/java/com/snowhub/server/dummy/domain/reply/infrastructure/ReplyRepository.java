package com.snowhub.server.dummy.domain.reply.infrastructure;

import java.util.List;

import com.snowhub.server.dummy.domain.board.infrastructure.Board;

public interface ReplyRepository  {

	void save(Reply reply);
	Reply findById(int replyId);

	List<Reply> findRepliesByBoard(Board board);
}
