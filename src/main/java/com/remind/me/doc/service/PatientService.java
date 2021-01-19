package com.remind.me.doc.service;

import com.remind.me.doc.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.remind.me.doc.repository.PatientRepository;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PatientService {

  @Autowired
  PatientRepository patientRepository;

  public Patient getPatient(String idFacebook) {
    if (patientRepository.exists(idFacebook)) {
      return  patientRepository.findOne(idFacebook);
    } else throw new EntityNotFoundException();
  }


  public Patient createPatient(Patient patient) {
    return patientRepository.save(patient);
  }

  public void savePatient(Patient patient) throws EntityNotFoundException {
    if(patientRepository.exists(patient.getIdFacebook())){
      patientRepository.save(patient);}
    else  throw new EntityNotFoundException();
  }

  public void deletePatient(String id){
    patientRepository.delete(id);
  }

  public List<Patient> listPatient(){
    List<Patient> list = new ArrayList<>();
    patientRepository.findAll().forEach(list::add);
    return list;
  }
}
