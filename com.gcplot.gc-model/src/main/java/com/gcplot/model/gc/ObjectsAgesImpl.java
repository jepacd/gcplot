package com.gcplot.model.gc;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 *         8/4/16
 */
public class ObjectsAgesImpl implements ObjectsAges {

    @Override
    public long desiredSurvivorSize() {
        return desiredSurvivorSize;
    }
    public ObjectsAgesImpl desiredSurvivorSize(long desiredSurvivorSize) {
        this.desiredSurvivorSize = desiredSurvivorSize;
        return this;
    }

    @Override
    public List<Long> occupied() {
        return Collections.unmodifiableList(occupied);
    }
    public ObjectsAgesImpl occupied(List<Long> occupied) {
        this.occupied = occupied;
        return this;
    }

    @Override
    public List<Long> total() {
        return Collections.unmodifiableList(total);
    }
    public ObjectsAgesImpl total(List<Long> total) {
        this.total = total;
        return this;
    }

    @Override
    public String ext() {
        return ext;
    }
    public ObjectsAgesImpl ext(String ext) {
        this.ext = ext;
        return this;
    }

    @Override
    public DateTime occurred() {
        return occurred;
    }
    public ObjectsAgesImpl occurred(DateTime occurred) {
        this.occurred = occurred;
        return this;
    }

    @Override
    public String jvmId() {
        return jvmId;
    }
    public ObjectsAgesImpl jvmId(String jvmId) {
        this.jvmId = jvmId;
        return this;
    }

    @Override
    public String analyseId() {
        return analyseId;
    }
    public ObjectsAgesImpl analyseId(String analyseId) {
        this.analyseId = analyseId;
        return this;
    }

    private long desiredSurvivorSize;
    private List<Long> occupied;
    private List<Long> total;
    private String ext;
    private DateTime occurred;
    private String jvmId;
    private String analyseId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObjectsAgesImpl that = (ObjectsAgesImpl) o;

        if (desiredSurvivorSize != that.desiredSurvivorSize) return false;
        if (occupied != null ? !occupied.equals(that.occupied) : that.occupied != null) return false;
        if (total != null ? !total.equals(that.total) : that.total != null) return false;
        if (ext != null ? !ext.equals(that.ext) : that.ext != null) return false;
        if (occurred != null ? !occurred.equals(that.occurred) : that.occurred != null) return false;
        if (jvmId != null ? !jvmId.equals(that.jvmId) : that.jvmId != null) return false;
        return analyseId != null ? analyseId.equals(that.analyseId) : that.analyseId == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (desiredSurvivorSize ^ (desiredSurvivorSize >>> 32));
        result = 31 * result + (occupied != null ? occupied.hashCode() : 0);
        result = 31 * result + (total != null ? total.hashCode() : 0);
        result = 31 * result + (ext != null ? ext.hashCode() : 0);
        result = 31 * result + (occurred != null ? occurred.hashCode() : 0);
        result = 31 * result + (jvmId != null ? jvmId.hashCode() : 0);
        result = 31 * result + (analyseId != null ? analyseId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ObjectsAgesImpl{");
        sb.append("desiredSurvivorSize=").append(desiredSurvivorSize);
        sb.append(", occupied=").append(occupied);
        sb.append(", total=").append(total);
        sb.append(", ext='").append(ext).append('\'');
        sb.append(", occurred=").append(occurred);
        sb.append(", jvmId='").append(jvmId).append('\'');
        sb.append(", analyseId='").append(analyseId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
