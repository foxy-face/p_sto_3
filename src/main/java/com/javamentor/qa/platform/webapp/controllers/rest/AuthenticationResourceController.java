package com.javamentor.qa.platform.webapp.controllers.rest;

import com.javamentor.qa.platform.dao.abstracts.model.UserDao;
import com.javamentor.qa.platform.models.dto.AuthenticationRequest;
import com.javamentor.qa.platform.models.dto.JwtTokenDto;
import com.javamentor.qa.platform.models.entity.user.User;
import com.javamentor.qa.platform.security.jwt.JwtUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Collection;

@RestController
@RequestMapping("/api")
public class AuthenticationResourceController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDao userDAO;

    public AuthenticationResourceController(JwtUtil jwtUtil, AuthenticationManager authenticationManager, UserDao userDAO) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDAO = userDAO;
    }

    @PostMapping("/auth/token/")
    @ApiOperation("Возвращает строку токена в виде объекта JwtTokenDto, на вход получает объект AuthenticationRequest, который содержит username, password и значение поля isRemember")
    public ResponseEntity<JwtTokenDto> getToken(@RequestBody AuthenticationRequest request)
    {
        JwtTokenDto jwtTokenDTO = new JwtTokenDto();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            User user = (User) authentication.getPrincipal();
            if (request.isRemember()) {
                jwtTokenDTO.setToken(jwtUtil.generateLongToken(user));
            } else {
                jwtTokenDTO.setToken(jwtUtil.generateAccessToken(user));
            }
        }
        catch (BadCredentialsException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Имя или пароль неправильны", exception);
        }
        return new ResponseEntity<>(jwtTokenDTO, HttpStatus.OK);
    }

    @GetMapping("/user/check_auth")
    public ResponseEntity<Void> checkAuthorization() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals("USER")) {
                return ResponseEntity.status(HttpStatus.OK).build();
            }
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/testuser")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> usertest() {
        return new ResponseEntity<>("API USER TEST", HttpStatus.OK);
    }

    @GetMapping("/testadmin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> admintest() {
        return new ResponseEntity<>("API ADMIN TEST", HttpStatus.OK);
    }
}
