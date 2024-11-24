package ru.optimus.shedulerapi.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class Value<V> {

  private V value;


  public static <V> Value<V> of(V value){
    return new Value<>(value);
  }

}
