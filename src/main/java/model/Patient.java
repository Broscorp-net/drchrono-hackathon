package model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

import java.util.List;

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

  @ToString.Exclude
  @JsonManagedReference
  @OneToMany(cascade = CascadeType.ALL, mappedBy = "patient")
  List<Medication> medicationList;
}