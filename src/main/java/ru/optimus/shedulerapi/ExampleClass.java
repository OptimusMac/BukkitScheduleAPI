package ru.optimus.shedulerapi;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import ru.optimus.shedulerapi.model.Schedule;

public class ExampleClass {


  @Schedule(millis = 1000)
  public void execute(){

    Server server = Bukkit.getServer();
    server.broadcast(Component.text("sosal?"));

  }

}
