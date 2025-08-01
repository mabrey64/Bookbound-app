package com.bookbound.repository;

import com.bookbound.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Store entity operations.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, String> {
    
    /**
     * Find stores by store name.
     * @param storeName the store name to search for
     * @return list of stores with matching name
     */
    List<Store> findByStoreName(String storeName);
    
    /**
     * Find stores by book ID.
     * @param bookId the book ID
     * @return list of stores selling the specified book
     */
    @Query("SELECT s FROM Store s WHERE s.book.id = :bookId")
    List<Store> findByBookId(@Param("bookId") String bookId);
    
    /**
     * Find stores by store name containing the given string (case-insensitive).
     * @param storeName the store name fragment to search for
     * @return list of matching stores
     */
    List<Store> findByStoreNameContainingIgnoreCase(String storeName);
} 