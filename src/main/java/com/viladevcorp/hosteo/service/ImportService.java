package com.viladevcorp.hosteo.service;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import com.viladevcorp.hosteo.exceptions.*;
import com.viladevcorp.hosteo.model.Apartment;
import com.viladevcorp.hosteo.model.FailedImportedEvent;
import com.viladevcorp.hosteo.model.dto.ImportResultDto;
import com.viladevcorp.hosteo.model.dto.FailedImportedEventDto;
import com.viladevcorp.hosteo.model.forms.EventCreateForm;
import com.viladevcorp.hosteo.model.forms.FailedImportedEventUpdateForm;
import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.EventType;
import com.viladevcorp.hosteo.repository.ApartmentRepository;
import com.viladevcorp.hosteo.repository.FailedImportedEventRepository;
import com.viladevcorp.hosteo.utils.AuthUtils;
import com.viladevcorp.hosteo.utils.CodeErrors;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ImportService {

  private final FailedImportedEventRepository importedEventRepository;
  private final ApartmentRepository apartmentRepository;
  private final EventProcessor eventProcessor;

  public ImportService(
      FailedImportedEventRepository importedEventRepository,
      ApartmentRepository apartmentRepository,
      EventProcessor eventProcessor) {
    this.importedEventRepository = importedEventRepository;
    this.apartmentRepository = apartmentRepository;
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

  private record ImportCandidate(
      UUID importedEventId,
      Apartment apartment,
      String name,
      Instant startDate,
      Instant endDate,
      EventSource source) {}

  public ImportResultDto upload(File importFile, EventSource source)
      throws IOException, CsvException {
    List<ImportCandidate> candidates;
    if (source == EventSource.AIRBNB) {
      candidates = parseAirbnb(importFile);
    } else if (source == EventSource.BOOKING) {
      candidates = parseBooking(importFile);
    } else {
      throw new IllegalArgumentException("Unsupported import source: " + source);
    }

    // Maintain only the current batch.
    importedEventRepository.deleteAllByCreatedByUsername(AuthUtils.getUsername());

    return processEventsBatch(candidates);
  }

  public List<FailedImportedEvent> getFailedImportedEvents() {
    return importedEventRepository.findByCreatedByUsernameOrderByStartDateAsc(
        AuthUtils.getUsername());
  }

  public FailedImportedEvent updateFailedImportedEvent(UUID id, FailedImportedEventUpdateForm form)
      throws EntityNotFoundException {
    Optional<FailedImportedEvent> resultOpt =
        importedEventRepository.findById(id, AuthUtils.getUsername());
    if (resultOpt.isEmpty()) {
      throw new EntityNotFoundException("Imported event not found with id: " + id);
    }
    FailedImportedEvent importedEvent = resultOpt.get();

    if (form.getApartmentId() != null) {
      Optional<Apartment> apartmentOpt =
          apartmentRepository.findById(form.getApartmentId(), AuthUtils.getUsername());
      if (apartmentOpt.isEmpty()) {
        throw new EntityNotFoundException("Apartment not found with id: " + form.getApartmentId());
      }
      importedEvent.setApartment(apartmentOpt.get());
    }
    if (form.getName() != null) {
      importedEvent.setName(form.getName());
    }
    if (form.getStartDate() != null) {
      importedEvent.setStartDate(form.getStartDate());
    }
    if (form.getEndDate() != null) {
      importedEvent.setEndDate(form.getEndDate());
    }

    return importedEventRepository.save(importedEvent);
  }

  public ImportResultDto retry() {
    List<FailedImportedEvent> importedEvents = getFailedImportedEvents();
    List<ImportCandidate> candidates =
        importedEvents.stream()
            .map(
                e ->
                    new ImportCandidate(
                        e.getId(),
                        e.getApartment(),
                        e.getName(),
                        e.getStartDate(),
                        e.getEndDate(),
                        e.getSource()))
            .toList();
    return processEventsBatch(candidates);
  }

  public void dismiss(UUID id) {
    importedEventRepository.deleteById(id);
  }

  public void dismissAll() {
    importedEventRepository.deleteAllByCreatedByUsername(AuthUtils.getUsername());
  }

  private ImportResultDto processEventsBatch(List<ImportCandidate> candidates) {
    List<ImportCandidate> sortedCandidates =
        candidates.stream()
            .sorted(
                Comparator.comparing(
                    ImportCandidate::startDate, Comparator.nullsLast(Instant::compareTo)))
            .toList();

    int successCount = 0;
    List<FailedImportedEventDto> failedEvents = new ArrayList<>();

    for (ImportCandidate candidate : sortedCandidates) {
      if (candidate.apartment() == null) {
        handleFailure(candidate, CodeErrors.UNRECOGNIZED_APARTMENT_ID, failedEvents);
        continue;
      }

      EventCreateForm eventForm =
          EventCreateForm.builder()
              .type(EventType.BOOKING)
              .apartmentId(candidate.apartment().getId())
              .startDate(candidate.startDate())
              .endDate(candidate.endDate())
              .name(candidate.name())
              .state(EventState.PENDING)
              .source(candidate.source())
              .build();

      try {
        eventProcessor.executeCreateEventLogic(eventForm);
        successCount++;
        if (candidate.importedEventId() != null) {
          importedEventRepository.deleteById(candidate.importedEventId());
        }
      } catch (Exception e) {
        log.warn("Failed to create event from import ({}): {}", candidate.name(), e.getMessage());
        handleFailure(candidate, mapError(e), failedEvents);
      }
    }

    return new ImportResultDto(successCount, failedEvents);
  }

  private void handleFailure(
      ImportCandidate candidate, String error, List<FailedImportedEventDto> failedEvents) {
    FailedImportedEvent importedEvent;
    if (candidate.importedEventId() != null) {
      Optional<FailedImportedEvent> resultOpt =
          importedEventRepository.findById(candidate.importedEventId(), AuthUtils.getUsername());
      importedEvent = resultOpt.orElse(null);
      if (importedEvent == null) {
        importedEvent =
            FailedImportedEvent.builder()
                .apartment(candidate.apartment())
                .name(candidate.name())
                .startDate(candidate.startDate())
                .endDate(candidate.endDate())
                .source(candidate.source())
                .error(error)
                .build();
      } else {
        importedEvent.setError(error);
      }
    } else {
      importedEvent =
          FailedImportedEvent.builder()
              .apartment(candidate.apartment())
              .name(candidate.name())
              .startDate(candidate.startDate())
              .endDate(candidate.endDate())
              .source(candidate.source())
              .error(error)
              .build();
    }

    importedEvent = importedEventRepository.save(importedEvent);
    failedEvents.add(new FailedImportedEventDto(importedEvent));
  }

  private String mapError(Exception e) {
    if (e instanceof NotAvailableDatesException) {
      return CodeErrors.NOT_AVAILABLE_DATES;
    } else if (e instanceof PrevOfInProgressCannotBePendingOrInProgress) {
      return CodeErrors.PREV_OF_INPROGRESS_CANNOT_BE_PENDING_OR_INPROGRESS;
    } else if (e instanceof PrevOfFinishedCannotBeNotPendingOrInProgress) {
      return CodeErrors.PREV_OF_FINISHED_CANNOT_BE_PENDING_OR_INPROGRESS;
    } else if (e instanceof NextOfPendingCannotBeInprogressOrFinished) {
      return CodeErrors.NEXT_OF_PENDING_CANNOT_BE_INPROGRESS_OR_FINISHED;
    } else if (e instanceof NextOfInProgressCannotBeFinishedOrInProgress) {
      return CodeErrors.NEXT_OF_INPROGRESS_CANNOT_BE_FINISHED_OR_INPROGRESS;
    } else if (e instanceof EntityFrozenException) {
      return CodeErrors.ENTITY_FROZEN;
    } else if (e instanceof EntityNotFoundException) {
      return CodeErrors.UNRECOGNIZED_APARTMENT_ID;
    } else {
      log.error("Unexpected error creating event from import: {}", e.getMessage(), e);
      return CodeErrors.UNEXPECTED_ERROR;
    }
  }

  private List<ImportCandidate> parseAirbnb(File importFile) throws IOException, CsvException {
    try (BufferedReader br = Files.newBufferedReader(importFile.toPath(), StandardCharsets.UTF_8)) {
      CSVParser parser = new CSVParserBuilder().withSeparator(AIRBNB_SEPARATOR).build();
      try (CSVReader csvReader =
          new CSVReaderBuilder(br).withSkipLines(1).withCSVParser(parser).build()) {
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

                  Instant startDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[AIRBNB_START_DATE_POSITION] + " " + AIRBNB_CHECKIN_TIME));
                  Instant endDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[AIRBNB_END_DATE_POSITION] + " " + AIRBNB_CHECKOUT_TIME));

                  return new ImportCandidate(
                      null,
                      apartment,
                      line[AIRBNB_GUEST_POSITION],
                      startDate,
                      endDate,
                      EventSource.AIRBNB);
                })
            .toList();
      }
    }
  }

  private List<ImportCandidate> parseBooking(File importFile) throws IOException, CsvException {
    try (BufferedReader br = Files.newBufferedReader(importFile.toPath(), StandardCharsets.UTF_8)) {
      CSVParser parser = new CSVParserBuilder().withSeparator(BOOKING_SEPARATOR).build();
      try (CSVReader csvReader =
          new CSVReaderBuilder(br).withSkipLines(1).withCSVParser(parser).build()) {
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

                  Instant startDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[BOOKING_START_DATE_POSITION] + " " + BOOKING_CHECKIN_TIME));
                  Instant endDate =
                      Instant.from(
                          dateFormatter.parse(
                              line[BOOKING_END_DATE_POSITION] + " " + BOOKING_CHECKOUT_TIME));

                  return new ImportCandidate(
                      null,
                      apartment,
                      line[BOOKING_GUEST_POSITION],
                      startDate,
                      endDate,
                      EventSource.BOOKING);
                })
            .filter(Objects::nonNull)
            .toList();
      }
    }
  }
}
