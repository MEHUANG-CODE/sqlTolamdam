package com.example.demo.model;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import lombok.Data;
import java.util.List;

@Data
public class Expressionlist {
    private SQLBinaryOperator operator;/*表达式关系*/
    private List<Expression> Values;/*关系组*/
}
