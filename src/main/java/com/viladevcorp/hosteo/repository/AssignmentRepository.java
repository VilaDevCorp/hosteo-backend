package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.Assignment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AssignmentRepository extends EntityRepository<Assignment> {

  @Query(
      "SELECT a FROM Assignment a WHERE a.createdBy.username = :username "
          + "AND (:taskName IS NULL OR LOWER(a.task.name) LIKE :taskName) "
          + "AND (:state IS NULL OR a.state = :state) "
          + "ORDER BY a.startDate DESC")
  List<Assignment> advancedSearch(
      @Param("username") String username,
      @Param("taskName") String taskName,
      @Param("state") String state,
      Pageable pageable);

  @Query(
      "SELECT COUNT(a) FROM Assignment a WHERE a.createdBy.username = :username "
          + "AND (:taskName IS NULL OR LOWER(a.task.name) LIKE :taskName) "
          + "AND (:state IS NULL OR a.state = :state)")
  int advancedCount(
      @Param("username") String username,
      @Param("taskName") String taskName,
      @Param("state") String state);

  @Query(
      "SELECT a FROM Assignment a WHERE a.task.id = :taskId AND a.createdBy.username = :username")
  Set<Assignment> findByTaskIdAndCreatedByUsername(UUID taskId, String username);

  @Query(
      value =
          "SELECT a FROM Assignment a "
              + "WHERE a.createdBy.username = :username "
              + "AND a.task.apartment.id = :apartmentId "
              + "AND a.startDate < :endDate "
              + "AND a.endDate > :startDate "
              + "AND (:excludeAssignmentId IS NULL OR a.id != :excludeAssignmentId) ")
  List<Assignment> findAssignmentsBetween(
      String username,
      UUID apartmentId,
      Instant startDate,
      Instant endDate,
      UUID excludeAssignmentId);

  @Query(
      value =
          "SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Assignment a "
              + "WHERE a.createdBy.username = :username "
              + "AND a.worker.id = :workerId "
              + "AND a.worker.visible = true "
              + "AND a.startDate < :endDate "
              + "AND a.endDate > :startDate "
              + "AND (:excludeAssignmentId IS NULL OR a.id != :excludeAssignmentId) ")
  boolean checkWorkerAvailability(
      String username, UUID workerId, Instant startDate, Instant endDate, UUID excludeAssignmentId);

  @Query(
      "SELECT a FROM Assignment a WHERE a.createdBy.username = :username "
          + "AND (:apartmentId IS NULL OR a.task.apartment.id = :apartmentId) "
          + "AND (:state IS NULL OR a.state = :state) AND (CAST(:startDate AS TIMESTAMP) IS NULL OR a.startDate >= :startDate) "
          + "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR a.startDate < :endDate)")
  Set<Assignment> findByApartmentAndStateAndDateRange(
      String username, UUID apartmentId, String state, Instant startDate, Instant endDate);

  @Query(
      "SELECT a FROM Assignment a WHERE a.createdBy.username = :username  "
          + "AND a.id IN :ids ORDER BY a.startDate ASC ")
  List<Assignment> findInIdsAndCreatedByUsername(Set<UUID> ids, String username);
}
