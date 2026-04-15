package com.marketback.main.controller;

import com.marketback.main.entity.Comment;
import com.marketback.main.service.CommentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController extends BaseCrudController<Comment> {

    public CommentController(CommentService commentService) {
        super(commentService, "commentId");
    }
}
