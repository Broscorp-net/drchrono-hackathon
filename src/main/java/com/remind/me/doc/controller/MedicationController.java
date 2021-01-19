package com.remind.me.doc.controller;

import com.remind.me.doc.model.Medication;
import com.remind.me.doc.service.MedicationService;
import com.remind.me.doc.service.ReminderShedulerService;
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
public class MedicationController {

  @Autowired
  AuthorizeService authorizeService;
  @Autowired
  MessengerService messengerService;
  @Autowired
  MedicationService medicationService;
  @Autowired
  ReminderShedulerService reminderShedulerService;


  @GetMapping("/medication/{id}/date/{idFacebook}")
  public String addDate(Model model, @PathVariable("idFacebook") String idFacebook, @PathVariable("id") String id) {
    Medication medication = new Medication();
    medication.setId(Long.parseLong(id));
    model.addAttribute("medication", medication);
    model.addAttribute("idFacebook", idFacebook);
    return "date";
  }

  @SneakyThrows
  @PostMapping("/medication/{id}/date/{idFacebook}")
  public String addDate(@ModelAttribute Medication medicationDate, Model model, @PathVariable("idFacebook") String idFacebook, @PathVariable("id") String id) {
    Medication medication = medicationService.getMedication(Long.parseLong(id));

    medication.setReminderTime(medicationDate.getReminderTime());
    medicationService.saveMedication(medication);
    String cron = reminderShedulerService.getCron(medication);
    reminderShedulerService.addTaskToScheduler(Math.toIntExact(medication.getId()),
            () -> messengerService.messageRemindTakePillsWithPillsName(idFacebook, medication), cron);
    messengerService.addSetReminderButton(idFacebook);
    return "redirect:https://www.messenger.com/closeWindow/";
  }
}
