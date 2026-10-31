
Feature: Disponibilidad de mecanicos del taller

  Como administrador de AutoService
  Quiero consultar la disponibilidad de los mecanicos
  Para conocer quienes pueden recibir nuevas tareas

  Scenario: Consultar un mecanico con capacidad disponible
    Given que el mecanico tiene una capacidad maxima de 3 tareas
    And tiene 2 tareas activas
    When el administrador consulta su disponibilidad
    Then el mecanico aparece como disponible
    And se muestran 2 tareas asignadas

  Scenario: Consultar un mecanico con capacidad completa
    Given que el mecanico tiene una capacidad maxima de 3 tareas
    And tiene 3 tareas activas
    When el administrador consulta su disponibilidad
    Then el mecanico aparece como no disponible
    And se muestran 3 tareas asignadas
