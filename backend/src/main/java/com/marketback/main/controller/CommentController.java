package com.marketback.main.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.marketback.main.common.ApiResponse;
import com.marketback.main.entity.Comment;
import com.marketback.main.service.CommentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController extends BaseCrudController<Comment> {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        super(commentService, "commentId", Comment.class);
        this.commentService = commentService;
    }

    @GetMapping("/good/{goodId}")
    public ApiResponse<List<Comment>> listByGood(@PathVariable Integer goodId) {
        List<Comment> comments = commentService.list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getGoodId, goodId)
                .orderByDesc(Comment::getCreatedAt));
        return ApiResponse.success(comments);
    }
}
