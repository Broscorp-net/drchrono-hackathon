package com.remind.me.doc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

  @GetMapping("/")
  public String mainController(){
    return "redirect:https://github.com/Broscorp-net/drchrono-hackathon";
  }
}
