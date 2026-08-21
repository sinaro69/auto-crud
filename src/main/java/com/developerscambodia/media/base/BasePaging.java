package com.developerscambodia.media.base;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class BasePaging<T> {
    private int currentPage;
    private long totalItems;
    private int totalPages;
    private List<T> items;
    private boolean hasNext;
}
