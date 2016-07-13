package com.gcplot.model.gc;

import com.google.common.base.Preconditions;

import java.util.List;

public class CapacityImpl implements Capacity {

    @Override
    public long usedBefore() {
        return usedBefore;
    }
    public void usedBefore(long usedBefore) {
        this.usedBefore = usedBefore;
    }

    @Override
    public long usedAfter() {
        return usedAfter;
    }
    public void usedAfter(long usedAfter) {
        this.usedAfter = usedAfter;
    }

    @Override
    public long total() {
        return total;
    }
    public void total(long total) {
        this.total = total;
    }

    protected long usedBefore;
    protected long usedAfter;
    protected long total;

    public CapacityImpl(List<Long> list) {
        Preconditions.checkState(list.size() == 3, "Wrong input list: {}", list);
        this.usedBefore = list.get(0);
        this.usedAfter = list.get(1);
        this.total = list.get(2);
    }

    public CapacityImpl(long usedBefore, long usedAfter, long total) {
        this.usedBefore = usedBefore;
        this.usedAfter = usedAfter;
        this.total = total;
    }

    public CapacityImpl(Capacity capacity) {
        this.usedBefore = capacity.usedBefore();
        this.usedAfter = capacity.usedAfter();
        this.total = capacity.total();
    }

    public CapacityImpl() {
    }

    public static CapacityImpl of(long usedBefore, long usedAfter, long total) {
        return new CapacityImpl(usedBefore, usedAfter, total);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CapacityImpl capacity = (CapacityImpl) o;

        if (usedBefore != capacity.usedBefore) return false;
        if (usedAfter != capacity.usedAfter) return false;
        return total == capacity.total;

    }

    @Override
    public int hashCode() {
        int result = (int) (usedBefore ^ (usedBefore >>> 32));
        result = 31 * result + (int) (usedAfter ^ (usedAfter >>> 32));
        result = 31 * result + (int) (total ^ (total >>> 32));
        return result;
    }
}