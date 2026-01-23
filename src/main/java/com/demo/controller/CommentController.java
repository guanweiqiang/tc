package com.demo.controller;

import com.demo.advice.BizLog;
import com.demo.model.dto.CommentAddDTO;
import com.demo.model.dto.CommentReplyDTO;
import com.demo.pojo.Response;
import com.demo.model.vo.CommentVO;
import com.demo.service.CommentService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("article/comment")
@Validated
public class CommentController {

    @Resource
    private CommentService service;

    @PostMapping("add")
    @BizLog("add comment")
    public Response<Void> add(@Valid @RequestBody CommentAddDTO commentAddDTO) {
        service.add(commentAddDTO);
        return Response.ok();
    }

    @PostMapping("reply")
    @BizLog("reply comment")
    public Response<Void> reply(@Valid @RequestBody CommentReplyDTO commentReplyDTO) {
        service.reply(commentReplyDTO);
        return Response.ok();
    }

    @GetMapping("{id}")
    @BizLog("get root comment")
    public Response<List<CommentVO>> getRoot(
            @PathVariable("id")
            @NotNull(message = "文章id不能为空")
            @Positive(message = "文章id必须为正数") Long articleId) {
        List<CommentVO> commentVOS = service.getRootComment(articleId);

        return Response.ok(commentVOS);
    }

    @GetMapping("get/{id}")
    @BizLog("get secondary comment ")
    public Response<List<CommentVO>> get(
            @PathVariable("id")
            @NotNull(message = "rootId不能为空")
            @Positive(message = "rootId必须为正数") Long rootId) {
        List<CommentVO> secondaryComment = service.getSecondaryComment(rootId);

        return Response.ok(secondaryComment);
    }

}
