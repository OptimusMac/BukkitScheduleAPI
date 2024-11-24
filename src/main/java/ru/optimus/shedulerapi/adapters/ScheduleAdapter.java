package ru.optimus.shedulerapi.adapters;

import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.google.common.hash.Hashing;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.optimus.shedulerapi.mappings.MethodMappingGenerator;
import ru.optimus.shedulerapi.model.Schedule;
import ru.optimus.shedulerapi.utils.Value;

class ScheduleAdapter {

  final Collection<Runnable> runnables = new ArrayList<>();
  private final JavaPlugin javaPlugin;
  private final HashMap<String, Class<?>> hashedClasses = new HashMap<>();
  private final HashMap<Method, String> mappings = new HashMap<>();
  private final HashMap<Method, Value<Long>> cronRunnable = new HashMap<>();

  public ScheduleAdapter(JavaPlugin javaPlugin) {
    this.javaPlugin = javaPlugin;
    this.processCronRunnable(true).runTaskTimerAsynchronously(javaPlugin, 20L, 20L);
    this.processCronRunnable(false).runTaskTimer(javaPlugin, 20L, 20L);
  }

  public void register(Method method, String mappings) {
    if (javaPlugin == null) {
      throw new RuntimeException("JavaPlugin in ScheduleAdapter is not register!");
    }
    this.mappings.put(method, mappings);
    registerAndPushRunnable(method);
  }


  private void registerAndPushRunnable(Method method) {
    long iteration = ejectScheduleTime(method);
    boolean cron = isCron(method);
    if (cron) {
      cronRunnable.put(method, Value.of(nextTimeCron(method)));
    } else {
      Runnable runnable = buildTask(method, iteration, isAsync(method));
      runnables.add(runnable);

    }

  }

  private BukkitRunnable processCronRunnable(boolean async) {
    return new BukkitRunnable() {
      @Override
      public void run() {
        cronRunnable.entrySet().stream()
            .filter(methodValueEntry -> {
              Schedule schedule = methodValueEntry.getKey().getAnnotation(Schedule.class);
              return schedule.async() == async;
            }).forEach(entry -> {
              long current = System.currentTimeMillis();

              if (entry.getValue().getValue() < current) {
                try {
                  entry.getKey().invoke(findClass(entry.getKey()).newInstance());
                  long nexTime = nextTimeCron(entry.getKey());
                  entry.getValue().setValue(nexTime);
                } catch (IllegalAccessException | InvocationTargetException |
                         InstantiationException e) {
                  throw new RuntimeException(e);
                }
              }
            });
      }
    };
  }

  private Runnable buildTask(Method method, long mills, boolean async) {
    BukkitRunnable runnable = new BukkitRunnable() {
      @Override
      public void run() {
        try {
          if (!Bukkit.getServer().getWorlds().isEmpty()) {
            method.invoke(findClass(method).newInstance());
          }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
          throw new RuntimeException(e);
        }
      }
    };
    if (async) {
      runnable.runTaskTimerAsynchronously(javaPlugin, 0, mills);
    } else {
      runnable.runTaskTimer(javaPlugin, 0, mills);
    }
    return runnable;
  }


  public void mergeHashingClasses(HashMap<String, Class<?>> map) {
    this.hashedClasses.putAll(map);
  }

  private long nextTimeCron(Method method) {
    Schedule schedule = method.getAnnotation(Schedule.class);
    return System.currentTimeMillis() + getNextScheduledTime(schedule.cron());
  }

  private Class<?> findClass(Method method) {

    String hash = mappings.get(method);

    for (Class<?> value : hashedClasses.values()) {
      for (Method declaredMethod : value.getDeclaredMethods()) {
        if (declaredMethod.isAnnotationPresent(Schedule.class)) {
          String hashing = getMapping(value, declaredMethod);
          if (hash.equals(hashing)) {
            return value;
          }
        }
      }

    }
    return null;
  }

  private boolean isAsync(Method method) {

    Schedule schedule = method.getAnnotation(Schedule.class);
    return schedule.async();

  }

  private boolean isCron(Method method) {

    Schedule schedule = method.getAnnotation(Schedule.class);
    return schedule.millis() <= 0;

  }


  @SneakyThrows
  public String getMapping(Class<?> clazz, Method method) {
    return MethodMappingGenerator.generateMethodMapping(clazz, method.getName());
  }


  @SneakyThrows
  public String hashing(Class<?> clazz) {
    byte[] bytes = ClassScanner.getClassAsByteArray(clazz);
    byte[] name = javaPlugin.getDescription().getName().getBytes(StandardCharsets.UTF_8);
    byte[] mergedArray = new byte[bytes.length + name.length];

    System.arraycopy(bytes, 0, mergedArray, 0, bytes.length);

    System.arraycopy(name, 0, mergedArray, bytes.length, name.length);

    return Hashing.hmacMd5(mergedArray).toString();
  }

  private long ejectScheduleTime(Method method) {
    Schedule schedule = method.getAnnotation(Schedule.class);
    long iteration;
    if (schedule.millis() > 1) {
      iteration = schedule.millis();
    } else {
      iteration = getNextScheduledTime(schedule.cron());
    }
    return convertMillisToTicks(iteration);
  }

  private long convertMillisToTicks(long millis) {
    return millis * 20 / 1000;
  }

  private long getNextScheduledTime(String cronExpression) {
    CronDefinition cronDefinition = CronDefinitionBuilder.defineCron().withSeconds().and()
        .withMinutes().and().withHours().and().withDayOfMonth().supportsHash().supportsL()
        .supportsW().and().withMonth().and().withDayOfWeek().withIntMapping(7, 0).supportsHash()
        .supportsL().supportsW().and().withYear().optional().and().instance();

    CronParser parser = new CronParser(cronDefinition);
    ExecutionTime executionTime = ExecutionTime.forCron(parser.parse(cronExpression));
    ZonedDateTime now = ZonedDateTime.now();

    Optional<Duration> timeToNextExecution = executionTime.timeToNextExecution(now);

    if (timeToNextExecution.isPresent()) {
      Duration duration = timeToNextExecution.get();
      return duration.toMillis();
    } else {
      System.err.println("No next execution time available.");
      return 0L;
    }
  }


}
