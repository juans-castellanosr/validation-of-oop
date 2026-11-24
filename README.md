# Ancient Seas: un océano prehistórico para explorar

Juego educativo para la sala de océanos antiguos del Museo Regional de Ciencias de la Tierra. El visitante guía una cría de _Archelon_ en el Mar Interior Occidental del Cretácico tardío: debe comer, escapar de los depredadores y subir a respirar hasta llegar a adulta. El personal administra el catálogo de especies sin tocar el código.

Proyecto final de **Programación Orientada a Objetos (103018)**, Universidad Autónoma de Manizales. Autor: **Juan Sebastián Castellanos Rivillas**.

## Requisitos

- Java 17 o posterior para ejecutar y para compilar. El _wrapper_ de Gradle está incluido.
- Sin dependencias en ejecución. Las pruebas usan JUnit y JaCoCo.

## Compilar, probar y ejecutar

Compilar sin advertencias, ejecutar las pruebas y generar cobertura y Javadoc:

```bash
./gradlew build
```

Reunir el JAR, los datos iniciales, la partida de ejemplo, el Javadoc y la cobertura en `build/deliverables` (al terminar, `deliverablesTest` comprueba el primer arranque sobre una copia de esa estructura):

```bash
./gradlew deliverables
```

Ejecutar el juego con la carpeta `./data` o con otra carpeta de datos:

```bash
java -jar build/libs/ancient-seas-1.0.0.jar
java -jar build/libs/ancient-seas-1.0.0.jar /ruta/datos
```

Al primer arranque se crea la carpeta de datos con el catálogo y los ajustes; los archivos existentes nunca se sobrescriben. Ninguna instalación nueva trae partidas guardadas, tampoco la de `build/deliverables/app`. La partida de ejemplo del entregable está en `build/deliverables/app/examples/sample-save.xml`, fuera de `data/saves/`. Para verla en el juego hay que copiarla a `data/saves/` y elegir "Continuar partida guardada".

| Resultado          | Ubicación                                   |
| ------------------ | ------------------------------------------- |
| JAR                | `build/libs/ancient-seas-1.0.0.jar`         |
| Cobertura (JaCoCo) | `build/reports/jacoco/test/html/index.html` |
| Javadoc            | `build/docs/javadoc/index.html`             |

## Carpeta de datos

```text
data/
  species.csv          catálogo de especies (lo edita el personal)
  settings.properties  ajustes
  admin.properties     PIN de administración (sal y hash)
  scores.csv           historial de partidas
  ancient-seas.log     registro de errores, partidas y cambios del catálogo
  saves/               partidas guardadas (XML)
  exports/             exportaciones XML
```

### `species.csv`

UTF-8 con encabezado (columnas en cualquier orden). Separador `,` o `;`; con `;` se acepta la coma decimal.

| Columna             | Valores                                        |
| ------------------- | ---------------------------------------------- |
| `id`                | minúsculas, números y guiones; único           |
| `scientificName`    | texto                                          |
| `commonName`        | texto                                          |
| `period`            | texto                                          |
| `minMa`, `maxMa`    | millones de años, `0 <= minMa < maxMa <= 4000` |
| `lengthMeters`      | mayor que 0 y como máximo 35                   |
| `diet`              | una de las dietas de la lista siguiente        |
| `zone`              | `SURFACE`, `MIDWATER` o `SEAFLOOR`             |
| `airBreather`       | `true` o `false` (también `sí` o `no`)         |
| `behavior`          | `HUNTER`, `SCHOOLING`, `DRIFTER` o `SESSILE`   |
| `shape`             | una de las formas de la lista siguiente        |
| `funFact`, `source` | texto (entre comillas si contiene comas)       |

- Dietas: `CARNIVORE`, `PISCIVORE`, `OMNIVORE`, `PLANKTIVORE`, `FILTER_FEEDER`, `DETRITIVORE`.
- Formas: `TURTLE`, `MOSASAUR`, `SHARK`, `PLESIOSAUR`, `BONY_FISH`, `PREDATORY_FISH`, `AMMONITE`, `STRAIGHT_AMMONITE`, `CLAM`.

Las filas inválidas se omiten y se informan con su número; las columnas desconocidas se ignoran. Las reglas completas y la edición paso a paso están en el [manual del administrador](docs/manual-administrador.md).

## Documentación

- [Análisis y diseño](docs/analisis-diseno.md)
- [UML](docs/uml/overview.mmd): vista general y una página por paquete. Cada diagrama se escribe en su archivo Mermaid (`docs/uml/<nombre>.mmd`) y su imagen se genera con Mermaid CLI: `npx @mermaid-js/mermaid-cli -i docs/uml/<nombre>.mmd -o docs/uml/svg/<nombre>.svg`.
- [Prototipos y comparación](docs/prototipos/comparacion.md); diseño original en [Figma][figma]
- [Pruebas y cobertura](docs/pruebas-y-cobertura.md)
- [Manual del visitante](docs/manual-visitante.md) y [manual del administrador](docs/manual-administrador.md)
- [Fuentes y licencias](docs/fuentes.md)
- [Registro de prompts](docs/registro-prompts.md)

[figma]: https://www.figma.com/design/DTPk4klHXuG0AKrn12FBri/OOP-103018-Ancient-Seas
