package org.netcorepal.cap4j.ddd.application.query;

/**
 * 无参查询
 *
 * @author binking338
 * @date
 */
public abstract class QueryNoArgs<RESULT> implements Query<Void, RESULT> {
    @Override
    public RESULT exec(Void aVoid) {
        return query();
    }

    public abstract RESULT query();
}
