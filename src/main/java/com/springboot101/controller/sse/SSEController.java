package com.springboot101.controller.sse;

import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
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
        for (int i = 0; i < 100; i++) {
            SseEmitterUtils.sendMessage(id, content + i);
        }

    }

    @ResponseBody
    @GetMapping(path = "breakConnect")
    public void breakConnect(String id, HttpServletRequest request, HttpServletResponse response) {
        request.startAsync();
        SseEmitterUtils.removeUser(id);
    }

    int i = 0;

    @GetMapping(path = "/test-sse", produces = "text/event-stream;charset=UTF-8")
    @ResponseBody
    public String testServerSentEvent(HttpServletRequest request) throws Exception {
        Thread.sleep(1);
        //Logic to impl here
        System.out.println(i++);
        return "data:" + LocalTime.now() + "\n\n";
    }

    @RequestMapping("/test")
    public String sseTest() {
        return "sseTest";
    }

    @RequestMapping("/test2")
    public String sseTest2() {
        return "sseTest2";
    }

    /**
     * 测试new SseEmitter(1L)决定的是一次请求响应的超时时间，而不是一次http长连接。
     */
    SseEmitter emitter;   // 实现SSE功能的类
    Integer backEndId = 0; // 记录后端最后发送的数据的id，用于判断客户端是否遗漏数据
    String backEndData = null; // 记录后端最后发送的数据，如果客户端有遗漏数据，则先发送该条数据

    @GetMapping("test-sse2")
    @ResponseBody
    public SseEmitter handle(HttpServletRequest request) throws IOException {
       emitter = new SseEmitter(1L); // 设置后端SSE连接的超时时长为30秒
        // String lastId = request.getHeader("Last-Event-ID"); // 获取客户端收到的最后一个数据的id，用来判断是否有漏收
        // 两种情况意味着漏收数据：
        // ①客户端没收到任何数据，但后端记录的最后发送的数据不为空
        // ②客户端收到过数据，但客户端最后收到的数据id和后端最后发送的数据id不一致
        // if ((lastId == null && backEndData != null) || (lastId != null && !lastId.equals(backEndId.toString()))) {
            // 客户端漏收了数据，后端先将最新的数据返回给客户端
            emitter.send(
                    SseEmitter
                            .event()
                            .data("a")
                            .id(backEndId.toString())
                            .reconnectTime(1)
                        );
        // }
        return emitter;
    }

    @GetMapping("test-sse2-send")
    @ResponseBody
    public void trigger() throws IOException {
        try {
            backEndId++; // 每次发送时id都要+1
            emitter.send(
                    SseEmitter
                            .event()
                            .data("data" + backEndId) // 发送新数据
                            .id(backEndId.toString()) // 发送新id
                            .reconnectTime(1)
                        );
            backEndData = "data" + backEndId; // 更新最新的数据
            System.out.println("id: " + backEndId.toString() + " data: " + backEndData);
        } catch (Exception e) {
            backEndId--;
            System.out.println("id: " + backEndId.toString() + " data: " + backEndData);
            System.out.println(e.getMessage());
        }
    }
}

