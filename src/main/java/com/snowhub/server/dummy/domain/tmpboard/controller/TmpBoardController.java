package com.snowhub.server.dummy.domain.tmpboard.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.snowhub.server.dummy.common.response.ApiResult;
import com.snowhub.server.dummy.domain.tmpboard.infrastructure.TmpBoard;
import com.snowhub.server.dummy.domain.tmpboard.model.request.TmpBoardRequest;
import com.snowhub.server.dummy.domain.tmpboard.service.TmpBoardService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class TmpBoardController {

	private final TmpBoardService tmpBoardService;

	// Create
	// 1. 게시글 임시 저장하기
	@Operation(summary = "게시글 임시저장",description = "현재 작성중인 게시글을 임시저장합니다.")
	@PostMapping("/api/v1/tmpboard") // OK
	public ApiResult<Object> saveTmpBoard(
		@RequestBody TmpBoardRequest tmpBoardRequest,
		HttpServletRequest request){
		return tmpBoardService.save(tmpBoardRequest,request);
	}

	// Read
	// 2. 임시 저장된 게시글 1건 불러오기(최신)
	@Operation(summary = "임시저장 게시글 불러오기",description = "임시저장한 게시글을 불러옵니다.")
	@GetMapping("/api/v1/tmpboard") // OK
	public ApiResult<TmpBoard> getTmpBoard(
		HttpServletRequest request){
		return tmpBoardService.getTmpBoard(request);
	}

	// 3. 임시 게시글 1건 가져오기( 임시게시글 id기반 )
	@GetMapping("/api/v1/tmpboard/{boardId}")
	public ResponseEntity<Object> getMyTmpBoard( // OK
		@Valid @PathVariable("boardId") int boardId
	){
		return tmpBoardService.getMyTmpBoard(boardId);
	}


	// 4. 내가 작성한 임시 게시글 여러개 가져오기(커서기반)
	@GetMapping("/api/v1/tmpboards")
	public ResponseEntity<Object> getMyTmpBoards( // OK
		@RequestParam(name = "cursor",required = false ) Integer cursorId,
		@AuthenticationPrincipal UserDetails principal){

		String username = principal.getUsername();// "1"
		int userId = Integer.valueOf(username);

		return ResponseEntity.ok(tmpBoardService.getMyTmpBoards(userId,cursorId));
	}

	// Update
	// 내가 작성한 임시 게시글 업데이트하기
	@PutMapping("/api/v1/tmpboard")
	public ResponseEntity<Object> updateMyTmpBoard( // OK
		@Valid @RequestBody TmpBoardRequest tmpBoardRequest
	){
		return ResponseEntity.ok(tmpBoardService.updateMyTmpBoards(tmpBoardRequest));
	}

	// 4. 임시 게시글 삭제하기
	@DeleteMapping("/api/v1/tmpboard/{boardId}")
	public ResponseEntity<Object> deleteMyTmpBoards( // OK
		@PathVariable("boardId") int boardId,
		@AuthenticationPrincipal UserDetails principal){
		return ResponseEntity.ok(tmpBoardService.deleteTmpBoard(boardId));
	}


}
