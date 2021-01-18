package controller;

import lombok.SneakyThrows;
import model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import service.AuthorizeService;
import service.MessengerService;

@Controller
public class MedicationController {

  @Autowired
  AuthorizeService authorizeService;
  @Autowired
  MessengerService messengerService;


  @GetMapping("/medication/{id}/date/{senderId}")
  public String addDate(Model model, @PathVariable String senderId, @PathVariable String id) {
    Patient patient = new Patient();
    patient.setIdFacebook(senderId);
    model.addAttribute("patient", patient);
    model.addAttribute("medicationId", id);
    return "/date";
  }

  @SneakyThrows
  @PostMapping("/medication/{id}/date/{senderId}")
  public String addDate(@ModelAttribute Patient patient, Model model, @PathVariable String senderId, @PathVariable String id) {
    //  patientService.createPerson(person);
    // patient.setIdFacebook(senderId);
    messengerService.addSetReminderButton(senderId);
    return "redirect:https://www.messenger.com/closeWindow/";
  }
}
