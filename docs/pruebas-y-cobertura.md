# Pruebas unitarias y cobertura

## Cómo ejecutarlas

Ejecutar las pruebas y después generar el informe de JaCoCo:

```bash
./gradlew test
```

Generar solo el informe:

```bash
./gradlew jacocoTestReport
```

Comprobar el entregable real (se ejecuta también al final de `deliverables`):

```bash
./gradlew deliverablesTest
```

- Informe HTML: `build/reports/jacoco/test/html/index.html` (también se incluye en `build/deliverables/coverage` con `./gradlew deliverables`).
- Las pruebas se ejecutan con `java.awt.headless=true`: ninguna abre una ventana. La lógica del juego se prueba sin interfaz gráfica, como pide el enunciado.

## Qué se prueba

Las pruebas intentan romper el sistema: valores límite, archivos malformados, choques en el borde de la escena, una presa alcanzada por dos depredadores en el mismo paso y comandos enviados desde muchos hilos.

| Área                | Clases de prueba                                     |
| ------------------- | ---------------------------------------------------- |
| Genérico y catálogo | `RangeTest`, `SpeciesTest`, `SpeciesCatalogTest`     |
|                     | `CoexistenceTest`                                    |
| Criaturas           | `CreatureBehaviorTest`, `PlayerCreatureTest`         |
| Simulación          | `EventSchedulerTest`, `ResumableRandomTest`          |
| Reglas y sesión     | `GameSessionTest`, `LeaderboardTest`                 |
| Concurrencia        | `GameEngineTest`                                     |
| Persistencia        | `SpeciesCsvTest`, `CsvParserTest`, `SaveGameXmlTest` |
|                     | `SettingsStoreTest`, `AdminAccessStoreTest`          |
|                     | `ScoreHistoryStoreTest`, `XmlExporterTest`           |
|                     | `ApplicationLogTest`, `DataDirectoryTest`            |
|                     | `BundledCatalogTest`, `FileMuseumServicesTest`       |
| Accesibilidad       | `ThemeContrastTest`, `AccessibilitySettingsTest`     |
| Dibujo              | `RenderingTest`, `CoexistenceNoteTest`               |
| Ecosistema          | `EcosystemBalanceTest`                               |
| Administración      | `AdminAccessTest`                                    |
| Entregable          | `DeliverableFirstRunTest` (tarea aparte)             |

Casos representativos de cada área:

- **Genérico y catálogo.** Límites inclusivos, rangos invertidos, tamaños negativos, cero, NaN o excesivos, edades invertidas o iguales, ids mal formados, animal fijo fuera del fondo, especies duplicadas, reemplazo que conserva el orden.
- **Criaturas.**
  - Choques y bordes: criatura en el borde derecho y en la esquina, criatura fuera del océano.
  - Comida: **presa alcanzada por dos cazadores en el mismo paso (se come una sola vez)**, partícula disputada por dos peces, dieta y tamaño, cazador saciado que no come aunque una presa lo toque.
  - Ciclo de vida: muerte por hambre, límite de población.
  - Movimiento: reptil sin aire que sube, amonite que escapa con chorro, peces que huyen, almeja que no arrastra la corriente.
  - Jugador: **dos mordidas simultáneas solo hieren una vez**, tres mordidas son fatales, asfixia bajo el agua, respiración en la superficie.
- **Simulación.** Eventos que ocurren y se anuncian, corriente que deja de actuar al terminar, evento puntual que no se repite al restaurar, frío que reduce la actividad, llegada de depredador, evento de especie eliminada, ningún tipo de evento duplicado.
- **Reglas y sesión.**
  - Estados: transiciones inválidas, pausa que congela todo, victoria al llegar a adulto, derrota, puntuación según dificultad.
  - Guardado: **guardar y restaurar reproduce exactamente el estado** (incluida la protección tras una mordida), partida terminada que no se puede guardar, ids que no se reutilizan tras restaurar.
  - Guardados incompatibles o dañados: especie eliminada, sin jugador, dos jugadores, jugador de otra especie, partida ya ganada, ids repetidos, tamaño cero, criatura fuera del océano.
  - Tabla de puntuaciones: orden, empates, capacidad, iterador sin borrado, uso como `Collection` estándar.
