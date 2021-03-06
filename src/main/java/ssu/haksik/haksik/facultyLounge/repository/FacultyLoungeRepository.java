package ssu.haksik.haksik.facultyLounge.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ssu.haksik.haksik.common.enums.EatingTime;
import ssu.haksik.haksik.facultyLounge.entity.FacultyLounge;

public interface FacultyLoungeRepository extends JpaRepository<FacultyLounge, Long> {
    FacultyLounge findByEatingTime(EatingTime eatingTime);
}
