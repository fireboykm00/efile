
package com.efile.core.department;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByName(String name);
    Optional<Department> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.head")
    List<Department> findAllWithHead();
    
    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.head WHERE d.id = :id")
    Optional<Department> findByIdWithHead(Long id);
}
