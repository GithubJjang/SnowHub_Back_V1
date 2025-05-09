package com.snowhub.server.dummy.domain.tmpboard.entity;



import java.sql.Timestamp;
import java.util.List;

import com.snowhub.server.dummy.common.condition.State;
import com.snowhub.server.dummy.domain.tmpboard.infrastructure.TmpBoard;
import com.snowhub.server.dummy.domain.tmpboard.model.response.TmpBoardResponse;

public class TmpBoardMananger {

	// 1. 여러건의 TmpBoards -> 여러건의 TmpBoardReponse로 바꾸기
	public List<TmpBoardResponse> converToDto(List<Object[]> tmpBoards){

		return tmpBoards.stream()
			.map(
				e-> {
					String state = (String)e[4];

					return TmpBoardResponse.builder()
						.id((Integer)e[0])
						.title((String)e[1])
						.content((String)e[2])
						.category((String)e[3])
						.state(State.valueOf(state))
						.createDate((Timestamp)e[5])
						.build();
				}
				).toList();

	}

	// 2. TmpBoards 1건 -> TmpBoardReponse로 바꾸기
	public TmpBoardResponse converToDto(TmpBoard tmpBoard){
		return TmpBoardResponse.builder()
			.id(tmpBoard.getId())
			.title(tmpBoard.getTitle())
			.content(tmpBoard.getContent())
			.category(tmpBoard.getCategory())
			.state(tmpBoard.getState())
			.createDate(tmpBoard.getCreateDate())
			.build()
			;

	}
}
