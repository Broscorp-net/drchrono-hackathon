package com.remind.me.doc.service;

import com.remind.me.doc.model.Appointment;
import com.remind.me.doc.repository.AppointmentRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityNotFoundException;

@Service
public class AppointmentService {

  @Value("${dr.chrono.url}")
  private String defaultUrl;
  @Value("${doctorId}")
  private Integer doctor;
  @Value("${office}")
  private Integer office;

  @Autowired
  private UpdateTokenService updateTokenService;
  @Autowired
  private AppointmentRepository appointmentRepository;

  public Appointment getAppointment(Integer id) {
    if (appointmentRepository.exists(id)) {
      return  appointmentRepository.findOne(id);
    } else throw new EntityNotFoundException();
  }


  public Appointment createAppointment(Appointment appointment) {
    appointment.setDoctor(doctor);
    appointment.setExam_room(1);
    appointment.setDuration(30);
    appointment.setOffice(office);
    return appointmentRepository.save(appointment);
  }

  public void saveAppointment(Appointment appointment) throws EntityNotFoundException {
    if(appointmentRepository.exists(appointment.getId())){
      appointmentRepository.save(appointment);}
    else  throw new EntityNotFoundException();
  }

  public void deleteAppointment(Integer id){
    appointmentRepository.delete(id);
  }
  
  public void sendAppoitment(Appointment appointment) {
    RestTemplate restTemplate = new RestTemplate();
    HttpHeaders headers = new HttpHeaders();

    headers.set("Authorization", "Bearer " + updateTokenService.getAccessToken());
    headers.setContentType(MediaType.APPLICATION_JSON);
    String url = defaultUrl + "/api/appointments";
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("doctor", appointment.getDoctor());
    jsonObject.put("duration", appointment.getDuration());
    jsonObject.put("office", appointment.getOffice());
    jsonObject.put("patient", "91649384");
    jsonObject.put("scheduled_time", appointment.getScheduled_time());
    jsonObject.put("exam_room", appointment.getExam_room());
    HttpEntity<String> entity = new HttpEntity(jsonObject.toString(), headers);
    String responseEntity = restTemplate.postForObject(url, entity, String.class);
    System.out.println(responseEntity);
  }
}
