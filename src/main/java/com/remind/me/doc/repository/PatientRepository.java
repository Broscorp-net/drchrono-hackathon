package com.remind.me.doc.repository;

import com.remind.me.doc.model.Patient;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface PatientRepository extends CrudRepository<Patient, String> {
}
