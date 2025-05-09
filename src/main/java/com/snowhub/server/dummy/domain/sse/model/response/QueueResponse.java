package com.snowhub.server.dummy.domain.sse.model.response;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

@Getter
@Setter
public class QueueResponse {
	private List<String> myQueue;
	public QueueResponse(List<String> myQueue){
		this.myQueue = myQueue;
	}

}

