
Feature: Registro de vehículos en AutoService

  Como administrador del taller
  Quiero registrar vehículos y evitar placas duplicadas
  Para mantener un catálogo confiable de vehículos

  Scenario: Registrar un vehículo válido
    Given que el taller no tiene vehículos registrados
    And se dispone de un vehículo Toyota Corolla con placa "ABC-123"
    When el administrador registra el vehículo
    Then el registro se realiza correctamente
    And el vehículo queda guardado con placa "ABC-123"

  Scenario: Rechazar un vehículo con placa duplicada
    Given que existe un vehículo registrado con placa "ABC-123"
    And se dispone de otro vehículo con placa " abc-123 "
    When el administrador registra el vehículo
    Then el registro es rechazado por placa duplicada
    And no se guarda un segundo vehículo
