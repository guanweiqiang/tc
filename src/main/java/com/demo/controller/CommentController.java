package com.demo.controller;

import com.demo.advice.BizLog;
import com.demo.pojo.dto.CommentAddDTO;
import com.demo.pojo.dto.CommentReplyDTO;
import com.demo.pojo.Response;
import com.demo.pojo.vo.CommentVO;
import com.demo.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("article/comment")
public class CommentController {

    @Resource
    private CommentService service;

    @PostMapping("add")
    @BizLog("add comment")
    public Response<Void> add(@RequestBody CommentAddDTO commentAddDTO) {
        service.add(commentAddDTO);
        return Response.ok();
    }

    @PostMapping("reply")
    @BizLog("reply comment")
    public Response<Void> reply(@RequestBody CommentReplyDTO commentReplyDTO) {
        service.reply(commentReplyDTO);
        return Response.ok();
    }

    @GetMapping("{id}")
    @BizLog("get root comment")
    public Response<List<CommentVO>> getRoot(@PathVariable("id") Long articleId) {
        List<CommentVO> commentVOS = service.getRootComment(articleId);

        return Response.ok(commentVOS);
    }

    @GetMapping("get/{id}")
    @BizLog("get secondary comment ")
    public Response<List<CommentVO>> get(@PathVariable("id") Long rootId) {
        List<CommentVO> secondaryComment = service.getSecondaryComment(rootId);

        return Response.ok(secondaryComment);
    }

}
