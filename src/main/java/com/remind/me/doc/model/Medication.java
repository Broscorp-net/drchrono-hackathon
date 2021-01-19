package com.remind.me.doc.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
public class Medication {

  @Id
  @Expose
  private Long id;

  @Column
  @Expose
  private String date_started_taking;

  @Column
  @Expose
  private String date_stopped_taking;

  @Column
  @Expose
  private String name;
  @Column
  @Expose
  private String pharmacy_note;
  @Expose
  @Column
  private String dosage_quantity;
  @Expose
  @Column
  private String notes;
  @Expose
  @Column
  private String status;
  @Column
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime reminderTime;
  @Column
  private Integer takingPills = 0;

  @ToString.Exclude
  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "patient_id")
  private Patient patient;
}
