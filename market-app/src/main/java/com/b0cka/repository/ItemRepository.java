package com.b0cka.repository;

import com.b0cka.models.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {

    @Query("""
SELECT id, title, description, img_path, price
FROM items
WHERE title ILIKE ('%' || :q || '%')
   OR COALESCE(description,'') ILIKE ('%' || :q || '%')
ORDER BY title
LIMIT :limit OFFSET :offset
""")
    Flux<Item> searchAlpha(String q, int limit, long offset);

    @Query("""
SELECT id, title, description, img_path, price
FROM items
WHERE title ILIKE ('%' || :q || '%')
   OR COALESCE(description,'') ILIKE ('%' || :q || '%')
ORDER BY price
LIMIT :limit OFFSET :offset
""")
    Flux<Item> searchPrice(String q, int limit, long offset);

    @Query("""
SELECT id, title, description, img_path, price
FROM items
WHERE title ILIKE ('%' || :q || '%')
   OR COALESCE(description,'') ILIKE ('%' || :q || '%')
ORDER BY id
LIMIT :limit OFFSET :offset
""")
    Flux<Item> searchNoSort(String q, int limit, long offset);

}
