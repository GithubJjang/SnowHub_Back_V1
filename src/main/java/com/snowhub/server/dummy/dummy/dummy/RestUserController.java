package com.snowhub.server.dummy.dummy.dummy;

import com.snowhub.server.dummy.domain.user.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

@Slf4j
//@AllArgsConstructor
//@RestController
public class RestUserController {
    //private final FirebaseAuth firebaseAuth;
    //private final UserService userService;


    // hasRole인 경우 ROLE_ 생략가능.
    // SecurityContextHolder에서 Authentication의 authority를 확인.
    /*
    @PreAuthorize("hasRole('user')")
    @GetMapping("/demo/verify")
    public RedirectView getToken(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader,
                           HttpServletRequest request,
                           HttpServletResponse response
                           ) throws FirebaseAuthException {

            System.out.println("Endpoint:/auth/verify");


        log.info("============ <Redirect to TestA page> ============");
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:3000/test1");
        return redirectView;

    }

     */

    // hasRole인 경우 ROLE_ 생략가능.
    // SecurityContextHolder에서 Authentication의 authority를 확인.
    @PreAuthorize("hasRole('user')")
    @GetMapping("/demo/verify")
    public ResponseEntity<?> getToken(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                      HttpServletRequest request,
                                      HttpServletResponse response
    ) throws FirebaseAuthException {

        log.info("You request "+request.getRequestURI());
        return ResponseEntity.ok("you request "+request.getRequestURI());

    }



    @PreAuthorize("hasRole('admin')")
    @GetMapping("/demo/admin")
    public String getToken2(@RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader) throws FirebaseAuthException {
        return "Testing! Only Admin Access";
    }

    public void checkUpdatedToken(HttpServletRequest request, HttpServletResponse response){
        String updatedAccessToken = (String) request.getAttribute("updatedAccessToken");// request에 담긴 refreshToken 추출.

        if(updatedAccessToken!=null){// refreshToken이 담긴 경우 => accessToken 갱신

            // 새로운 Bearer Token을 전송한다.
            response.addHeader("Authorization",updatedAccessToken);

        }

    }

}
    /* Google까지 같이 로그아웃되서 X
    @GetMapping("/test/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 둘다 상관없음. 서버에서 보내는 것이 아니라(서버는 사용자의 정보가 없어서 안됨.)
        // 클라이언트가 직접 요청하도록 한다(redirect를 통해서 사용자가 아래 엔드포인트로 요청하게끔 한다.)
        //https://accounts.google.com/logout
        //http://accounts.google.com/logout
        response.sendRedirect("https://accounts.google.com/logout");
        return HttpStatus.OK.toString();
    }
    */