package com.xuecheng.content.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FreemarkerController {
    @GetMapping(" ")
    public ModelAndView test(){
        return new ModelAndView("test","name"," freemarker");
    }
}
