package com.example.demo.visitor.lamdam;

import com.example.demo.model.Expression;

import java.util.List;

public interface IPredicateAdapter<D> {
   public List<D> fiterData(List<D> obj, List<Expression> expressions);
}
