package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.FailedImportedEvent;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FailedImportedEventRepository extends EntityRepository<FailedImportedEvent> {

  List<FailedImportedEvent> findByCreatedByUsernameOrderByStartDateAsc(String username);

  void deleteAllByCreatedByUsername(String username);

  @Modifying
  @Query(
      "DELETE FROM FailedImportedEvent i WHERE i.apartment.id = :apartmentId "
          + "AND i.createdBy.username = :username")
  void deleteByApartmentId(
      @Param("apartmentId") UUID apartmentId, @Param("username") String username);
}