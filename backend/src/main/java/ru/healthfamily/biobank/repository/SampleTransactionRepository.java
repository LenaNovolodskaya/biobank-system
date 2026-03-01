package ru.healthfamily.biobank.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.SampleTransaction;

import java.util.List;

@Repository
public interface SampleTransactionRepository extends JpaRepository<SampleTransaction, Long> {

    @Query("SELECT t FROM SampleTransaction t " +
           "LEFT JOIN FETCH t.sample " +
           "LEFT JOIN FETCH t.user " +
           "LEFT JOIN FETCH t.transactionType " +
           "LEFT JOIN FETCH t.department " +
           "ORDER BY t.transactionDate DESC")
    List<SampleTransaction> findAllWithDetails();

    @Query("SELECT t FROM SampleTransaction t " +
           "LEFT JOIN FETCH t.sample s " +
           "LEFT JOIN FETCH t.user " +
           "LEFT JOIN FETCH t.transactionType " +
           "LEFT JOIN FETCH t.department " +
           "WHERE s.sampleId = :sampleId " +
           "ORDER BY t.transactionDate DESC")
    List<SampleTransaction> findBySampleIdWithDetails(@Param("sampleId") Long sampleId);

    @Query("SELECT t FROM SampleTransaction t " +
           "LEFT JOIN FETCH t.sample " +
           "LEFT JOIN FETCH t.specimen sp " +
           "LEFT JOIN FETCH t.user " +
           "LEFT JOIN FETCH t.transactionType " +
           "LEFT JOIN FETCH t.department " +
           "WHERE sp.specimenId = :specimenId " +
           "ORDER BY t.transactionDate ASC")
    List<SampleTransaction> findBySpecimenIdWithDetails(@Param("specimenId") Long specimenId);
}
