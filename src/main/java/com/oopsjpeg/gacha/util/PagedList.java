package com.oopsjpeg.gacha.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class PagedList<T>
{
    private List<T> original;

    private Predicate<T> predicate;
    private Comparator<T> comparator;

    private int page;
    private int itemsPerPage;

    public PagedList(Collection<T> original)
    {
        this(original, 10);
    }

    public PagedList(Collection<T> original, int itemsPerPage)
    {
        this.original = new LinkedList<>(original);
        this.itemsPerPage = itemsPerPage;
    }

    public PagedList<T> filter(Predicate<T> predicate)
    {
        this.predicate = predicate;
        return this;
    }

    public PagedList<T> sort(Comparator<T> comparator)
    {
        this.comparator = comparator;
        return this;
    }

    public PagedList<T> page(int page)
    {
        this.page = page;
        return this;
    }

    public int pages()
    {
        return (int) Math.ceil((float) original.size() / itemsPerPage);
    }

    public List<T> get()
    {
        int from = page * 10;
        int to = from + 10;

        List<T> paged = original.subList(from, Math.min(size(), to));
        if (predicate != null) paged.removeIf(predicate.negate());
        if (comparator != null) paged.sort(comparator);

        return paged;
    }

    public int size()
    {
        return original.size();
    }

    public List<T> getOriginal()
    {
        return original;
    }

    @Override
    public String toString()
    {
        return get().toString();
    }
}
