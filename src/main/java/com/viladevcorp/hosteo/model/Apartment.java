package com.viladevcorp.hosteo.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.viladevcorp.hosteo.model.dto.ApartmentDto;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.viladevcorp.hosteo.model.jsonconverters.AddressJsonConverter;
import com.viladevcorp.hosteo.model.types.ApartmentState;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "apartments")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class Apartment extends BaseEntity {

  @NotNull
  @NotBlank
  @Column(nullable = false, unique = true)
  private String name;

  @Column(unique = true)
  private String airbnbId;

  @Column(unique = true)
  private String bookingId;

  @Convert(converter = AddressJsonConverter.class)
  private Address address;

  @NotNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private ApartmentState state = ApartmentState.READY;

  @Builder.Default private boolean visible = true;

  @OneToMany(mappedBy = "apartment")
  @JsonIgnore
  @Builder.Default
  private Set<Event> events = new HashSet<>();

  @OneToMany(mappedBy = "apartment")
  @Builder.Default
  private List<Task> tasks = new ArrayList<>();

  public void addTask(Task task) {
    task.setApartment(this);
    tasks.add(task);
  }

  public void removeTask(Task task) {
    task.setApartment(null);
    tasks.remove(task);
  }

  @Override
  public ApartmentDto toDto() {
    return new ApartmentDto(this);
  }
}
