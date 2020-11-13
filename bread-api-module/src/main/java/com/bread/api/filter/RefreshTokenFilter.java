package com.bread.api.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

@Component
public class RefreshTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
/*        Collection<String> headers = response.getHeaders(SET_COOKIE);
        boolean isFirst;
        for (String header : headers) {
            if (i == 0) {
                response.setHeader(SET_COOKIE, format("%s; Secure; $s"), header, "SameSite=" + );
            }
        }*/
    }
}
