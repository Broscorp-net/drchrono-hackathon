package com.remind.me.doc.service;

import com.github.messenger4j.exception.MessengerApiException;
import com.github.messenger4j.exception.MessengerIOException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.remind.me.doc.model.Medication;
import com.remind.me.doc.model.Patient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Service
public class TakingMedicineService {

  @Value("${dr.chrono.url}")
  private String defaultUrl;

  @Autowired
  private MessengerService messengerService;
  @Autowired
  private PatientService patientService;
  @Autowired
  private MedicationService medicationService;
  @Autowired
  private UpdateTokenService updateTokenService;

  public void checkDiagnosis(String senderId) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    List<String> diagnosis = new ArrayList<>();
    Patient patient = patientService.getPatient(senderId);
    headers.set("Authorization", "Bearer " + updateTokenService.getAccessToken());
    String url = defaultUrl + "/api/problems";
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("patient", patient.getIdChronos());
    HttpEntity entity = new HttpEntity(headers);
    ResponseEntity<String> response = restTemplate.exchange(
            builder.toUriString(), HttpMethod.GET, entity, String.class);
    if (response.getStatusCode().value() == 200) {
      JSONObject jsonObject = new JSONObject(response.getBody());
      JSONArray array = jsonObject.getJSONArray("results");
      for (int i = 0; i < array.length(); i++) {
        JSONObject diagnosisJson = (JSONObject) array.get(i);
        String status = diagnosisJson.get("status").toString();
        if (status.equals("active")) {
          String description = diagnosisJson.get("description").toString();
          diagnosis.add(description);
        }
      }
    }
    messengerService.sendDiagnosisMessage(diagnosis, senderId);
  }


  private List<Medication> checkMedication(String senderId) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();
    List<String> diagnosis = new ArrayList<>();
    Patient patient = patientService.getPatient(senderId);
    headers.set("Authorization", "Bearer " + updateTokenService.getAccessToken());
    String url = defaultUrl + "/api/medications";
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("patient", patient.getIdChronos());
    HttpEntity entity = new HttpEntity(headers);
    ResponseEntity<String> response = restTemplate.exchange(
            builder.toUriString(), HttpMethod.GET, entity, String.class);
    List<Medication> medications = new ArrayList<>();
    if (response.getStatusCode().value() == 200) {
      JSONObject jsonObject = new JSONObject(response.getBody());
      JSONArray array = jsonObject.getJSONArray("results");
      for (int i = 0; i < array.length(); i++) {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        Medication medication = gson
                .fromJson(array.get(i).toString(), Medication.class);
        if (medication.getStatus().equals("active")) {
          medication.setPatient(patient);
          medicationService.createMedication(medication);
          medications.add(medication);
        }
      }
      patientService.savePatient(patient);
    }
    return medications;
  }

  public void sendMessageAboutMedications(String senderId){
    List<Medication> medications = checkMedication(senderId);
    if (medications.size() > 0) {
     messengerService.sendTextMessage(senderId, "You're prescribed the following medications:");
      for (Medication medication: medications) {
         StringBuilder message = new StringBuilder(medication.getName() + " " + medication.getDate_started_taking());
          if (medication.getDate_stopped_taking() != null) {
            message.append(" - ").append(medication.getDate_stopped_taking()).append("\n");
          } else message.append("+\n");
        messengerService.sendTextMessage(senderId, message.toString());
      }
    }
    try {
      messengerService.sendReminderTakePills(senderId);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }

  public void sendMessageAboutMedication(String senderId) {
    Patient patient = patientService.getPatient(senderId);
    List<Medication> medications = patient.getMedicationList();
    Medication medication;
   if (patient.getCurrentMedication() == null) {
     medication = medications.get(0);
   } else {
     int indexOfMedication = medications.indexOf(medicationService.getMedication(patient.getCurrentMedication()));
     if (indexOfMedication == medications.size()-1) indexOfMedication = 0;
     medication = medications.get(indexOfMedication+1);
   }
    messengerService.sendTextMessage(senderId, medication.getName());
    StringBuilder messageDate = new StringBuilder("Date to take: \n" + medication.getDate_started_taking());
    if (medication.getDate_stopped_taking() != null) {
      messageDate.append(" - ").append(medication.getDate_stopped_taking()).append("\n");
    } else messageDate.append("+\n");
    messengerService.sendTextMessage(senderId, messageDate.toString());
    if (medication.getDosage_quantity() != null || medication.getNotes() != null) {
      String messageDose = "Dose and Timing: \n" + medication.getDosage_quantity()
             +  "\n" + medication.getNotes();
      messengerService.sendTextMessage(senderId, messageDose);
    }
    if (medication.getPharmacy_note() != null) {
      messengerService.sendTextMessage(senderId, medication.getPharmacy_note());
    }
    try {
      patient.setCurrentMedication(medication.getId());
      patientService.savePatient(patient);
      messengerService.sendButtonsAboutReminds(senderId);
    } catch (MessengerApiException | MessengerIOException e) {
      e.printStackTrace();
    }
  }
}
