package com.bookbound.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Book entity representing books that users can track and read.
 */
@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    private String genre;
    
    @Column(nullable = false)
    private Integer totalPages;
    
    // Series information - nullable for standalone books
    private String seriesName;
    
    private Integer seriesOrder;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Store> purchaseLocations = new ArrayList<>();
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("book-tracking")
    private List<UserBookTracking> trackingEntries = new ArrayList<>();
    
    // Convenience method to add store
    public void addStore(Store store) {
        purchaseLocations.add(store);
        store.setBook(this);
    }
    
    // Convenience method to remove store
    public void removeStore(Store store) {
        purchaseLocations.remove(store);
        store.setBook(null);
    }
    
    // Convenience method to add tracking entry
    public void addTracking(UserBookTracking tracking) {
        trackingEntries.add(tracking);
        tracking.setBook(this);
    }
    
    // Convenience method to remove tracking entry
    public void removeTracking(UserBookTracking tracking) {
        trackingEntries.remove(tracking);
        tracking.setBook(null);
    }
} 