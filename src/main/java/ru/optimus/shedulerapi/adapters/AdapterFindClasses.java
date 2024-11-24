package ru.optimus.shedulerapi.adapters;


import com.google.common.hash.Hashing;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.optimus.shedulerapi.mappings.MethodMappingGenerator;
import ru.optimus.shedulerapi.model.Schedule;

@Getter
public class AdapterFindClasses implements IAdapter {

  private final JavaPlugin plugin;
  private final HashMap<String, Class<?>> hashedClasses = new HashMap<>();
  private final HashMap<Method, String> mappingMethods = new HashMap<>();
  private final String _package;
  private final ScheduleAdapter scheduleAdapter;


  public AdapterFindClasses(JavaPlugin plugin) {
    this(plugin, plugin.getDescription().getMain());
  }

  public AdapterFindClasses(JavaPlugin plugin, String _package) {
    this(plugin, _package, 100L);
  }

  public AdapterFindClasses(JavaPlugin plugin, String _package, long nextTime){
    this.plugin = plugin;
    this._package = _package;
    this.scheduleAdapter = new ScheduleAdapter(plugin);
    new BukkitRunnable(){
      @Override
      public void run() {
        loadProcess();
      }
    }.runTaskLater(plugin, nextTime);
  }

  @SneakyThrows
  @Override
  public void run() {
    Iterator<Class<?>> findClasses = ClassScanner.findClassesInPackage(_package)
        .stream()
        .filter(aClass -> {
          HashMap<Method, String> mapMappings = new HashMap<>();

          for (Method declaredMethod : aClass.getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(Schedule.class)) {
              String mappings =  scheduleAdapter.getMapping(aClass, declaredMethod);
              mapMappings.put(declaredMethod, mappings);
            }
          }

          mappingMethods.putAll(mapMappings);

          return !mapMappings.isEmpty();
        }).iterator();


    while (findClasses.hasNext()) {
      Class<?> clazz = findClasses.next();
      final String hash =  scheduleAdapter.hashing(clazz);
      hashedClasses.put(hash, clazz);
    }
  }



  void loadProcess() {
    this.run();

    for (Class<?> value : hashedClasses.values()) {

      String hashing = scheduleAdapter.hashing(value);
      Class<?> clazz = hashedClasses.get(hashing);
      if (clazz != null) {

        for (Method declaredMethod : clazz.getDeclaredMethods()) {
          if (declaredMethod.isAnnotationPresent(Schedule.class)) {
            String mappings =  scheduleAdapter.getMapping(clazz, declaredMethod);
            if (mappingMethods.containsValue(mappings)) {
              scheduleAdapter.register(declaredMethod, mappings);
            }
          }
        }
      }
    }
    scheduleAdapter.mergeHashingClasses(hashedClasses);
  }
}
