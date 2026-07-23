package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.Apartment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ApartmentRepository extends EntityRepository<Apartment> {

  @Query(
      "SELECT a FROM Apartment a WHERE a.createdBy.username = :username AND (:visible is null OR a.visible = :visible) "
          + "AND (:name is null OR lower(a.name) like :name) AND (:states is null OR a.state IN :states) ORDER BY a.visible DESC, a.createdAt DESC ")
  List<Apartment> advancedSearch(
      String username, String name, Set<String> states, Boolean visible, Pageable pageable);

  @Query(
      "SELECT COUNT(a) FROM Apartment a WHERE a.createdBy.username = :username AND (:visible is null OR a.visible = :visible) "
          + "AND (:name is null OR lower(a.name) like :name) AND (:states is null OR a.state IN :states)")
  int advancedCount(String username, String name, Set<String> states, Boolean visible);

  @Query(
      "SELECT a FROM Apartment a LEFT JOIN FETCH a.tasks t WHERE a.id = :id AND a.createdBy.username = :username")
  Optional<Apartment> findById(
      @Param("id") @NonNull UUID id, @Param("username") @NonNull String username);

  Optional<Apartment> findByAirbnbIdAndCreatedByUsername(String airbnbId, String username);

  Optional<Apartment> findByBookingIdAndCreatedByUsername(String bookingId, String username);
}
