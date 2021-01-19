package com.remind.me.doc.service;

import com.remind.me.doc.model.Medication;
import com.remind.me.doc.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ResultsService {

  @Autowired
  PatientService patientService;
  @Autowired
  MessengerService messengerService;

  @Value("${progressMessage}")
  private String progressMessage;

  public void sendResults(String senderId) {
    Patient patient = patientService.getPatient(senderId);
    String message = String.format(progressMessage, patient.getFirstName(), 75, 25);
    messengerService.sendTextMessageWithMetadata(senderId, message, "SEND_RESULTS");
  }

  @Scheduled(cron = "0 0 22 * * *")
  public void sendWeeklyResults() {
    for (Patient patient : patientService.listPatient()) {
      messengerService.sendTextMessageWithMetadata(patient.getIdFacebook(),
              messageAboutResults(patient), "SEND_RESULTS");
    }
  }

  private String messageAboutResults(Patient patient) {
    int takepills = 0;
    int all = patient.getMedications().size() * 7;
    for (Medication medication : patient.getMedications()) {
      takepills += medication.getTakingPills();
    }
    int percentTakes = (int) ((takepills * 1.0 / all) * 100);
    String message = String.format(progressMessage, patient.getFirstName(), percentTakes, 100 - percentTakes);
    return message;
  }
}
