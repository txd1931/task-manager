package com.taskmanager.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Recurrence {

    private final long id;
    private final RecurrenceType type;
    private final int interval;
    private final LocalDate startDate;

    private boolean active;
    private final Set<LocalDate> generatedDates;
    
    private final Set<Long> processedOccurrenceIds;
    
    private final List<RecurrenceRevision> revisions;


    public Recurrence(
            long id,
            RecurrenceType type,
            int interval,
            LocalDate startDate) {

        if (id < 1) {
            throw new IllegalArgumentException(
                    "O identificador da recorrência deve ser positivo."
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "A periodicidade não pode ser nula."
            );
        }

        if (interval < 1) {
            throw new IllegalArgumentException(
                    "O intervalo deve ser maior que zero."
            );
        }

        if (startDate == null) {
            throw new IllegalArgumentException(
                    "A data inicial é obrigatória."
            );
        }

        this.id = id;
        this.type = type;
        this.interval = interval;
        this.startDate = startDate;
        this.active = true;
        this.generatedDates = new HashSet<>();
        this.processedOccurrenceIds = new HashSet<>();
        this.revisions = new ArrayList<>();

    }

    public long getId() {
        return id;
    }

    public RecurrenceType getType() {
        return type;
    }

    public int getInterval() {
        return interval;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public boolean isActive() {
        return active;
    }

    public void end() {
        active = false;
    }

    public boolean hasGeneratedDate(LocalDate date) {
        return generatedDates.contains(date);
    }

    public void registerGeneratedDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException(
                    "A data programada não pode ser nula."
            );
        }

        if (!generatedDates.add(date)) {
            throw new IllegalStateException(
                    "Já existe uma ocorrência registrada para essa data."
            );
        }
    }

    public LocalDate getDateAt(long occurrenceIndex) {
        if (occurrenceIndex < 0) {
            throw new IllegalArgumentException(
                    "O índice da ocorrência não pode ser negativo."
            );
        }

        long offset = Math.multiplyExact(
                occurrenceIndex,
                interval
        );

        switch (type) {
            case DAILY:
                return startDate.plusDays(offset);

            case WEEKLY:
                return startDate.plusWeeks(offset);

            case MONTHLY:
                return startDate.plusMonths(offset);

            default:
                throw new IllegalStateException(
                        "Periodicidade inválida."
                );
        }
    }

    public LocalDate getNextDate(LocalDate referenceDate) {
        if (referenceDate == null) {
            throw new IllegalArgumentException(
                    "A data de referência não pode ser nula."
            );
        }

        long low = 0;
        long high = 1;

        while (!getDateAt(high).isAfter(referenceDate)) {
            high = Math.multiplyExact(high, 2);
        }

        while (low < high) {
            long middle = low + (high - low) / 2;

            if (getDateAt(middle).isAfter(referenceDate)) {
                high = middle;
            } else {
                low = middle + 1;
            }
        }

        return getDateAt(low);
    }

	public boolean hasGeneratedSuccessor(long occurrenceId) {
	    return processedOccurrenceIds.contains(occurrenceId);
	}
	
	public void registerGeneratedSuccessor(long occurrenceId) {
	    if (occurrenceId < 1) {
	        throw new IllegalArgumentException(
	                "O identificador da ocorrência deve ser positivo."
	        );
	    }
	
	    if (!processedOccurrenceIds.add(occurrenceId)) {
	        throw new IllegalStateException(
	                "Esta ocorrência já gerou uma sucessora."
	        );
	    }
	}

	
	public void addRevision(RecurrenceRevision revision) {
	    if (revision == null) {
	        throw new IllegalArgumentException(
	                "A revisão não pode ser nula."
	        );
	    }
	
	    if (revision.getEffectiveDate().isBefore(startDate)) {
	        throw new IllegalArgumentException(
	                "A revisão não pode ser anterior ao início da recorrência."
	        );
	    }
	
	    LocalDate validDate = getNextDate(
	            revision.getEffectiveDate().minusDays(1)
	    );
	
	    if (!validDate.equals(revision.getEffectiveDate())) {
	        throw new IllegalArgumentException(
	                "A revisão deve começar em uma data programada da recorrência."
	        );
	    }
	
	    revisions.removeIf(
	            existing -> existing.getEffectiveDate()
	                    .equals(revision.getEffectiveDate())
	    );
	
	    revisions.add(revision);
	
	    revisions.sort(
	            Comparator.comparing(RecurrenceRevision::getEffectiveDate)
	    );
	}
	
	public RecurrenceRevision getRevisionFor(LocalDate scheduledDate) {
	    if (scheduledDate == null) {
	        throw new IllegalArgumentException(
	                "A data programada não pode ser nula."
	        );
	    }
	
	    RecurrenceRevision result = null;
	
	    for (RecurrenceRevision revision : revisions) {
	        if (revision.getEffectiveDate().isAfter(scheduledDate)) {
	            break;
	        }
	
	        result = revision;
	    }
	
	    if (result == null) {
	        throw new IllegalStateException(
	                "Nenhuma configuração foi encontrada para a ocorrência."
	        );
	    }
	
	    return result;
	}
	
	public List<RecurrenceRevision> getRevisions() {
	    return Collections.unmodifiableList(revisions);
	}
	
	
	public void replaceRevisionsFrom(RecurrenceRevision revision) {
	    if (revision == null) {
	        throw new IllegalArgumentException(
	                "A revisão não pode ser nula."
	        );
	    }
	
	    if (revision.getEffectiveDate().isBefore(startDate)) {
	        throw new IllegalArgumentException(
	                "A revisão não pode ser anterior ao início da recorrência."
	        );
	    }
	
	    LocalDate validDate = getNextDate(
	            revision.getEffectiveDate().minusDays(1)
	    );
	
	    if (!validDate.equals(revision.getEffectiveDate())) {
	        throw new IllegalArgumentException(
	                "A revisão deve começar em uma data programada da recorrência."
	        );
	    }
	
	    revisions.removeIf(
	            existing -> !existing.getEffectiveDate()
	                    .isBefore(revision.getEffectiveDate())
	    );
	
	    revisions.add(revision);
	
	    revisions.sort(
	            Comparator.comparing(RecurrenceRevision::getEffectiveDate)
	    );
	}
	

	public Set<LocalDate> getGeneratedDates() {
	    return Collections.unmodifiableSet(generatedDates);
	}
	
	public Set<Long> getProcessedOccurrenceIds() {
	    return Collections.unmodifiableSet(processedOccurrenceIds);
	}
	


}
