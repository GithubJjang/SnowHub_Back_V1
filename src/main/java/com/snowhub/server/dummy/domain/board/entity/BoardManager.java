package com.snowhub.server.dummy.domain.board.entity;

import java.util.ArrayList;
import java.util.List;

import com.snowhub.server.dummy.domain.board.model.response.BoardResponse;

public class BoardManager {

	public List<BoardResponse> convertToBoardResponses(List<BoardEntity> boardEntities){

		// 1.

		return new ArrayList<>(boardEntities.stream()
			.map(   // param = Board, return BoadListDTO
				(e) -> BoardResponse.builder()
					.id(e.getId())
					.category(e.getCategory())
					.title(e.getTitle())
					.writer(e.getUser().getDisplayName())
					.count(e.getCount())
					.createDate(e.getCreateDate())
					.build()
			)
			.toList());


	}
}
