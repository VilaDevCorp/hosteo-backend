package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class EntityFrozenException extends Exception {
  public EntityFrozenException(String message) {
    super(message);
  }
}