package com.System.controller;

import com.System.payload.PollResponse;
import com.System.payload.VoteRequest;
import com.System.security.CurrentUser;
import com.System.security.UserPrincipal;
import com.System.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("auth/poll")
public class UserController {

    @Autowired
    UserService userService;


    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping("/{pollId}/castVote")
    @PreAuthorize("hasRole('USER')")
    public PollResponse castVote(@CurrentUser UserPrincipal currentUser,
                                 @PathVariable Long pollId,
                                 @Valid @RequestBody VoteRequest voteRequest) {
        logger.info("user casting");
        return userService.castVoteAndGetUpdatedPoll(pollId, voteRequest, currentUser);
    }

}
