# test
## Сборка
Команды для сборки клиента и сервера :
- gradle :client:distZip
- gradle :server:distZip
## Конфигурация
- конфигурация log4j стандартная, если конфиг не указан - используется дефолтный
(вывод на консоль и в файл client.log/server.log)
- server.properties ищется в рабочем каталоге, если не найден - 
используется дефолтный (service1=...Service1)
## Запуск сервера
Сервер включает тестовый сервис Service1 (подключается в дефолтном конфиге).
Параметры запуска:

> server порт количество_потоков_обработки(4)

## Запуск клиента
Класс Client может использоваться как главный, если его запустить - 
выполнится тестовый пример. Параметры запуска:

> client хост порт
 
