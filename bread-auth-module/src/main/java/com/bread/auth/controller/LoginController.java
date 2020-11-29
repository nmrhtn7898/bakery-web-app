package com.bread.auth.controller;

import com.bread.auth.model.AccountDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static java.util.regex.Pattern.matches;

@Controller
public class LoginController {

    /**
     * SSO 사용하게 된다면 사용할 로그인 페이지
     *
     * @param redirectUri 로그인 콜백 URI
     * @param account     현재 세션 정보
     * @param model
     * @return
     */
    @GetMapping("/login")
    public String login(@RequestParam("continue") String redirectUri, @AuthenticationPrincipal AccountDetails account, Model model) {
        if (!matches("^[a-zA-Z]{2,20}://.*$", redirectUri)) { // TODO 정규식 아닌 화이트 리스트 방식으로 변경 필요
            throw new IllegalArgumentException();
        }
        if (account != null) {
            return "redirect:" + redirectUri;
        } else {
            model.addAttribute("continue", redirectUri);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        return "logout";
    }

}
