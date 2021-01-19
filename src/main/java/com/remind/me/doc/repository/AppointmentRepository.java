package com.remind.me.doc.repository;

import com.remind.me.doc.model.Appointment;
import com.remind.me.doc.model.Medication;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends CrudRepository<Appointment, Integer> {
}
