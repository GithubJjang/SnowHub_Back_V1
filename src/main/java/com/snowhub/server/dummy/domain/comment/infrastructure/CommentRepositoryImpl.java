package com.snowhub.server.dummy.domain.comment.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.snowhub.server.dummy.domain.reply.infrastructure.Reply;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class CommentRepositoryImpl implements CommentRepository{

	private final CommentJpaRepository commentJpaRepository;

	@Override
	public void save(Comment comment) {
		commentJpaRepository.save(comment);
	}

	@Override
	public List<Comment> findByReplyJpaEntity(Reply reply) {
		return commentJpaRepository.findByReply(reply);
	}
}
