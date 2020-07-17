package com.example.demo;

import com.example.demo.model.Expression;
import com.example.demo.model.TestData;
import com.example.demo.visitor.lamdam.IPredicateAdapter;
import com.example.demo.visitor.lamdam.PredicateAdapter;
import com.example.demo.visitor.sql.ISqlToExpr;
import com.example.demo.visitor.sql.SqlToExpr;
import com.google.gson.Gson;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

/**
 *  根据sql语句从左到右过滤
 *  主要可以解释json转换为实体模型组，通过lamdam进行筛选
 *
 */
@SpringBootApplication
public class DemoApplication {
    /*sql 转换成 lamdam*/
    public static void main(String[] args) {
        /*测试数据库表达式*/
        // String sql = "select  *  from table where   name='Ts7' and id=7 and ( id=9 or id=6) ";
        // String sql = "select  *  from table where   name='Ts7' and id=7  or id=6 or  name='Ts8'";
        String sql = "select  *  from table where   name='Ts7'   or ( id=6 and    name='Ts6' ) and   id>5  or id>30";

        ISqlToExpr isqltoexp = new SqlToExpr();
        List<Expression> expressions = isqltoexp.parse(sql);


        List<TestData> t = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            int finalI = i;
            t.add(new TestData() {{
                setId(finalI);
                setName("Ts" + finalI);
            }});
        }

        IPredicateAdapter ipr = new PredicateAdapter<TestData>();
        List<TestData> txx = ipr.fiterData(t, expressions);


        Gson gson = new Gson();
        System.out.println(gson.toJson(expressions));
        System.out.println("Hello World!");
    }
}

