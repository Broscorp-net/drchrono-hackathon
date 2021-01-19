package com.remind.me.doc.service;

import com.remind.me.doc.model.Medication;
import com.remind.me.doc.repository.MedicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class MedicationService {
  @Autowired
  MedicationRepository medicationRepository;

  public Medication getMedication(Long id) {
    if (medicationRepository.exists(id)) {
      return  medicationRepository.findOne(id);
    } else throw new EntityNotFoundException();
  }


  public Medication createMedication(Medication medication) {
    return medicationRepository.save(medication);
  }

  public void saveMedication(Medication medication) throws EntityNotFoundException {
    if(medicationRepository.exists(medication.getId())){
      medicationRepository.save(medication);}
    else  throw new EntityNotFoundException();
  }

  public void deleteMedication(Long id){
    medicationRepository.delete(id);
  }
}
