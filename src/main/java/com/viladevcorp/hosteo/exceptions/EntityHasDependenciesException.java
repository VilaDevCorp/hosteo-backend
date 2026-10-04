package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EntityHasDependenciesException extends Exception {
  public EntityHasDependenciesException(String message) {
    super(message);
  }
}