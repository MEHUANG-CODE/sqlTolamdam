# SQL 条件 转换成lamdam表达式
测试代码

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