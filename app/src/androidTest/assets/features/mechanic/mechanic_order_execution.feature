Feature: Ejecución y reporte técnico de órdenes de trabajo por el mecánico

  Como mecánico del taller AutoService
  Quiero iniciar, diagnosticar y completar tareas asignadas a una orden de trabajo
  Para reflejar el avance técnico real del vehículo y solicitar repuestos adicionales si es necesario

  Scenario: Mecánico inicia una tarea técnica asignada
    Given que el mecánico ha iniciado sesión con su cuenta asignada
    And tiene una orden de trabajo activa con id 10 y placa "ABC-123"
    And la orden tiene una tarea pendiente "Cambio de pastillas de freno" con id 101
    When el mecánico presiona el botón "Iniciar" en la tarea con id 101
    Then el estado de la tarea cambia a "IN_PROGRESS"
    And se habilita el campo para registrar el diagnóstico técnico

  Scenario: Mecánico completa una tarea agregando el diagnóstico técnico
    Given que la tarea con id 101 se encuentra en estado "IN_PROGRESS"
    When el mecánico ingresa el diagnóstico técnico "Discos rectificados y pastillas cerámicas instaladas"
    And presiona el botón "Completar Tarea"
    Then el estado de la tarea pasa a "COMPLETED"
    And el porcentaje de avance de la orden de trabajo se incrementa al 100 por ciento

  Scenario: Mecánico propone una tarea adicional con repuestos del inventario
    Given que durante la inspección de la orden 10 se detecta un problema no contemplado
    When el mecánico propone la tarea "Reemplazo de manguera de freno cuarteada" con prioridad "URGENT" y 30 minutos estimados
    And añade 1 unidad del repuesto "Manguera flexible de freno" con id de inventario 12
    Then la solicitud de tarea se registra con estado de revisión "SUBMITTED"
    And queda en espera de la aprobación del administrador del taller
