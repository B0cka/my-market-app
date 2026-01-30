package com.b0cka.repository;

import com.b0cka.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description, Pageable pageable);

}
