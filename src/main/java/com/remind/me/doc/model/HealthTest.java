package com.remind.me.doc.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Data
public class HealthTest {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column
  private Double temperature;

  @Column
  private Integer pulse;

  @Column
  private String bloodPressure;

  @Column
  private Integer weight;

  @Column
  private Integer painLevel;

  @ToString.Exclude
  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "patient_id")
  private Patient patient;
}
