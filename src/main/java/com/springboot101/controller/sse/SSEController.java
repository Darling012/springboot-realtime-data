package com.springboot101.controller.sse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;

@CrossOrigin("*")
@Controller
@RequestMapping("/sse")
public class SSEController {

    /**
     * sse 页面
     */
    @RequestMapping("/index")
    public String sse() {
        return "sse";
    }

    /**
     * sse 订阅消息
     */
    @GetMapping(path = "sub/{id}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @ResponseBody
    public SseEmitter sub(@PathVariable String id) throws IOException {

        return SseEmitterUtils.connect(id);
    }

    /**
     * sse 发布消息
     */
    @GetMapping(path = "push")
    @ResponseBody
    public void push(String id, String content) throws IOException {
        SseEmitterUtils.sendMessage(id, content);
    }

    @ResponseBody
    @GetMapping(path = "breakConnect")
    public void breakConnect(String id, HttpServletRequest request, HttpServletResponse response) {
        request.startAsync();
        SseEmitterUtils.removeUser(id);
    }


    @GetMapping(path = "/test-sse", produces = "text/event-stream;charset=UTF-8")
    @ResponseBody
    public String testServerSentEvent(HttpServletRequest request) throws Exception {
        Thread.sleep(10000);
        //Logic to impl here
        return "data:"+ LocalTime.now() + "\n\n";
    }

    @RequestMapping("/test")
    public String sseTest() {
        return "sseTest";
    }

}

