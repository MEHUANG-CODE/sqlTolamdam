package com.example.demo.visitor.sql;

import com.example.demo.model.Expression;

import java.util.List;

public interface ISqlToExpr {
    public List<Expression> parse(String Sql);
}
