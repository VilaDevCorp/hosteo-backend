package com.viladevcorp.hosteo.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.viladevcorp.hosteo.model.Task;

@Repository
public interface TaskRepository extends EntityRepository<Task> {

  @Query(
      "SELECT t FROM Task t WHERE t.createdBy.username = :username "
          + "AND (:name IS NULL OR LOWER(t.name) LIKE :name) "
          + "AND (:type IS NULL OR t.type = :type) "
          + "ORDER BY t.createdAt DESC")
  List<Task> advancedSearch(
      @Param("username") String username,
      @Param("name") String name,
      @Param("type") String type,
      Pageable pageable);

  @Query(
      "SELECT COUNT(t) FROM Task t WHERE t.createdBy.username = :username "
          + "AND (:name IS NULL OR LOWER(t.name) LIKE :name) "
          + "AND (:type IS NULL OR t.type = :type) ")
  int advancedCount(
      @Param("username") String username, @Param("name") String name, @Param("type") String type);
}
