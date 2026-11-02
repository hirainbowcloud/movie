package com.example.movie.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.movie.model.TicketType;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType,Integer> {

	Optional<TicketType> findByTypeName(String typeName);
}
