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
 * User entity representing app users who track their book reading.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<UserBookTracking> readingList = new ArrayList<>();
    
    // Convenience method to add tracking entry
    public void addBookTracking(UserBookTracking tracking) {
        readingList.add(tracking);
        tracking.setUser(this);
    }
    
    // Convenience method to remove tracking entry
    public void removeBookTracking(UserBookTracking tracking) {
        readingList.remove(tracking);
        tracking.setUser(null);
    }
} 