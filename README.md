# 📜 ScheduleAPI  

**ScheduleAPI** — это удобный инструмент для создания и управления периодическими задачами в ваших Bukkit-плагинах. С помощью аннотации `@Schedule` вы можете легко настроить выполнение методов с поддержкой cron-таймеров, асинхронности и задержек.

---

## 🚀 Основные возможности
- 🧩 **Автоматический сбор классов**: Сканирует указанный пакет для поиска методов с аннотацией `@Schedule`.  
- ⏰ **Гибкость задач**: Поддержка задержек (в миллисекундах) и cron-таймеров.  
- 🔄 **Асинхронность**: Настройка выполнения задач в синхронном или асинхронном режиме.  
- 📦 **Простая интеграция**: Быстрая настройка с минимальными изменениями в коде.

---

## 📖 Использование

### 1️⃣ Подключение `ScheduleAPI`

Для использования API создайте экземпляр обработчика `AdapterFindClasses` в методе `onEnable` вашего плагина:

```java
public class Main extends JavaPlugin {

  @Override
  public void onEnable() {
    // Инициализация обработчика и указание пакета для поиска задач
    new AdapterFindClasses(this, "ru.optimus");
  }
}
```
## Изменение задержки запуска задач  
По умолчанию все потоки запускаются через 100 тиков (5 секунд). Вы можете изменить это значение:  

```java
new AdapterFindClasses(plugin, "ru.optimus", 500); // Задержка 500 мс
```
## 2️⃣ Создание задач  

### Синхронная задача  
Создайте метод с аннотацией `@Schedule`. Укажите интервал выполнения задачи в миллисекундах через параметр `millis`:  

```java
public class ExampleClass {

  @Schedule(millis = 1000) // Выполняется каждые 1 секунду
  public void execute() {
    Server server = Bukkit.getServer();
    server.broadcast(Component.text("Hello World!"));
  }
}
```
### Асинхронная задача  
Для асинхронного выполнения добавьте параметр `async = true`:  

```java
public class ExampleClass {

  @Schedule(millis = 1000, async = true) // Асинхронное выполнение
  public void execute() {
    Server server = Bukkit.getServer();
    server.broadcast(Component.text("Hello World async!"));
  }
}
```
## 3️⃣ Использование cron-таймера  
API поддерживает cron-таймеры в формате `1 * * * * *` (каждая 1 секунда новой минуты).  

Пример задачи с cron-таймером:  

```java
public class ExampleClass {

  @Schedule(cron = "1 * * * * *") // Выполняется каждую 1 секунду новой минуты
  public void execute() {
    Server server = Bukkit.getServer();
    server.broadcast(Component.text("Hello from cron!"));
  }
}
```

## 📦 Установка  
1. Скачайте и подключите **ScheduleAPI** в проект.  
2. Укажите пакет, в котором находятся ваши задачи.  
3. Создавайте задачи с аннотацией `@Schedule` — синхронно или асинхронно.  

---

## 🤝 Поддержка и вклад  
Будем рады вашим вопросам, предложениям и улучшениям. Создавайте Issues или отправляйте Pull Requests!  




