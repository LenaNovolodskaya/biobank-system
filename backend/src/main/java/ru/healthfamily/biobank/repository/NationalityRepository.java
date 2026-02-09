package ru.healthfamily.biobank.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.healthfamily.biobank.model.Nationality;
import java.util.List;

@Repository
public interface NationalityRepository extends JpaRepository<Nationality, Long> {
    List<Nationality> findAllByOrderByNationalityNameAsc();
}
