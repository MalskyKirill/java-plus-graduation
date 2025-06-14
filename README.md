### Описание проекта:

Модуль core разбит на 3 микросервиса и вспомогательный модуль:
1. event-service обрабатывает запросы связанные с событиями, подборками событий и категориями
2. request-service обрабатывает запросы пользователей связанные с заявками на участие в событиях
3. user-service обрабатывает запросы связанные с пользователями
4. Вспомогательный модуль interaction-api содержит в себе общие для всех модулей DTO, model, validation, exception и Feign-клиенты,  с помощью
которых происходит общение между микросервисами

Для обращения к event-service, request-service, user-service были выделены отдельные внутренние контроллеры.

Все микросервисы при запуске регистрируются в eureka-server. Все конфигурации загружаются из config-server. Все запросы
проходят через gateway-server, который находит нужный сервис через eureka и перенаправляет запрос по нужному адресу

Модуль infra cодержит инфраструктурные сервисы, обеспечивающие работу приложения:
1. config-server служит для централизованного хранения конфигураций всех сервисов. 
2. gateway обеспечивает единую точку входа для всех запросов к сервисам.
3. discovery-server(Eureka) регистрирует все микросервисы и обеспечивает их обнаружение.

Модуль stats собирает информацию о статистике просмотров событий:
1. stats-client обеспечивает взаимодействие с сервисами модуля core.
2. stats-dto модуль для DTO.
3. stats-server служит для сбора статистики.
