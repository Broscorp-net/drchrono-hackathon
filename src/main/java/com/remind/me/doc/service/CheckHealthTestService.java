package com.remind.me.doc.service;

import com.remind.me.doc.model.HealthTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckHealthTestService {

  @Autowired
  private MessengerService messengerService;

  public void checkHealthTestResults(HealthTest healthTest) {
    String[] pressure = healthTest.getBloodPressure().split("/");
    StringBuilder sb = new StringBuilder();
    String idFacebook = healthTest.getPatient().getIdFacebook();
    sb.append(healthTest.getPatient().getFirstName());
    int systolicPressure = Integer.parseInt(pressure[0]);
    int diastolicPressure = Integer.parseInt(pressure[1]);

    boolean sendToDoctor = false;
    if (systolicPressure >= 140 || diastolicPressure >= 90) {
      sb.append(",I've noticed that you have a high blood pressure.");
      sendToDoctor = true;
    } else if (healthTest.getPulse() > 90) {
      sb.append(",I've noticed that you have a high pulse.");
      sendToDoctor = true;
    } else if (healthTest.getTemperature() > 37) {
      sb.append(",I've noticed that you have a high temperature.");
      sendToDoctor = true;
    } else if (healthTest.getPainLevel() > 5) {
      sb.append(",I've noticed that you have a pain.");
      sendToDoctor = true;
    }
    if (sendToDoctor) {
      sb.append("I recommend you to visit your doctor.");
      messengerService.sendButtonSendToDoctor(idFacebook, sb.toString());
    } else {
      sb.append(", your health is OK.");
      messengerService.sendTextMessage(idFacebook, sb.toString());
      messengerService.messageMenu(idFacebook);
    }
  }
}
