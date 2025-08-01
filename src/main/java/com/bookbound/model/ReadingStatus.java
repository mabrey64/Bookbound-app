package com.bookbound.model;

/**
 * Enum representing the reading status of a book for a user.
 * Used in UserBookTracking to track progress.
 */
public enum ReadingStatus {
    TO_READ,    // Book is marked to read next
    READING,    // Currently reading the book
    COMPLETED   // Finished reading the book
} 