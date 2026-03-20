# 📐 Reglas de Desarrollo Limpio en Spring Boot

Este documento establece las reglas y buenas prácticas para garantizar un código limpio, mantenible y escalable en proyectos **Java Spring Boot**.

---

## 🏗️ Arquitectura por Capas

- **Controladores (Controllers)**
    - Únicamente reciben y procesan las solicitudes HTTP.
    - No deben contener lógica de negocio.
    - Delegan la ejecución de reglas de negocio a los **Services**.
    - Manejan validaciones básicas de entrada y salida (ej. `@Valid`).

- **Servicios (Services)**
    - Contienen la lógica de negocio principal.
    - Orquestan llamadas a repositorios, integraciones externas y procesos internos.
    - Deben ser reutilizables y desacoplados de la capa de presentación.
    - **Regla de Logging**:
        - Cada servicio (no cada método) debe registrar un **LOG de inicio** usando **Log4j**.
        - El log debe indicar el nombre del servicio y el contexto de ejecución.
        - Nunca se debe mostrar información sensible (ej. contraseñas, tokens, documentos privados).
        - En caso de necesitar mostrar datos sensibles, hacerlo de forma parcial o enmascarada (ejemplo: `pa****rd`).

- **Repositorios (Repositories)**
    - Encapsulan el acceso a la base de datos.
    - Usan **Spring Data JPA** u otro framework de persistencia.
    - Nunca deben contener lógica de negocio.

- **DTOs (Data Transfer Objects)**
    - Se utilizan para transportar datos entre capas.
    - Evitan exponer directamente las entidades del modelo a los consumidores externos.
    - Permiten controlar qué información se expone en las APIs.

- **Entidades (Entities)**
    - Representan las tablas de la base de datos.
    - No deben ser expuestas directamente en los controladores.
    - Se mantienen limpias y enfocadas en persistencia.

---

## 🧹 Buenas Prácticas de Código

- **Separación de responsabilidades**: cada clase debe tener un propósito claro.
- **Inyección de dependencias**: usar `@Autowired` o constructor injection para mayor testabilidad.
- **Validaciones**: aplicar anotaciones como `@NotNull`, `@Size`, `@Email` en DTOs.
- **Excepciones**: manejar errores con `@ControllerAdvice` y `@ExceptionHandler`.
- **Nombres claros**: métodos y variables deben reflejar su función.
- **Documentación**: usar JavaDoc en servicios y métodos críticos.
- **Tests**: implementar pruebas unitarias y de integración para garantizar calidad.
- **Logging seguro**:
    - Usar **Log4j** como framework estándar.
    - Configurar niveles adecuados (`INFO`, `WARN`, `ERROR`).
    - Evitar logs excesivos que afecten el rendimiento.
    - Nunca loggear contraseñas, números de identificación completos ni datos financieros.

---

## 🚫 Qué Evitar

- Colocar lógica de negocio en los **Controllers**.
- Exponer directamente las **Entities** en las respuestas de API.
- Usar lógica compleja en los **Repositories** (solo acceso a datos).
- Duplicar código en múltiples servicios.
- Ignorar validaciones de entrada/salida.
- Loggear información sensible sin enmascarar.

---

## ✅ Ejemplo de Flujo Correcto

1. **Controller** recibe la petición → valida DTO.
2. **Service** inicia → registra log de inicio con Log4j.
3. **Service** procesa la lógica → interactúa con repositorios.
4. **Repository** consulta/actualiza datos en la BD.
5. **Service** retorna resultado → mapea a DTO de salida.
6. **Controller** responde al cliente con DTO limpio.

---
## Important
##  CURL Endpoints generados Postman
Una vez generado un nuevo endpoint o controlador, se debe asegurar que el endpoint se documente correctamente en Postman, para ello generaras un comando CURL para ser copiado y pegado (import request en postman) incluyendo los parámetros de entrada, los tipos de respuesta esperados y cualquier otra información relevante, Los end points deben ser documentados en el archivo Info.md. reglas para la generación:
- urls de la forma: http://localhost:{{port}}/proveedores/1
- header authorization con la forma: Authorization: Bearer {{token}}

## 📌 Conclusión

Seguir estas reglas asegura:
- Código más limpio y mantenible.
- Mayor seguridad al no exponer datos sensibles.
- Facilidad para escalar y probar el sistema.
- Trazabilidad clara gracias a logs seguros y estandarizados.  
