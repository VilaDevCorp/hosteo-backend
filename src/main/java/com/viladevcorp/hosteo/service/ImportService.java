package com.viladevcorp.hosteo.service;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.dto.ImportResultDto;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.ConflictType;
import com.viladevcorp.hosteo.model.types.EventType;
import com.viladevcorp.hosteo.repository.*;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;
import com.viladevcorp.hosteo.utils.ServiceUtils;

import java.io.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ImportService {

  private final ImpBookingRepository impBookingRepository;
  private final EventRepository eventRepository;
  private final AssignmentRepository assignmentRepository;
  private final ApartmentRepository apartmentRepository;
  private final EventProcessor eventProcessor;

  @Autowired
  public ImportService(
      ImpBookingRepository impBookingRepository,
      EventRepository eventRepository,
      AssignmentRepository assignmentRepository,
      ApartmentRepository apartmentRepository,
      EventProcessor eventProcessor) {
    this.apartmentRepository = apartmentRepository;
    this.eventRepository = eventRepository;
    this.impBookingRepository = impBookingRepository;
    this.assignmentRepository = assignmentRepository;
    this.eventProcessor = eventProcessor;
  }

  public static final int AIRBNB_START_DATE_POSITION = 4;
  public static final int AIRBNB_END_DATE_POSITION = 5;
  public static final int AIRBNB_GUEST_POSITION = 7;
  public static final int AIRBNB_APARTMENT_POSITION = 8;

  public static final String AIRBNB_CHECKIN_TIME = "15:00";
  public static final String AIRBNB_CHECKOUT_TIME = "11:00";

  public static final String AIRBNB_DATE_FORMAT = "MM/dd/yyyy HH:mm";
  public static final char AIRBNB_SEPARATOR = ',';

  public static final int BOOKING_START_DATE_POSITION = 4;
  public static final int BOOKING_END_DATE_POSITION = 5;
  public static final int BOOKING_GUEST_POSITION = 2;
  public static final int BOOKING_APARTMENT_POSITION = 0;
  public static final int BOOKING_STATUS_POSITION = 7;

  public static final String BOOKING_CHECKIN_TIME = "15:00";
  public static final String BOOKING_CHECKOUT_TIME = "11:00";
  public static final String BOOKING_DATE_FORMAT = "d MMMM yyyy HH:mm";
  public static final char BOOKING_SEPARATOR = ';';

  public static final int PAGE_SIZE = 15;

  public boolean existsImportInProgress() {
    return impBookingRepository.existsByCreatedByUsername(AuthUtils.getUsername());
  }

  public List<ImpBooking> searchUserImpBookings(int pageNumber) {
    PageRequest pageRequest = ServiceUtils.createPageRequest(pageNumber, PAGE_SIZE);
    List<ImpBooking> impBookings =
        impBookingRepository.getUserImpBookings(AuthUtils.getUsername(), pageRequest);
    impBookings = impBookings.stream().map(this::checkImportConflict).toList();
    return impBookings;
  }

  public PageMetadata getImpBookingsMetadata() {

    int totalRows = impBookingRepository.countUserImpBookings(AuthUtils.getUsername());
    int totalPages = ServiceUtils.calculateTotalPages(PAGE_SIZE, totalRows);
    return new PageMetadata(totalPages, totalRows);
  }

  public ImpBooking getImpBookingConflict(
      UUID apartmentId, Instant startDate, Instant endDate, UUID excludeImpBookingId) {
    return impBookingRepository
        .getImpBookingsInRangeDate(
            AuthUtils.getUsername(), apartmentId, endDate, startDate, excludeImpBookingId)
        .stream()
        .findFirst()
        .orElse(null);
  }

  private ImpBooking checkImportConflict(ImpBooking impBooking) {
    Conflict conflict;

    Pair<Event, Assignment> conflicts =
        ServiceUtils.getScheduleConflicts(
            eventRepository,
            assignmentRepository,
            impBooking.getApartment().getId(),
            impBooking.getStartDate(),
            impBooking.getEndDate(),
            null,
            null);

    if (conflicts.a != null) {
      conflict = new Conflict(ConflictType.EVENT_CONFLICT, conflicts.a.toDto());
    } else {
      if (conflicts.b != null) {
        conflict = new Conflict(ConflictType.ASSIGNMENT_CONFLICT, conflicts.b.toDto());
      } else {
        ImpBooking impBookingConflict =
            getImpBookingConflict(
                impBooking.getApartment().getId(),
                impBooking.getStartDate(),
                impBooking.getEndDate(),
                impBooking.getId());
        if (impBookingConflict != null) {
          conflict = new Conflict(ConflictType.IMPORT_BOOKING_CONFLICT, impBookingConflict.toDto());
        } else {
          conflict = null;
        }
      }
    }
    impBooking.setConflict(conflict);
    return impBooking;
  }

  public List<ImpBooking> importAirbnbBookings(File importFile) throws IOException, CsvException {
    try (BufferedReader br = new BufferedReader(new FileReader(importFile))) {
      CSVParser parser = new CSVParserBuilder().withSeparator(AIRBNB_SEPARATOR).build();
      try (CSVReader csvReader =
          new CSVReaderBuilder(br).withSkipLines(1).withCSVParser(parser).build(); ) {
        Map<String, Apartment> apartmentCache = new HashMap<>();
        DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(AIRBNB_DATE_FORMAT).withZone(ZoneOffset.UTC);

        return csvReader.readAll().stream()
            .map(
                line -> {
                  Apartment apartment =
                      apartmentCache.computeIfAbsent(
                          line[AIRBNB_APARTMENT_POSITION],
                          airbnbId ->
                              apartmentRepository
                                  .findByAirbnbIdAndCreatedByUsername(
                                      airbnbId, AuthUtils.getUsername())
                                  .orElse(null));
                  if (apartment == null) {
                    return null;
                  }

                  Instant startDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[AIRBNB_START_DATE_POSITION] + " " + AIRBNB_CHECKIN_TIME));
                  Instant endDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[AIRBNB_END_DATE_POSITION] + " " + AIRBNB_CHECKOUT_TIME));

                  ImpBooking impBooking =
                      ImpBooking.builder()
                          .apartment(apartment)
                          .source(EventSource.AIRBNB)
                          .startDate(startDate)
                          .endDate(endDate)
                          .name(line[AIRBNB_GUEST_POSITION])
                          .build();
                  checkImportConflict(impBooking);
                  return impBookingRepository.save(impBooking);
                })
            .filter(Objects::nonNull)
            .toList();
      } catch (IOException | CsvException e) {
        log.error("Error reading import file: {}", e.getMessage());
        throw e;
      }
    }
  }

  public List<ImpBooking> importBookingBookings(File importFile) throws IOException, CsvException {
    try (BufferedReader br = new BufferedReader(new FileReader(importFile))) {
      CSVParser parser = new CSVParserBuilder().withSeparator(BOOKING_SEPARATOR).build();
      try (CSVReader csvReader =
          new CSVReaderBuilder(br).withSkipLines(1).withCSVParser(parser).build(); ) {
        Map<String, Apartment> apartmentCache = new HashMap<>();
        DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(BOOKING_DATE_FORMAT)
                .withZone(ZoneOffset.UTC)
                .withLocale(Locale.ENGLISH);
        return csvReader.readAll().stream()
            .map(
                line -> {
                  if (line[BOOKING_STATUS_POSITION].equalsIgnoreCase("Cancelled")) {
                    return null;
                  }
                  Apartment apartment =
                      apartmentCache.computeIfAbsent(
                          line[BOOKING_APARTMENT_POSITION],
                          bookingId ->
                              apartmentRepository
                                  .findByBookingIdAndCreatedByUsername(
                                      bookingId, AuthUtils.getUsername())
                                  .orElse(null));
                  if (apartment == null) {
                    return null;
                  }
                  Instant startDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[BOOKING_START_DATE_POSITION] + " " + BOOKING_CHECKIN_TIME));
                  Instant endDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[BOOKING_END_DATE_POSITION] + " " + BOOKING_CHECKOUT_TIME));

                  ImpBooking impBooking =
                      ImpBooking.builder()
                          .apartment(apartment)
                          .source(EventSource.BOOKING)
                          .startDate(startDate)
                          .endDate(endDate)
                          .name(line[BOOKING_GUEST_POSITION])
                          .build();
                  checkImportConflict(impBooking);
                  return impBookingRepository.save(impBooking);
                })
            .filter(Objects::nonNull)
            .toList();
      }
    } catch (IOException | CsvException e) {
      log.error("Error reading import file: {}", e.getMessage());
      throw e;
    }
  }

  public ImportResultDto executeImportBookings() {
    List<ImpBooking> impBookings =
        impBookingRepository.getUserImpBookings(AuthUtils.getUsername(), null);
    Set<UUID> importedBookingIds = new HashSet<>();
    AtomicInteger importErrorNumber = new AtomicInteger();
    impBookings.forEach(
        impBooking -> {
          if (impBooking.getConflict() != null) {
            importErrorNumber.getAndIncrement();
            return;
          }
          EventCreateForm eventForm =
              EventCreateForm.builder()
                  .type(EventType.BOOKING)
                  .apartmentId(impBooking.getApartment().getId())
                  .type(EventType.BOOKING)
                  .startDate(impBooking.getStartDate())
                  .endDate(impBooking.getEndDate())
                  .name(impBooking.getName())
                  .state(EventState.PENDING)
                  .source(impBooking.getSource())
                  .build();
          String createError = null;
          try {
            eventProcessor.executeCreateEventLogic(eventForm);
            importedBookingIds.add(impBooking.getId());
            return;
          } catch (NotAvailableDatesException e) {
            createError = CodeErrors.NOT_AVAILABLE_DATES;
          } catch (PrevOfInProgressCannotBePendingOrInProgress e) {
            createError = CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS;
          } catch (PrevOfFinishedCannotBeNotPendingOrInProgress e) {
            createError = CodeErrors.PREV_OF_FINISHED_CANNOT_BE_PENDING_OR_INPROGRESS;
          } catch (NextOfPendingCannotBeInprogressOrFinished e) {
            createError = CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED;
          } catch (NextOfInProgressCannotBeFinishedOrInProgress e) {
            createError = CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS;
          } catch (Exception e) {
            log.error("Unexpected error creating booking from import: {}", e.getMessage());
            createError = CodeErrors.UNEXPECTED_ERROR;
          }
          impBooking.setCreationError(createError);
          impBookingRepository.save(impBooking);
          importErrorNumber.getAndIncrement();
        });
    impBookingRepository.deleteAllById(importedBookingIds);
    return new ImportResultDto(importedBookingIds.size(), importErrorNumber.get());
  }

  public void deleteUserImpBookings() {
    List<ImpBooking> impBookings =
        impBookingRepository.getUserImpBookings(AuthUtils.getUsername(), null);
    impBookingRepository.deleteAll(impBookings);
  }

  public void deleteImpBookingById(UUID id) {
    impBookingRepository.deleteById(id);
  }
}
