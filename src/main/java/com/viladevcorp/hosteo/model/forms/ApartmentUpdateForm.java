package com.viladevcorp.hosteo.model.forms;

import java.util.UUID;

import com.viladevcorp.hosteo.model.Address;

import com.viladevcorp.hosteo.model.types.ApartmentState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ApartmentUpdateForm {

  @NotNull private UUID id;

  @NotNull @NotBlank private String name;

  private String airbnbId;

  private String bookingId;

  private Address address;
}
