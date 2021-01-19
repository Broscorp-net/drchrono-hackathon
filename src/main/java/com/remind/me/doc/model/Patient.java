package com.remind.me.doc.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

import java.util.List;

import static javax.persistence.FetchType.EAGER;

@Entity
@Data
public class Patient {

  @Id
  @Column
  private String idFacebook;

  @Column
  private String chartId;
  @Column
  private String firstName;
  @Column
  private String lastName;
  @Column
  private String idChronos;
  @Column
  private Long currentMedication;

  @ToString.Exclude
  @JsonManagedReference
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "patient",  fetch = EAGER)
  List<Medication> medicationList;

  @ToString.Exclude
  @JsonManagedReference
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "patient")
  List<HealthTest> healthTestList;
}