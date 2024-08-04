package org.ddd.application.query;

/**
 * @author <template/>
 * @date
 */
public interface Query<PARAM, RESULT> {
    RESULT exec(PARAM param);
}
