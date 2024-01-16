package org.gzu.adminbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description TODO
 * @classname TestController
 * @date 1/16/2024 11:47 PM
 * @created by LIONS7
 */

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }
}
