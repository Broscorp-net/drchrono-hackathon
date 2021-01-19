package com.remind.me.doc.controller;

import com.remind.me.doc.service.PatientService;
import lombok.SneakyThrows;
import com.remind.me.doc.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import com.remind.me.doc.service.AuthorizeService;
import com.remind.me.doc.service.MessengerService;

@Controller
public class PatientController {

  @Autowired
  AuthorizeService authorizeService;
  @Autowired
  MessengerService messengerService;
  @Autowired
  PatientService patientService;

  @GetMapping("/patient/add/{senderId}")
  public String newPatientForm(Model model, @PathVariable String senderId) {
    Patient patient = new Patient();
    patient.setIdFacebook(senderId);
    model.addAttribute("patient", patient);
    return "/authorize";
  }

  @SneakyThrows
  @PostMapping("/patient/add/{senderId}")
  public String newPatientAdd(@ModelAttribute Patient patient, Model model, @PathVariable String senderId) {

    patient.setIdFacebook(senderId);
    patientService.createPatient(patient);
    String textMessage = String.format("Your first name: %s \n Your last name: %s \n Your ID: %s \n Right?",
            patient.getFirstName(), patient.getLastName(), patient.getChartId());
    messengerService.sendButtonMessageAboutForm(senderId, textMessage);
    return "redirect:https://www.messenger.com/closeWindow/";
  }

}
