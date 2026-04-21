package com.green.eats.auth.application;

import com.green.eats.auth.application.model.*;
import com.green.eats.auth.entity.User;
import com.green.eats.common.auth.UserContext;
import com.green.eats.common.model.JwtUser;
import com.green.eats.common.model.ResultResponse;
import com.green.eats.common.model.UserDto;
import com.green.eats.common.security.JwtTokenManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenManager jwtTokenManager;

    @PostMapping("/signup")
    public ResultResponse<?> signup(@RequestBody UserSignupReq req){
        userService.signup(req);
        return ResultResponse.builder()
                            .resultMessage("회원가입 성공")
                            .resultData(1)
                            .build();
    }

    @PostMapping("/signin")
    public ResultResponse<?> signin (HttpServletResponse res, @RequestBody UserSigninReq req){
        log.info("req: {}", req);
        User signinUser = userService.signin(req);

        //인증쿠키
        JwtUser jwtUser  = new JwtUser( signinUser.getId(), signinUser.getName(), signinUser.getEnumUserRole() );
        jwtTokenManager.issue(res, jwtUser);

        UserSigninRes userSigninRes = UserSigninRes.builder()
                .id(signinUser.getId())
                .name(signinUser.getName())
                .build();

        //리턴
        return ResultResponse.builder()
                .resultMessage("로그인 성공")
                .resultData(userSigninRes)
                .build();

    }

    @PutMapping
    public ResultResponse<?> updUser(@Valid @RequestBody UserPutReq req) {
        UserDto userDto = UserContext.get();
        log.info("userPutReq: {}, userDto: {}", req, userDto);
        userService.updUser(userDto.id(), req);
        return ResultResponse.builder()
                .resultMessage("수정 성공")
                .build();
    }

    @DeleteMapping
    public ResultResponse<?> delUser() {
        UserDto userDto = UserContext.get();
        log.info(" userDto: {}", userDto);
        userService.delUser(userDto.id());
        return ResultResponse.builder()
                .resultMessage("소프트딜리트 성공")
                .build();
    }

    @PostMapping("/signout")
    public ResultResponse<?> signOut(HttpServletResponse res) {
        jwtTokenManager.signOut(res);
        return new ResultResponse<>("로그아웃 성공", 1);
    }

    @PostMapping("/reissue")
    public ResultResponse<?> reissue(HttpServletResponse res, HttpServletRequest req) {
        jwtTokenManager.reissue(req, res);
        return new ResultResponse<>("AT 재발행", null);
    }

}
