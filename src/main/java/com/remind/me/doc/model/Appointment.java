package com.remind.me.doc.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Appointment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column
  private Integer doctor;
  @Column
  private Integer duration;
  @Column
  private Integer exam_room;
  @Column
  private Integer office;
  @Column
  @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
  private LocalDateTime scheduled_time;

  @ToString.Exclude
  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "patient_id")
  private Patient patient;
}
