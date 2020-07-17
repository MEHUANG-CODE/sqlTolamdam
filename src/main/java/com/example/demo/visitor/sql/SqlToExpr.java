package com.example.demo.visitor.sql;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.statement.SQLSelectQuery;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.example.demo.model.Expression;
import java.util.ArrayList;
import java.util.List;


/*
select col1,col2 from table1 where (col3 = 'val3' or col8 = '1' and  col8 = '2' )   or   (col6 ='6' and  col7 ='7')   and   (col4 ='4' and  col8 ='8')
二叉树解析顺序：
        col3 = 'val3'
        col8 = '1'
        col8 = '2'
        col8 = '1' and  col8 = '2'
        二级表达式解析： col3 = 'val3' or col8 = '1' and  col8 = '2'
        col6 ='6'
        col7 ='7'
        二级表达式解析：col6 ='6' and  col7 ='7'
        col4 ='4'
        col8 ='8'
        col4 ='4' and  col8 ='8
        二级表达式解析： (col6 ='6' and  col7 ='7')   and   (col4 ='4' and  col8 ='8')
*/


/*where 条件解析*/
public class SqlToExpr implements ISqlToExpr  {

    private String sqlstring = "";

    public List<Expression> parse(String Sql) {
        sqlstring = Sql;
        SQLStatementParser parser = new MySqlStatementParser(sqlstring);
        SQLSelectStatement stmt = (SQLSelectStatement) parser.parseStatement();
        SQLSelectQuery sqlSelectQuery = stmt.getSelect().getQuery();
        List<Expression> expressions = new ArrayList<>();
        // 非union的查询语句
        if (sqlSelectQuery instanceof SQLSelectQueryBlock) {
            SQLSelectQueryBlock sqlSelectQueryBlock = (SQLSelectQueryBlock) sqlSelectQuery;
            // 获取where条件
            SQLExpr where = sqlSelectQueryBlock.getWhere();
            // 如果是二元表达式
            if (where instanceof SQLBinaryOpExpr) {
                /*最后一个表达式捕获*/
                Expression expression=parseSqlWhereToSolr(where, expressions);
                /*无表形式时追加最后一个作为表达式*/
                if(expressions.size()==0){
                    expressions.add(expression);
                }else {
                    /*根据二叉树把最后一个关系[and or ]放到最后的一个表达式中*/
                    for (int i = expressions.size()-1 ; i >-1; i--) {
                        if (expressions.get(i).getOutoperator() == null) {
                            expressions.get(i).setOutoperator(expression.getOperator());
                            break;
                        }
                    }
                }
            }
            // union的查询语句
        } else if (sqlSelectQuery instanceof SQLUnionQuery) {
            // 处理---------------------
            System.out.println("不支持union的查询语句");
        }
        return   expressions;
    }
    public Expression parseSqlWhereToSolr(SQLExpr where, List<Expression> expressions) {
        Expression expression = new Expression();
        if (where instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr sqlBinaryOpExpr = (SQLBinaryOpExpr) where;
            SQLExpr left = sqlBinaryOpExpr.getLeft();
            SQLBinaryOperator operator = sqlBinaryOpExpr.getOperator();
            SQLExpr right = sqlBinaryOpExpr.getRight();

            // 去掉查询语句用来拼接条件的1=1 1=0之类的无意义表达式
            if (left instanceof SQLIntegerExpr && right instanceof SQLIntegerExpr) {
                return expression;
            }
            Expression leftStr = parseSQLExprToSolr(left, expressions);
            Expression rightStr = parseSQLExprToSolr(right, expressions);
            String opr = " " + operator.name + " ";

            if (leftStr.getValues() != null && leftStr.getValues().toString().length() > 0) {
                if (operator == SQLBinaryOperator.BooleanAnd || operator == SQLBinaryOperator.BooleanOr) {
                    expression.setOperator(operator);
                    expression.setName("");
                    expression.setValues("");
                } else {
                    expression.setOperator(operator);
                    expression.setName(leftStr.getValues().toString());
                    expression.setValues(rightStr.getValues());
                }
            } else {
                expression.setOperator(operator);
                expression.setName("");
                expression.setValues("");
            }
            // 处理---------------------
            // 如果是子查询
        } else {
            System.out.println("暂不解析子查询");
        }

        return expression;
    }

    public Expression parseSQLExprToSolr(SQLExpr expr, List<Expression> expressions) {
        Expression expression = new Expression();
        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) expr;
            if (binaryOpExpr.isBracket()) { //二级表达式解析

                List<Expression> expressions2 = new ArrayList<>();
                expression = parseSqlWhereToSolr(binaryOpExpr, expressions2);
                if (expression.getOperator() == SQLBinaryOperator.BooleanAnd || expression.getOperator() == SQLBinaryOperator.BooleanOr) {
                    for (int i = expressions.size() - 1; i >= 0; i--) { /*关系表达式跟随最外层二级表达式组*/
                        if (expressions.get(i).getOutoperator() == null) {
                            expressions.get(i).setOutoperator(expression.getOperator());
                            break;
                        }
                    }
                }
                for (int i = expressions2.size() - 1; i >= 0; i--) {
                    if (expressions2.get(i).getOutoperator() == null) {
                        expressions2.get(i).setOutoperator(expression.getOperator());
                        break;
                    }
                }
                Expression expression1 = new Expression();
                expression1.setValues(expressions2);
                expressions.add(expression1);

            } else {
                expression = parseSqlWhereToSolr(binaryOpExpr, expressions);
                /*区分关系表达式还是键值关系*/
                if (expression.getOperator() == SQLBinaryOperator.BooleanAnd || expression.getOperator() == SQLBinaryOperator.BooleanOr) {
                    for (int i = expressions.size() - 1; i >= 0; i--) {
                        if (expressions.get(i).getOutoperator() == null && i != 0) {/*i！=0 解决二叉树最外层的表达式放在函数最外层获取*/
                            expressions.get(i).setOutoperator(expression.getOperator());
                            break;
                        }
                    }
                } else {
                    expressions.add(expression);
                }
            }
        } else if (expr instanceof SQLCharExpr) {
            SQLCharExpr c = (SQLCharExpr) expr;
            expression.setValues(c.getValue());
        } else {
            expression.setValues(expr.toString());
        }
        return expression;
    }
}
