package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.Transaction;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends EntityRepository<Transaction> {

  @Query(
      "SELECT t FROM Transaction t WHERE t.createdBy.username = :username "
          + "AND (:workerId IS NULL OR t.worker.id = :workerId) "
          + "AND (CAST(:startDate AS TIMESTAMP) IS NULL OR t.createdAt >= :startDate) "
          + "AND (CAST(:endDate AS TIMESTAMP) IS NULL OR t.createdAt <= :endDate) "
          + "AND (:isPayment IS NULL "
          + "     OR (:isPayment = TRUE AND t.assignment IS NULL) "
          + "     OR (:isPayment = FALSE AND t.assignment IS NOT NULL)) "
          + "ORDER BY t.createdAt DESC")
  List<Transaction> search(
      @Param("username") String username,
      @Param("workerId") UUID workerId,
      @Param("startDate") Instant startDate,
      @Param("endDate") Instant endDate,
      @Param("isPayment") Boolean isPayment);

  @Query(
      "SELECT t.worker, COALESCE(SUM(t.amount), 0) FROM Transaction t "
          + "WHERE t.createdBy.username = :username GROUP BY t.worker")
  List<Object[]> sumByWorker(@Param("username") String username);

  @Query(
      "SELECT t FROM Transaction t WHERE t.assignment.id = :assignmentId "
          + "AND t.createdBy.username = :username")
  Optional<Transaction> findByAssignmentId(
      @Param("assignmentId") UUID assignmentId, @Param("username") String username);

  @Query(
      "SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Transaction t "
          + "WHERE t.assignment.id = :assignmentId AND t.createdBy.username = :username")
  boolean existsByAssignmentId(
      @Param("assignmentId") UUID assignmentId, @Param("username") String username);

  @Modifying
  @Query(
      "DELETE FROM Transaction t WHERE t.assignment.id = :assignmentId "
          + "AND t.createdBy.username = :username")
  void deleteByAssignmentId(
      @Param("assignmentId") UUID assignmentId, @Param("username") String username);

  boolean existsByWorkerIdAndCreatedByUsername(UUID workerId, String username);
}
