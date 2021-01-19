package com.remind.me.doc.controller;

import com.remind.me.doc.model.HealthTest;
import com.remind.me.doc.model.Patient;
import com.remind.me.doc.service.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class HealthTestController {

  @Autowired
  AuthorizeService authorizeService;
  @Autowired
  MessengerService messengerService;
  @Autowired
  PatientService patientService;
  @Autowired
  CheckHealthTestService checkHealthTestService;
  @Autowired
  HealthTestService healthTestService;


  @GetMapping("/healthTest/add/{idFacebook}")
  public String addDate(Model model, @PathVariable("idFacebook") String idFacebook) {
    HealthTest healthTest = new HealthTest();
    model.addAttribute("healthTest", healthTest);
    model.addAttribute("idFacebook", idFacebook);
    return "health-test";
  }

  @SneakyThrows
  @PostMapping("/healthTest/add/{idFacebook}")
  public String addDate(@ModelAttribute HealthTest healthTest, Model model, @PathVariable("idFacebook") String idFacebook) {
    Patient patient = patientService.getPatient(idFacebook);
    healthTest.setPatient(patient);
    healthTestService.createHealthTest(healthTest);
    messengerService.sendTextMessageWithMetadata(idFacebook, "Got it", "TEST_CREATED");
    checkHealthTestService.checkHealthTestResults(healthTest);
    return "redirect:https://www.messenger.com/closeWindow/";
  }
}
