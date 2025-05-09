package com.snowhub.server.dummy.dummy;

public class Google {
    /*
    private static Google google;
    private CustomUserDetailsService customUserDetailsService;
    private UserService userService;
    private GoogleConfig GoogleConfig;

    private Google(){

    }
    public static Google getInstance(){
        if (google==null){
            google=new Google();
            return google;
        }
        return google;
    }

    // 1. UserDetailService 초기화
    public void setCustomUserDetailsService(CustomUserDetailsService customUserDetailsService){
        this.customUserDetailsService=customUserDetailsService;
    }

    // 2. UserService 초기화
    public void setUserService(UserService userService){
        this.userService=userService;
    }

    // 3. 환경설정 초기화
    public void setEnviromentConfig(GoogleConfig2 GoogleConfig){
        this.GoogleConfig = GoogleConfig;
    }

    public void verifyToken(HttpServletRequest request,
                            HttpServletResponse response,
                            String bearerToken,
                            FilterChain filterChain) throws IOException {
            System.out.println("VerifyTokenFilter/Google");
            boolean isRefreshToken = false;// refresh 토큰인가요???

            RestTemplate rt = SingleRestTemplate.getInstance();
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> googleReq =
                    new HttpEntity<>(payload, headers);

            // if문을 이용해서 refresh토큰/access토큰 케이스르 나눠서 처리해주자.
            if(userService.findRefreshToken(bearerToken)!=null){
                isRefreshToken=true;
            }

            if(isRefreshToken==false){//Google access토큰에 대해서 처리
                try{
                    ResponseEntity<String> googleTokeninfo = rt.exchange(
                            "https://oauth2.googleapis.com/tokeninfo?access_token="+bearerToken,
                            HttpMethod.GET,
                            googleReq,
                            String.class
                    );

                    String values = googleTokeninfo.getBody();
                    JSONObject jsonObject1 = new JSONObject(values);
                    String restTokenTime = (String) jsonObject1.get("expires_in");

                    if(restTokenTime.equals("0")){// accessToken시간이 만료된 경우
                        throw new Exception("토큰 시간 만료");// 수정예정.

                    }

                    // 권한부여하기.
                    UserDetails getUserDetails = org.springframework.security.core.userdetails.User.builder()
                            .username("AAA")
                            .password("123456789")
                            .authorities(new SimpleGrantedAuthority("ROLE_user"))
                            .build();
                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(getUserDetails,"123456789",getUserDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);


                    filterChain.doFilter(request,response);

                }
                catch (Exception e){
                    // accessToken에 문제가 발생한 모든 경우.
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    JSONObject data = new JSONObject();
                    data.put("error","AccessToken의 데이터가 유효하지 않거나, 만료되었습니다.");
                    data.put("status",HttpStatus.BAD_REQUEST.value());

                    response.getWriter().write(data.toString());

                }
            }

            else {//Google refreshToken에 대해서 처리.

                try{
                    String refreshToken;
                    User findUser = null;

                    // DB에서 refreshToken과 일치여부확인
                    findUser = userService.findRefreshToken(bearerToken);
                    refreshToken=findUser.getRefreshToken();

                    // Header설정
                    HttpHeaders headers1 = new HttpHeaders();
                    headers1.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

                    // Payload설정
                    MultiValueMap<String, String> payload1 = new LinkedMultiValueMap<>();
                    payload1.add("client_id", GoogleConfig.getClientId());
                    payload1.add("client_secret", GoogleConfig.getClientSecret());
                    payload1.add("grant_type", "refresh_token");
                    payload1.add("refresh_token", refreshToken);

                    // Header + Payload
                    HttpEntity<MultiValueMap<String, String>> googleReq1 =
                            new HttpEntity<>(payload1, headers1);

                    RestTemplate rt1 = SingleRestTemplate.getInstance();

                    ResponseEntity<String> googleResponse = rt1.exchange(
                            "https://oauth2.googleapis.com/token",
                            HttpMethod.POST,
                            googleReq1,
                            String.class
                    );

                    String val = googleResponse.getBody();
                    JSONObject jsonObject = new JSONObject(val);

                    String accessToken = (String) jsonObject.get("access_token");
                    //int expTime = (Integer) jsonObject.get("expires_in");
                    //String idToken = (String) jsonObject.get("id_token");
                    //String tokenType = (String) jsonObject.get("token_type");

                    // 권한부여하기.
                    UserDetails getUserDetails = org.springframework.security.core.userdetails.User.builder()
                            .username("AAA")
                            .password("123456789")
                            .authorities(new SimpleGrantedAuthority("ROLE_user"))
                            .build();
                    Authentication authentication =
                            new UsernamePasswordAuthenticationToken(getUserDetails,"123456789",getUserDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    // request에 accessToken 실어서 보내기.
                    //request.setAttribute("updatedAccessToken",accessToken);
                    response.addHeader("Authorization",accessToken);
                    response.addHeader("RefreshToken",refreshToken);

                    filterChain.doFilter(request,response);


                }
                catch (Exception e){//refeshToken에 문제가 있는 경우.
                    // accessToken에 문제가 발생한 모든 경우.
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");

                    JSONObject data = new JSONObject();
                    data.put("error","RefreshToken의 데이터가 유효하지 않거나, 만료되었습니다.");
                    data.put("status",HttpStatus.BAD_REQUEST.value());

                    response.getWriter().write(data.toString());
                    //filterChain.doFilter(request,response);

                }

            }



    }

     */

}
