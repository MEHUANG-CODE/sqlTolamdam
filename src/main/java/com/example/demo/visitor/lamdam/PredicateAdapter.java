package com.example.demo.visitor.lamdam;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.example.demo.model.Expression;
import lombok.Data;
import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


import static com.alibaba.druid.sql.ast.expr.SQLBinaryOperator.BooleanAnd;
import static com.alibaba.druid.sql.ast.expr.SQLBinaryOperator.BooleanOr;

/*lamdam 表达式识别器*/

@Data
public class PredicateAdapter<D>  implements  IPredicateAdapter<D> {
    /*数据过滤*/
    public List<D> fiterData(List<D> obj, List<Expression> expressions) {
        return (List<D>) obj.stream().filter(PredicateCreate(expressions)).collect(Collectors.toList());
    }

    /*创建表达式 只能到达二维，多维需要用递归*/
    public Predicate PredicateCreate(List<Expression> expressions) {
        Predicate pre = (n) -> {
            return true;
        };
        int i = 0;
        for (Expression item : expressions) {
            if (item.getValues() instanceof List) {
                Predicate prelist = (n) -> {
                    return true;
                };
                int k = 0;
                for (Expression it : (List<Expression>) item.getValues()) {
                    Predicate priit = getpre(it);
                    prelist = getprejoin(k, prelist, priit, it);
                    ++k;
                }
                pre = getprejoin(i, pre, prelist, item);
            } else {
                Predicate pritem = getpre(item);
                pre = getprejoin(i, pre, pritem, item);
            }
            ++i;
        }
        return pre;
    }

    public Predicate getprejoin(int i, Predicate oldpre, Predicate pre, Expression item) {
        if (item.getOutoperator() == BooleanAnd || i == 0) { /*i==0 结构体第一个式不记录关系*/
            oldpre = oldpre.and(pre);
        } else if (item.getOutoperator() == BooleanOr) {
            oldpre = oldpre.or(pre);
        }
        return oldpre;
    }

    /*表达式主体*/
    public Predicate getpre(Expression item) {
        return (n) -> {
            try {
                Field field;
                if ((field = getField(n.getClass(), item.getName())) == null)
                    return false;
                field.setAccessible(true);
                boolean o = getType(field.get(n), item.getValues(), item.getOperator());
                System.out.println("比对：" + field.get(n) + "->" + item.getValues() + "->" + o);
                return o;
            } catch (Exception e) {
                System.out.println(e.fillInStackTrace());
                return false;
            }
        };
    }

    public Field getField(Class<?> clazz, String propertyName) {
        if (clazz == null)
            return null;
        try {
            return clazz.getDeclaredField(propertyName);
        } catch (NoSuchFieldException e) {
            return getField(clazz.getSuperclass(), propertyName);
        }
    }

    public boolean getType(Object obj, Object val, SQLBinaryOperator Operator) {
        if (obj instanceof Integer) {
            int i = Integer.parseInt(val.toString());
            return getExp(obj, i, Operator);

        } else if (obj instanceof Long) {
            Long i = Long.parseLong(val.toString());
            return getExp(obj, i, Operator);

        } else if (obj instanceof Short) {
            Short i = Short.parseShort(val.toString());
            return getExp(obj, i, Operator);

        } else if (obj instanceof Boolean) {
            Boolean i = Boolean.parseBoolean(val.toString());
            return getExp(obj, i, Operator);

        } else if (obj instanceof Byte) {
            Byte i = Byte.parseByte(val.toString());
            return getExp(obj, i, Operator);

        } else if (obj instanceof Character) {
            String i = val.toString();
            return getExp(obj, i, Operator);
        } else if (obj instanceof Double) {
            Double i = Double.parseDouble(val.toString());
            return getExp(obj, i, Operator);

        } else if (obj instanceof Float) {
            Float i = Float.parseFloat(val.toString());
            return getExp(obj, i, Operator);

        } else if (obj instanceof String) {
            String i = val.toString();
            return getExp(obj, i, Operator);
        } else {
            return getExp(obj, val, Operator);
        }
    }

    public boolean getExp(Object obj, Object val, SQLBinaryOperator operator) {

        switch (operator) {
            case Equality:
                return obj.equals(val);
            case NotEqual:
                return obj.toString() != val.toString();
            case Like:
                return obj.toString().contains((CharSequence) val);
            case GreaterThan:
                return Float.parseFloat(obj.toString()) - Float.parseFloat(val.toString()) > 0;
            case GreaterThanOrEqual:
                return Float.parseFloat(obj.toString()) - Float.parseFloat(val.toString()) >= 0;
            case LessThan:
                return Float.parseFloat(obj.toString()) - Float.parseFloat(val.toString()) < 0;
            case LessThanOrEqual:
                return Float.parseFloat(obj.toString()) - Float.parseFloat(val.toString()) <= 0;
            default:
                return obj.toString().equals(val);
        }
    }
}