- **Concurrencia.** La simulación avanza en su propio hilo, la pausa congela el tiempo, **1 600 comandos enviados desde 8 hilos se aplican todos sin corromper el estado**, guardar mientras corre da un estado coherente, un error inesperado detiene el motor y se informa, apagado limpio.
- **Persistencia.**
  - CSV: columna extra, tamaño negativo, especie duplicada, `;` con coma decimal, comillas, comas y saltos de línea dentro de campos, BOM, archivo vacío, solo encabezado, columna faltante, número de campos incorrecto.
  - Partidas: archivo truncado, texto plano, intento de XXE, número no finito, atributo faltante, versión de formato desconocida.
  - Ajustes con valores basura.
  - Historial: una línea rota, una comilla sin cerrar al final, un jugador llamado como la columna del encabezado, la última línea cortada antes de agregar.
  - Exportación con texto que contiene `<` y `&`; escritura atómica sin archivos temporales sobrantes.
  - Catálogo incluido válido y con especies que comparten una ventana de edad.
- **Accesibilidad.** Contraste AA de todos los pares de texto y fondo (incluidos el estado deshabilitado, los avisos del HUD y el texto sobre el panel translúcido en el peor caso); los tonos de hover y pulsación nunca pierden contraste; opciones de accesibilidad guardadas y cargadas; valores inválidos que vuelven al valor normal con aviso; archivos anteriores sin las opciones nuevas; la partida de ejemplo incluida sigue abriendo.
- **Dibujo.** Cada forma corporal tiene un pintor que dibuja algo en tamaño pequeño, mediano y grande; un cuadro completo de una partida real se dibuja sin pantalla, con efectos y en pausa; dibujar nunca cambia el estado del juego; el océano del menú se dibuja con y sin movimiento reducido.
- **Ecosistema.** Partidas aceleradas sin interfaz, con tres semillas y límite de tiempo: las presas se renuevan sin superar los límites ni extinguirse, solo los cazadores que comen reptiles pueden atacar a una cría de Archelon, y una estrategia sensata puede ganar en las tres dificultades: la prueba exige al menos 2 victorias de 3 partidas en cada una.
- **Administración.** PIN correcto e incorrecto, PIN mal formado, codificación de ida y vuelta.
- **Entregable.** Sobre una copia de `build/deliverables/app/data`: la carpeta entregada no trae partidas, el primer arranque no encuentra ninguna partida guardada, una partida guardada se reanuda tras reiniciar y la partida de `examples/` sigue abriendo al copiarla a `saves/`.

## Cobertura obtenida

JaCoCo 0.8.14, 212 pruebas aprobadas en `./gradlew clean build`, más las 4 de `deliverablesTest`, que no entran en el informe:

| Paquete       | Líneas cubiertas |
| ------------- | ---------------- |
| `error`       | 100 %            |
| `catalog`     | 98 %             |
| `simulation`  | 99 %             |
| `persistence` | 98 %             |
| `model`       | 98 %             |
| `admin`       | 95 %             |
| `game`        | 95 %             |
| `ui`          | 31 %             |
| `app`         | 27 %             |
| **Total**     | 58,9 %           |

- **Total:** 58,9 % de líneas (2.749 de 4.664) y 62,0 % de ramas (839 de 1.354).
- **`ui`:** se prueban sin pantalla el tema de colores, la nota de coexistencia y todo el dibujo del océano y las criaturas. Las ventanas y los diálogos se verificaron con la aplicación real.
- **`app`:** `FileMuseumServicesTest` prueba el guardado del catálogo, la copia de seguridad y la persistencia tras reiniciar. El arranque se verificó ejecutando el JAR.

La lógica del juego está entre el 94 % y el 100 %. El total baja por la interfaz gráfica, como se explica abajo.

## Qué no se probó con JUnit y por qué

- **`ui` (ventanas Swing).** Probarlas con JUnit exigiría una biblioteca de automatización de interfaces, que sería una dependencia externa sin aprobar y daría pruebas frágiles. La lógica que la interfaz necesita vive fuera de `ui` y está probada, y el dibujo se prueba sin pantalla. Las ventanas se verificaron ejecutando el JAR en Linux y en Windows: menú, juego con teclado y ratón, pausa, ficha, guardado, confirmaciones, resultados, exportación, PIN, catálogo, formulario, ajustes y continuar una partida.
- **`app` (arranque).** Capa delgada que conecta los cargadores ya probados con la interfaz. El guardado del catálogo sí se prueba; el arranque se verificó en cada ejecución del JAR.
- **Ramas defensivas sin probar en paquetes probados.** Por ejemplo, el `IllegalStateException` cuando el punto de partida fijo del jugador quedara fuera del océano, que no puede ocurrir con las dimensiones actuales, o algunos errores de E/S del sistema operativo difíciles de provocar de forma portable.
