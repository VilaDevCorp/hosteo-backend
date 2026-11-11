package com.viladevcorp.hosteo.model.types;

public enum ImageType {
  PROFILE("profile"),
  APARTMENT("apartment"),
  WORKER("worker");

  private final String prefix;

  ImageType(String prefix) {
    this.prefix = prefix;
  }

  public String objectName(String id) {
    return prefix + "_" + id;
  }
}
