Feature: Gestión de operaciones de taller, tareas y métricas del dashboard

  Como administrador del taller AutoService
  Quiero registrar tareas, asignar mecánicos y consultar métricas financieras y operativas
  Para supervisar el flujo de trabajo del taller y mantener la rentabilidad del negocio

  Scenario: Administrador registra una nueva tarea técnica asignando mecánico y tiempo estimado
    Given que existe una orden de trabajo activa con id 15
    And el mecánico "Carlos Mecánico" con id 3 está disponible en el taller
    When el administrador registra la tarea "Diagnóstico electrónico con scanner OBD2" con prioridad "HIGH", 45 minutos y mano de obra 80.0
    Then la tarea queda asociada a la orden de trabajo 15
    And el estado inicial de la tarea es "PENDING"
    And el mecánico asignado es "Carlos Mecánico"

  Scenario: Administrador actualiza el estado de una tarea y se recalcula el progreso de la orden
    Given que una orden de trabajo con id 15 tiene 2 tareas registradas
    And una tarea ya se encuentra "COMPLETED" y la otra "IN_PROGRESS"
    When el administrador actualiza el estado de la segunda tarea a "COMPLETED"
    Then el porcentaje de avance de la orden de trabajo se recalcula al 100 por ciento

  Scenario: Administrador consulta las métricas del taller en el dashboard financiero
    Given que existen órdenes de trabajo activas y completadas en el taller
    And se cuenta con un resumen financiero consolidado del backend
    When el administrador visualiza el dashboard principal del taller
    Then se muestran los vehículos activos en taller y las órdenes en proceso
    And se visualiza el ingreso proyectado, el margen de ganancia y el ticket promedio
