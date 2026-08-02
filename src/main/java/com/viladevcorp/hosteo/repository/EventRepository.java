package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.Event;
import com.viladevcorp.hosteo.model.types.EventState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EventRepository extends EntityRepository<Event> {

  @Query(
      value =
          "SELECT b FROM Event b "
              + "WHERE b.createdBy.username = :username "
              + "AND (:apartmentName IS NULL OR LOWER(b.apartment.name) LIKE :apartmentName) "
              + "AND (:states IS NULL OR b.state IN :states) "
              + "AND (:types IS NULL OR b.type IN :types) "
              + "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR b.endDate >= :startDate) "
              + "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR b.startDate < :endDate) "
              + "ORDER BY b.startDate DESC")
  List<Event> advancedSearch(
      @Param("username") String username,
      @Param("apartmentName") String apartmentName,
      @Param("states") Set<String> states,
      @Param("types") Set<String> types,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      Pageable pageable);

  @Query(
      value =
          "SELECT COUNT(b) FROM Event b "
              + "WHERE b.createdBy.username = :username "
              + "AND (:apartmentName IS NULL OR LOWER(b.apartment.name) LIKE :apartmentName) "
              + "AND (:states IS NULL OR b.state IN :states) "
              + "AND (:types IS NULL OR b.type IN :types) "
              + "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR b.endDate >= :startDate) "
              + "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR b.startDate < :endDate) ")
  int advancedCount(
      @Param("username") String username,
      @Param("apartmentName") String apartmentName,
      @Param("states") Set<String> states,
      @Param("types") Set<String> types,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT b FROM Event b "
              + "WHERE b.createdBy.username = :username  "
              + "AND b.apartment.id = :apartmentId "
              + "AND b.startDate < :endDate "
              + "AND b.endDate > :startDate "
              + "AND (:excludeEventId IS NULL OR b.id != :excludeEventId) "
              + "ORDER BY b.startDate ASC")
  List<Event> findEventsBetween(
      String username, UUID apartmentId, Instant startDate, Instant endDate, UUID excludeEventId);

  boolean existsEventByApartmentIdAndState(UUID apartmentId, EventState state);

  Optional<Event> findFirstEventByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateDesc(
      @Param("username") String username,
      @Param("apartmentId") UUID apartmentId,
      @Param("state") EventState state);

  Optional<Event> findFirstEventByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateAsc(
      @Param("username") String username,
      @Param("apartmentId") UUID apartmentId,
      @Param("state") EventState state);

  @Query(
      value =
          "SELECT * FROM events b WHERE b.created_by = :userId AND b.apartment_id = :apartmentId "
              + "AND b.start_date <  :dateParam AND (:state IS NULL OR b.state=:state) "
              + "ORDER BY b.end_date DESC LIMIT 1",
      nativeQuery = true)
  Optional<Event> findFirstEventBeforeDateWithState(
      @Param("userId") UUID userId,
      @Param("apartmentId") UUID apartmentId,
      @Param("dateParam") Instant dateParam,
      @Param("state") String state);

  @Query(
      value =
          "SELECT * FROM events b WHERE b.created_by = :userId AND b.apartment_id = :apartmentId AND b.start_date > :dateParam AND "
              + "(:state IS NULL OR b.state=:state) ORDER BY b.end_date ASC LIMIT 1",
      nativeQuery = true)
  Optional<Event> findFirstEventAfterDateWithState(
      @Param("userId") UUID userId,
      @Param("apartmentId") UUID apartmentId,
      @Param("dateParam") Instant dateParam,
      @Param("state") String state);

  @Query(
      value =
          "SELECT b FROM Event b "
              + "WHERE b.createdBy.username = :username "
              + "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR b.endDate >= :startDate) "
              + "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR b.startDate < :endDate) "
              + "ORDER BY b.startDate ASC")
  List<Event> findEventsByDateRange(
      @Param("username") String username,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate);

  @Query(
      value =
          "SELECT b FROM Event b LEFT JOIN FETCH b.assignments WHERE b.id = :id AND b.createdBy.username = :username")
  Optional<Event> findEventByIdWithAssignments(
      @Param("id") UUID id, @Param("username") String username);

  @Query(
      "SELECT b FROM Event b WHERE b.id IN :ids AND b.createdBy.username = :username ORDER BY b.startDate ASC")
  List<Event> findInIdsAndCreatedByUsername(
      @Param("ids") Set<UUID> ids, @Param("username") String username);

  @EntityGraph(attributePaths = {"assignments"})
  Optional<Event> findFirstByCreatedByUsernameAndApartmentIdAndStateOrderByEndDateDesc(
      String username, UUID apartmentId, EventState state);
}
