package com.snowhub.server.dummy.domain.comment.infrastructure;

import java.util.List;

import com.snowhub.server.dummy.domain.reply.infrastructure.Reply;

public interface CommentRepository {

	void save(Comment comment);

	List<Comment>  findByReplyJpaEntity(Reply reply);
}
