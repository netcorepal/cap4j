package org.ddd.application.query;

/**
 * @author <template/>
 * @date
 */
public abstract class QueryNoArgs<RESULT> implements Query<Void, RESULT> {
    @Override
    public RESULT exec(Void aVoid) {
        return query();
    }

    public abstract RESULT query();
}
