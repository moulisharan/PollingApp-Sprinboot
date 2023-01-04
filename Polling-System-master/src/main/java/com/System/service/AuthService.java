package com.System.service;

import com.System.entity.Role;
import com.System.entity.RoleName;
import com.System.entity.User;
import com.System.payload.SignInRequest;
import com.System.payload.SignInResponse;
import com.System.payload.SignUpRequest;
import com.System.payload.SignUpResponse;
import com.System.repository.RoleRepository;
import com.System.repository.UserRepository;
import com.System.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@SuppressWarnings("ALL")
@Service
public class AuthService {

    private static final Logger logger;

    static {
        logger = LoggerFactory.getLogger(AuthService.class);
    }

    @Autowired
    UserRepository userRepository;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    JwtTokenProvider tokenProvider;
    @Autowired
    AuthenticationManager authenticationManager;

    /**
     * User registration or SignUp
     *
     * @param signUpRequest - SignUp with credentials
     * @return String message
     */
    public ResponseEntity<SignInResponse> registerUser(SignUpRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername()))
            return new ResponseEntity(new SignUpResponse(false, "Username is already taken"), HttpStatus.BAD_REQUEST);

        if (userRepository.existsByEmail(signUpRequest.getEmail()))
            return new ResponseEntity(new SignUpResponse(false, "Email is already taken"), HttpStatus.BAD_REQUEST);

        User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if(userRepository.count()==0) {
            Role userRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new NullPointerException("User Role not set."));
            user.setRoles(Collections.singleton(userRole));
            userRepository.save(user);
        }
        else {
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                    .orElseThrow(() -> new NullPointerException("User Role not set."));
            user.setRoles(Collections.singleton(userRole));
            userRepository.save(user);
        }

        return new ResponseEntity(new SignUpResponse(true, "User registered successfully"), HttpStatus.ACCEPTED);
    }

    /**
     * User Authentication or SignIn
     *
     * @param signInRequest - SignIn with Credentials
     * @return JWT Token,String Message
     */
    public ResponseEntity<SignInResponse> userAuthentication(SignInRequest signInRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.getUsernameOrEmail(),
                        signInRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new SignInResponse(jwt));
    }
}
