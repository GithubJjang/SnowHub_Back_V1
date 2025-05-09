package com.snowhub.server.dummy.domain.reply.infrastructure;

import com.snowhub.server.dummy.domain.board.infrastructure.Board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ReplyJpaRepository extends JpaRepository<Reply,Integer> {

    List<Reply> findAllByBoard(Board board);
}
