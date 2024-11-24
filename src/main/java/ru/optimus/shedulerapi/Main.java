package ru.optimus.shedulerapi;

import org.bukkit.plugin.java.JavaPlugin;
import ru.optimus.shedulerapi.adapters.AdapterFindClasses;

public class Main extends JavaPlugin {

  @Override
  public void onEnable() {
    new AdapterFindClasses(this, "ru.optimus");
  }
}