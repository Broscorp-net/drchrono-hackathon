package com.remind.me.doc.repository;

import com.remind.me.doc.model.Appointment;
import com.remind.me.doc.model.Medication;
import com.remind.me.doc.model.Patient;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicationRepository extends CrudRepository<Medication, Long> {

}
