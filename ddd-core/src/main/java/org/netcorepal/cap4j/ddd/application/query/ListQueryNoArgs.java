package org.netcorepal.cap4j.ddd.application.query;

import java.util.List;

/**
 * 无参列表查询
 *
 * @author binking338
 * @date
 */
public abstract class ListQueryNoArgs<RESUTL> implements ListQuery<Void,RESUTL> {
    @Override
    public List<RESUTL> exec(Void aVoid) {
        return query();
    }

    public abstract List<RESUTL> query();
}
