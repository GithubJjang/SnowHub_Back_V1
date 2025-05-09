package com.snowhub.server.dummy.domain.tmpboard.infrastructure;

import java.util.List;

import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Repository
public class TmpBoardRepositoryImpl implements TmpBoardRepository {

	private final TmpBoardJpaRepository tmpBoardJpaRepository;

	// 기본적인 CRUD
	@Override
	public void save(TmpBoard tmpBoard) {
		tmpBoardJpaRepository.save(tmpBoard);
	}

	@Override
	public TmpBoard findById(int id) {
		return tmpBoardJpaRepository.findById(id)
			.orElseThrow(()->new RuntimeException("There is no TmpBoard : "+id));
	}

	// 커스텀 기능
	@Override
	public TmpBoard findLatestByUserId(int userId) {
		return tmpBoardJpaRepository.findLatestByUserId(userId);
	}

	@Override
	public List<Object[]> findTop10ByOrderByIdDesc(int userId) {
		return tmpBoardJpaRepository.findTop10ByOrderByIdDesc(userId);
	}

	@Override
	public List<Object[]> findNext10ByIdLessThan(int userId, Integer lastId) {
		return tmpBoardJpaRepository.findNext10ByIdLessThan(userId,lastId);
	}

}
