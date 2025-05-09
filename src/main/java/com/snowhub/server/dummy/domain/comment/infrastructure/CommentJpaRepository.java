package com.snowhub.server.dummy.domain.comment.infrastructure;

import com.snowhub.server.dummy.domain.reply.infrastructure.Reply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface CommentJpaRepository extends JpaRepository<Comment,Integer> {

    List<Comment> findByReply(Reply reply);
}
