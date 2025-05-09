package com.snowhub.server.dummy.repository.custom;

import static com.snowhub.server.dummy.domain.board.infrastructure.QBoard.*;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.snowhub.server.dummy.domain.board.infrastructure.Board;

import jakarta.persistence.EntityManager;

import java.util.List;



public class BoardRepoImpl implements BoardRepoCustom {

    /*
    private JPAQueryFactory queryFactory;

    public BoardRepoImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<Board> pagination(String category,int page) {
        List<Integer> subQuery = queryFactory
                .select(board.id)
                .from(board)
                .where(categoryuEq(category))
                // 메서드가 null 반환 = all, 아니면 category타입에 맞게 반환
                .orderBy(board.id.desc())
                .offset(16L *page) // OK
                .limit(16)// OK
                .fetch()
                ;

        return queryFactory
                .select(board)
                .from(board)
                .where(board.id.in(subQuery)) // NL조인
                .orderBy(board.id.desc())
                .fetch();
    }

    private BooleanExpression categoryuEq(String categoryCond) {

        return categoryCond.equals("all") ? null : board.category.eq(categoryCond);
    }

     */
}
