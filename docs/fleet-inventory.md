# Fleet e Inventory — aporte móvil de Mario

## Alcance implementado

- Fleet: listado, búsqueda por placa/marca/modelo/propietario, filtro de estado,
  registro, edición y detalle básico de identificación y propietario.
- Inventory: catálogo, búsqueda, filtro de stock bajo, registro/edición,
  precios de compra/venta, ganancia/margen y recepción de stock con confirmación.
- Estados de carga, error, vacío y guardado; recursos en inglés y español (Perú).
- Pantallas integradas a las pestañas Vehículos e Inventario del administrador.
- Demo local accesible desde el login solo en builds debug. El aviso identifica
  los datos ficticios; no se crea una sesión ni se hacen peticiones HTTP en demo.
  Los datos se conservan mientras esa pantalla siga en el back stack y se
  reinician al salir de la demo o morir el proceso. No hay persistencia offline.

## Arquitectura y contratos

| Elemento | Responsabilidad |
|---|---|
| `Vehicle`, `VehicleOwner` | Datos de vehículo y referencia de propietario |
| `FleetRepository` | Contrato para consultar vehículos/propietarios y guardar vehículos |
| `FleetApi`, `RemoteFleetRepository` | GET/POST/PUT de vehicles y GET de customers con el Retrofit autenticado existente |
| `InventoryItem`, `StockReceipt` | Datos, validación, stock bajo y cálculos monetarios con BigDecimal |
| `InventoryRepository` | Contrato para catálogo, guardado y recepción |
| `InventoryApi`, `RemoteInventoryRepository` | GET/POST/PUT de inventoryitems y POST de receipts |
| `ManagementViewModel` | Estado observable, selección explícita de repositorios demo/reales y acciones asíncronas |
| `ManagementScreen` | Formularios y listas Compose; selección de propietario real |
| `DemoRepositories`, `DemoScreen` | Datos ficticios aislados por sesión de demostración |

La autenticación existente, Retrofit, Hilt y el interceptor JWT se reutilizan.
No se guardan contraseñas ni tokens ficticios. La ruta demo no se registra en
release y también se protege al inicializar el ViewModel.

El registro de vehículos requiere un cliente existente. Si no hay clientes,
se informa al administrador que debe crearlos en Customer Management. La demo
incluye dos propietarios ficticios para poder probar ese flujo sin servidor.

El stock no forma parte del body de crear/editar catálogo. Un producto nuevo
empieza con stock cero; la recepción usa `/inventoryitems/{id}/receipts`.
Una respuesta incierta por timeout requiere consultar el stock antes de repetir
una recepción, porque el backend revisado no ofrece una clave de idempotencia.

## Trazabilidad para el reporte

| Historia | Aporte | Límite |
|---|---|---|
| US-19 | Búsqueda/lista/filtro por estado y propietario | Sin porcentaje de avance de órdenes |
| US-20 | Registro de datos técnicos y selección de cliente | Requiere Customer Management en modo real |
| US-21 | Identificación, propietario y estado en detalle | Parcial: faltan diagnóstico, órdenes y tareas |
| US-37 | Indicador y filtro por stock bajo | Parcial: no bloquea tareas ni envía notificaciones |
| Propuesta pendiente de acuerdo del equipo | Gestionar catálogo y recepción de repuestos | Añadir una historia al backlog sin renumerar unilateralmente las existentes |

Texto de implementación para adaptar al capítulo 5:

> Se desarrollaron los módulos móviles Fleet Management e Inventory Management
> con Kotlin y Jetpack Compose. Fleet permite consultar, filtrar, registrar y
> editar vehículos vinculados a un propietario. Inventory permite mantener el
> catálogo, consultar disponibilidad y registrar recepción de materiales. Se
> reutilizó la infraestructura de autenticación y comunicación existente, y se
> separaron los contratos de repositorio de sus implementaciones remotas y de
> demostración. El modo demo utiliza datos ficticios en memoria y permite
> verificar los flujos de interfaz mientras se resuelve el error HTTP 500 del
> registro del backend. La validación integrada con el servidor queda pendiente.

No describir capturas de la demo como evidencia de integración real. No afirmar
que todas las historias ni el porcentaje global del proyecto están completos.

## Pruebas y reproducción

Se incluyen 22 pruebas JUnit en `ManagementDomainTest` y
`ManagementRepositoryTest`: validaciones, búsqueda, umbral de stock, margen
con decimales, recepción, aislamiento de demo y serialización sin stock/id.
No son pruebas end-to-end ni medición de cobertura. El estado de ejecución
se documenta en VALIDACION.md incluido en el paquete de entrega.

En Android Studio, Execute Gradle Task:

```text
:app:testDebugUnitTest :app:assembleDebug
```

Resultados JUnit HTML: `app/build/reports/tests/testDebugUnitTest/index.html`.
Para instalar: `:app:installDebug`. Después abrir AutoService en el emulador.

## Guion de comprobación manual y capturas

1. Desde login, abrir demo; capturar el aviso DEMO junto a Vehículos.
2. Buscar DEMO-01 y filtrar Listo. Limpiar los filtros.
3. Registrar ABC-123, Toyota, Yaris, 2022 y Cliente Demo A. Verificar su tarjeta.
4. Editar su estado a Listo y abrir el detalle. Probar placa vacía/año inválido.
5. Abrir Inventario y filtrar stock bajo (el filtro de aceite inicia con 2).
6. Crear un producto: nombre Filtro de aire, compra 20, venta 35, mínimo 3.
   Verificar stock cero. Editar precio y verificar que el stock no cambia.
7. Recibir 5 unidades con Proveedor Demo, confirmar y verificar stock 5.
   Probar cantidad cero y proveedor vacío: no debe permitir confirmar.
8. Salir de demo y reabrir: los cambios deben reiniciarse.
9. Ejecutar tests y capturar sus resultados reales.
10. Cuando funcione el backend, repetir con cuenta y datos de prueba autorizados;
    reiniciar la app y confirmar persistencia. Registrar HTTP/status y errores.

Para capítulo 4 se pueden agregar capturas como evidencia de pantallas
implementadas, flujos Vehículos→Formulario→Guardar y
Inventario→Recepción→Confirmar, y el diccionario de clases de arriba.
No se generaron archivos Figma, wireframes ni un prototipo iOS en este aporte.

## Experimento propuesto (no ejecutado)

Pregunta: ¿el filtro de stock bajo reduce el tiempo para identificar productos
que necesitan reposición? Comparar lista sin filtro y lista con filtro usando
el mismo conjunto de datos, contrabalanceando el orden entre participantes.
Medir tiempo por tarea y exactitud; registrar dispositivo, tamaño de inventario,
familiaridad y orden de tareas. Reportar observaciones reales y limitaciones.
No confundir estas mediciones con pruebas unitarias ni inventar participantes
o resultados. Acordar muestra y análisis con la rúbrica antes de ejecutar.

## Pendientes y exclusiones

- Compilación Android e interacción visual en el entorno del usuario.
- Login y operaciones reales, bloqueados actualmente por el backend.
- Integración con órdenes/tareas para completar US-21 y US-37.
- Edición/captura de imágenes, borrado, métricas de cobertura, CI/CD y despliegue.
- El ajuste de timeouts que Mario hizo en NetworkModule.kt se conserva localmente
  y no se reemplaza con este paquete.
- No se han publicado commits, PR ni cambios en el reporte compartido.
