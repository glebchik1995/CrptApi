# Класс CrptApi

Этот класс предназначен для работы с API Честного Знака с использованием Java (версия 17). Он должен быть потокобезопасным и поддерживать ограничение на количество запросов к API в определенный интервал времени.

## Конструктор
```java
public CrptApi(TimeUnit timeUnit, int requestLimit)
timeUnit: Указывает промежуток времени (например, секунды, минуты).
requestLimit: Положительное значение, определяющее максимальное количество запросов в указанном промежутке времени.
Метод
В классе должен быть реализован единственный метод для создания документа для ввода товаров в оборот в России. Документ и подпись должны передаваться в метод в виде Java объекта и строки соответственно.

Метод должен вызываться через HTTPS метод POST по следующему URL:
https://ismp.crpt.ru/api/v3/1k/documents/create

Тело запроса должно быть в формате JSON следующего вида:

{
  "description": {
    "participantinn": "string",
    "doc_id": "string",
    "doc_status": "string",
    "doc_type": "LP_INTRODUCE_GOODS",
    "importRequest": true,
    "owner_inn": "string",
    "participant_inn": "string",
    "producer_inn": "string",
    "production_date": "2020-01-23",
    "production_type": "string",
    "products": [
      {
        "certificate_document": "string",
        "certificate_document_date": "2020-01-23",
        "certificate_document_number": "string",
        "owner_inn": "string",
        "producer_inn": "string",
        "production_date": "2020-01-23",
        "tnved_code": "string",
        "uit_code": "string",
        "uitu_code": "string",
        "reg_date": "2020-01-23",
        "reg_number": "string"
      }
    ]
  }
}
Библиотеки
Для реализации можно использовать библиотеки HTTP-клиента и сериализации JSON.

Структура Файлов
Решение должно быть оформлено в виде одного файла CrptApi.java. Все дополнительные классы, которые используются, должны быть внутренними.

Возможность расширения
Реализация должна быть разработана с возможностью легкого расширения для добавления дополнительного функционала.