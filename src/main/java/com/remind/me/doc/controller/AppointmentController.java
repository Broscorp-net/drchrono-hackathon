package com.remind.me.doc.controller;

import com.remind.me.doc.model.Appointment;
import com.remind.me.doc.model.Patient;
import com.remind.me.doc.service.AuthorizeService;
import com.remind.me.doc.service.AppointmentService;
import com.remind.me.doc.service.MessengerService;
import com.remind.me.doc.service.PatientService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AppointmentController {

  @Autowired
  AuthorizeService authorizeService;
  @Autowired
  MessengerService messengerService;
  @Autowired
  AppointmentService appointmentService;
  @Autowired
  PatientService patientService;

  @Value("${appointmentCreatedMessage}")
  private String appointmentCreatedMessage;



  @GetMapping("/appointment/{idFacebook}/add")
  public String addDate(Model model, @PathVariable("idFacebook") String idFacebook) {
    Appointment appointment = new Appointment();
    model.addAttribute("appointment", appointment);
    model.addAttribute("idFacebook", idFacebook);
    return "/appointment";
  }

  @SneakyThrows
  @PostMapping("/appointment/{idFacebook}/add")
  public String addDate(@ModelAttribute Appointment appointment, Model model, @PathVariable("idFacebook") String idFacebook) {
    Patient patient = patientService.getPatient(idFacebook);
    appointment.setPatient(patient);
    Appointment appointmentCreated = appointmentService.createAppointment(appointment);
    appointmentService.sendAppoitment(appointmentCreated);
    messengerService.sendTextMessage(idFacebook, appointmentCreatedMessage);
    messengerService.messageMenu(idFacebook);
    return "redirect:https://www.messenger.com/closeWindow/";
  }
}
