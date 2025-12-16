package com.sample.sampleservice.shared.pagination.domain;

import java.util.List;

public final class Page<T> {

    private static final int MINIMAL_PAGE_COUNT = 1;

    private int total;

    private int totalPages;

    private int currentPage;

    private boolean isLast;

    private boolean isEmpty;

    private boolean hasNext;

    private boolean hasPrevious;

    private List<T> content;

    public long getTotal() {
        return total;
    }

    public Page<T> total(int total) {
        this.total = total;
        return this;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public Page<T> totalPages(int totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public boolean isLast() {
        return isLast;
    }

    public Page<T> isLast(boolean last) {
        isLast = last;
        return this;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public Page<T> isEmpty(boolean empty) {
        isEmpty = empty;
        return this;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public Page<T> hasNext(boolean hasNext) {
        this.hasNext = hasNext;
        return this;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public Page<T> hasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
        return this;
    }

    public List<T> getContent() {
        return content;
    }

    public Page<T> content(List<T> content) {
        this.content = content;
        return this;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPages) {
        this.currentPage = currentPages;
    }

    public Page<T> currentPage(int currentPages) {
        this.currentPage = currentPages;
        return this;
    }
}
