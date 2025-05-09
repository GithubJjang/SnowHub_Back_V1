package com.snowhub.server.dummy.domain.tmpboard.infrastructure;

import java.util.List;

import org.springframework.data.repository.query.Param;

public interface TmpBoardRepository {

	// 기본적인 CRUD
	void save(TmpBoard tmpBoard);
	TmpBoard findById(int id);


	// 1. 사용자가 board/write를 조회시, 임시 게시글이 있다면, 가져온다.
	TmpBoard findLatestByUserId(@Param("userId") int userId);

	// 2. 최신순으로 정렬한 후, 상위 10건을 가져온다.
	List<Object[]> findTop10ByOrderByIdDesc(int userId);

	// 3. 2에 이어서 커서기반으로 가져오기 위한 메소드.
	List<Object[]> findNext10ByIdLessThan(int userId, Integer lastId);



}
