package com.example.demo.model;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import lombok.Data;
@Data
public class Expression {
    private String name;/*key*/
    private SQLBinaryOperator outoperator;/*表达式关系*/
    private SQLBinaryOperator operator; /*键值关系*/
    private Object Values;/*val*/
}