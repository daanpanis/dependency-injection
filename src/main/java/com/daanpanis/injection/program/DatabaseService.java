package com.daanpanis.injection.program;

import java.util.List;
import java.util.function.Predicate;

public interface DatabaseService {

    List<Object[]> query(String table, Predicate<Object[]> matcher);

    void addRow(String table, Object[] row);

}
