package ru.optimus.shedulerapi.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Pair<F, S> {

  private F first;
  private S second;


  public static <F, S> Pair<F, S> of(F first, S second){
    return new Pair<>(first, second);
  }

}
