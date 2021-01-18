package model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.LocalDateTime;

@Data
public class Medication {

  @Id
  private Long id;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @Column
  private String date_started_taking;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  @Column
  private String date_stopped_taking;

  @Column
  private String name;
  @Column
  private String pharmacy_note;
  @Column
  private String dosage_quantity;
  @Column
  private String notes;
  @Column
  private String status;
  @Column
  private LocalDateTime reminderTime;

  @ToString.Exclude
  @JsonBackReference
  @ManyToOne
  @JoinColumn(name = "patient_id")
  private Patient patient;
}
