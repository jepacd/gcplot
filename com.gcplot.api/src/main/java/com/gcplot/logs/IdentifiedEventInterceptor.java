package com.gcplot.logs;

import com.gcplot.model.IdentifiedEvent;
import com.gcplot.model.gc.analysis.GCAnalyse;
import com.gcplot.model.gc.GCEvent;

/**
 * @author <a href="mailto:art.dm.ser@gmail.com">Artem Dmitriev</a>
 * 10/22/17
 */
public interface IdentifiedEventInterceptor {

    boolean isActive();

    void intercept(IdentifiedEvent gcEvent);

    void finish();

}
