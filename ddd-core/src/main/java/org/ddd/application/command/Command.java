package org.ddd.application.command;

/**
 * @author <template/>
 * @date
 */
public interface Command<PARAM, RESULT> {
    RESULT exec(PARAM param);
}
