package ${basePackage}.adapter.infra.mybatis._share;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mybatis枚举转化
 *
 * @author cap4j-ddd-codegen
 */
public class MyEnumTypeHandler <E extends Enum<E>> extends BaseTypeHandler<E> {
    private static final String ENUM_PERSIST_FIELD_METHOD = "getCode";
    private Integer getEnumCode(E e){
        Integer code = 0;
        for (String fm : ENUM_PERSIST_FIELD_METHOD.split(",")) {
            try {
                Method m = e.getClass().getMethod(fm);
                if (m != null) {
                    code = (Integer) m.invoke(e);
                    break;
                }
            } catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(ex);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException(ex);
            }
        }
        return code;
    }

    private Map<Integer, E> enums;

    public MyEnumTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        } else {
            this.enums = Arrays.stream(type.getEnumConstants()).collect(Collectors.toMap(e -> getEnumCode((E) e), e -> (E) e));
            if (this.enums == null) {
                throw new IllegalArgumentException(type.getSimpleName() + " does not represent an enum type.");
            }
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, E e, JdbcType jdbcType) throws SQLException {
        int val = getEnumCode(e);
        preparedStatement.setInt(i, val);
    }

    @Override
    public E getNullableResult(ResultSet resultSet, String s) throws SQLException {
        int ordinal = resultSet.getInt(s);
        return this.enums.get(ordinal);
    }

    @Override
    public E getNullableResult(ResultSet resultSet, int i) throws SQLException {
        int ordinal = resultSet.getInt(i);
        return this.enums.get(ordinal);
    }

    @Override
    public E getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        int ordinal = callableStatement.getInt(i);
        return this.enums.get(ordinal);
    }
}
