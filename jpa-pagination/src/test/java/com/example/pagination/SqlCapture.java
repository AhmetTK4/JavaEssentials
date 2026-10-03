package com.example.pagination;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.resource.jdbc.spi.StatementInspector;
public class SqlCapture implements StatementInspector {
    private static final ThreadLocal<List<String>> SQL = ThreadLocal.withInitial(ArrayList::new);
    public String inspect(String sql) { SQL.get().add(sql.toLowerCase()); return sql; }
    static List<String> statements() { return List.copyOf(SQL.get()); }
    static void clear() { SQL.remove(); }
}
