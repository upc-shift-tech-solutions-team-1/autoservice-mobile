
Feature: Control de existencias del inventario

  Como administrador del taller AutoService
  Quiero registrar entradas y controlar salidas de materiales
  Para mantener cantidades correctas y evitar consumir stock inexistente

  Scenario: Registrar una recepción válida de materiales
    Given que existe un producto con 5 unidades disponibles
    And el administrador recibe 5 unidades adicionales
    When se registra el ingreso de materiales
    Then el stock del producto aumenta a 10 unidades
    And se solicita guardar el producto actualizado

  Scenario: Rechazar una salida por stock insuficiente
    Given que existe un producto con 5 unidades disponibles
    And se solicita retirar 10 unidades
    When se intenta registrar la salida de materiales
    Then la operación se rechaza por stock insuficiente
    And no se solicita guardar ningún cambio
