package com.dgut.online.jpa.extend;

import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BaseEntityQueryBuilder
 * @Description: TODO
 * @Author Administrator
 * @Date 2019/11/17 0017
 * @Version V1.0
 **/
@Data
public class BaseEntityQueryBuilder {
    private boolean withIn;
    private String inField;
    private List<String> inValue = Collections.EMPTY_LIST;
    private Class<?> resultClass;

    protected static String[] initResultFields(Class<?> resultClass){
        List<String> fieldNames = Arrays.asList(resultClass.getFields()).stream()
                .map(f -> f.getName()).collect(Collectors.toList());
        return fieldNames.toArray(new String[fieldNames.size()]);
    }
}
