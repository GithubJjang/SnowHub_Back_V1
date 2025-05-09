package com.snowhub.server.dummy.domain.board.controller;

import com.snowhub.server.dummy.common.response.ApiResult;
import com.snowhub.server.dummy.domain.board.model.request.BoardRequest;
import com.snowhub.server.dummy.domain.board.model.request.BoardUpdateDto;

import com.snowhub.server.dummy.domain.board.service.BoardService;
import com.google.gson.Gson;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor // <- JPAQueryFactory를 빈등록이 된 줄 알고 가져오려고 함. 그래서 어노테이션이 아닌 생성자 주입방식을 적용. @AllArgsConstructor는 모든 필드에 생성자 주입 지양
@RestController
@Tag(name = "게시글 관련 API", description = "게시글 작성/현재 작성중인 게시글 임시저장 및 불러오기/전체게시글 불러오기/게시글 상세보기/")
public class BoardController {

    // 생성자 주입 방식.
    private final BoardService boardService;

    @Operation(summary = "게시글 작성", description = "현재 게시글을 작성합니다.")
    @PostMapping("/api/v1/board/write") // OK
    public ApiResult<Object> writeBoard(
        @Valid @RequestBody BoardRequest boardDTO,
        HttpServletRequest request) {
        return boardService.save(boardDTO,request);
    }


    // - 게시글 불러오기
    @Operation(summary = "전체 게시글 불러오기",description = "전체 게시글을 불러옵니다.")
    @GetMapping("/api/v1/board/list")
    public ResponseEntity<Object> getBoards( // OK
        @RequestParam(name = "page") int page,
        @RequestParam(name = "category") String category){

        String response = boardService.getBoardsByPage(category, page)
            .map(boardList -> {
				return new Gson().toJson(boardList);              // ResponseEntity<String> 으로 반환
            }).block();

        return ResponseEntity.ok(response);

    }

    // /board/detail/{id} <- @PathVariable String id로 받기
    @Operation(summary = "게시글 상세보기",description = "하나의 게시글을 선택하여, 자세한 내용을 불러옵니다.( 게시글+댓글 )")
    @GetMapping("/api/v1/board/detail")
    public ResponseEntity<Object> getBoard( // OK
        @RequestParam(name = "id") int boardId,
        HttpServletRequest request
    ){
        return ResponseEntity.ok(boardService.getBoard(boardId,request));// body T에는 board Entity가 들어가야.
    }

    @DeleteMapping("/api/v1/board/{boardId}") // OK
    public ApiResult<Object> deletePost(
        @PathVariable("boardId") int boardId,
        @AuthenticationPrincipal UserDetails principal) {
        String userId = principal.getUsername();// "1"

        return boardService.deleteBoard(userId,boardId);


    }

    // 내가 작성한 게시글 여러개 가져오기
    @GetMapping("/api/v1/my/boards") // OK
    public ResponseEntity<Object> getMyBoards(
        @AuthenticationPrincipal UserDetails principal,
        @RequestParam(name = "cursor",required = false ) Integer cursorId){

        String username = principal.getUsername();// "1"
        int userId = Integer.valueOf(username);

        return ResponseEntity.ok(boardService.getMyBoards(userId,cursorId));
    }

    // 내가 작성한 게시글 불러오기 // OK
    @GetMapping("/api/v1/my/board")
    public ResponseEntity<Object> getMyBoard(@RequestParam(name = "id",required = true ) Integer boardId){
        return ResponseEntity.ok(boardService.updateMyBoard(boardId));
    }

    // 내가 작성한 게시글 업데이트하기 // OK
    @PutMapping("/api/v1/my/update/board")
    public ResponseEntity<Object> updateMyBoard(
        @Valid @RequestBody BoardUpdateDto boardUpdateDto
   ){
        return ResponseEntity.ok(boardService.dirtyChecking(boardUpdateDto));
    }

}