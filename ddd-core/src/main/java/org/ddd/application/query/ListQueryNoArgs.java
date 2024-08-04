package org.ddd.application.query;

import java.util.List;

/**
 * @author <template/>
 * @date
 */
public abstract class ListQueryNoArgs<RESUTL> implements ListQuery<Void,RESUTL> {
    @Override
    public List<RESUTL> exec(Void aVoid) {
        return query();
    }

    public abstract List<RESUTL> query();
}
