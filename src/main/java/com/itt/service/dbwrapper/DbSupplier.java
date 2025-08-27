package com.itt.service.dbwrapper;

@FunctionalInterface
public interface DbSupplier<T> {
    T get() throws Exception;
}
