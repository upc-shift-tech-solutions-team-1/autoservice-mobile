
Feature: Seguimiento del mantenimiento de vehículos

  Como cliente de AutoService
  Quiero consultar el mantenimiento de mi vehículo
  Para conocer el estado de mi orden de servicio

  Scenario: Consultar una orden con código de seguimiento válido
    Given que existe una orden con código "TRK-100"
    And el cliente ingresa el código "TRK-100"
    When el cliente consulta el seguimiento
    Then se muestra la orden correctamente
    And se visualiza la placa "ABC-123"
    And no se muestra un mensaje de error

  Scenario: Consultar un código de seguimiento inexistente
    Given que no existe una orden con código "TRK-404"
    And el cliente ingresa el código "TRK-404"
    When el cliente consulta el seguimiento
    Then se muestra el error de orden no encontrada
    And no se muestra ninguna orden
