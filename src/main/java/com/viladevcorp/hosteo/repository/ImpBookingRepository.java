package com.viladevcorp.hosteo.repository;

import com.viladevcorp.hosteo.model.ImpBooking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ImpBookingRepository extends EntityRepository<ImpBooking> {

  @Query(
      value =
          "SELECT b FROM ImpBooking b "
              + "WHERE b.createdBy.username = :username "
              + "ORDER BY b.startDate ASC")
  List<ImpBooking> getUserImpBookings(@Param("username") String username, Pageable pageable);

  @Query(
      "SELECT b FROM ImpBooking b "
          + "WHERE b.createdBy.username = :username "
          + "AND b.apartment.id = :apartmentId "
          + "AND b.startDate <= :endDate "
          + "AND b.endDate >= :startDate "
          + "AND (:excludeBookingId IS NULL OR b.id <> :excludeBookingId) ")
  List<ImpBooking> getImpBookingsInRangeDate(
      String username, UUID apartmentId, Instant endDate, Instant startDate, UUID excludeBookingId);

  @Query(value = "SELECT count(b) FROM ImpBooking b " + "WHERE b.createdBy.username = :username ")
  int countUserImpBookings(@Param("username") String username);

  boolean existsByCreatedByUsername(String username);

  List<ImpBooking> findByNameAndCreatedByUsername(String name, String username);

  @Modifying
  @Query(
      "DELETE FROM ImpBooking b WHERE b.apartment.id = :apartmentId "
          + "AND b.createdBy.username = :username")
  void deleteByApartmentId(
      @Param("apartmentId") UUID apartmentId, @Param("username") String username);
}
