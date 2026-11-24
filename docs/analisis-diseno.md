# Análisis y diseño

## 1. Problema

Los grupos escolares pasan por la sala de océanos antiguos en menos de dos minutos y leen poco. El museo quiere una aplicación con un océano prehistórico vivo en el que el visitante participe. Un niño debe entender qué hacer en segundos, un docente debe poder explicar con ella una cadena alimenticia y el personal debe mantener los datos científicos sin un programador.

## 2. Usuarios

| Usuario            | Puede                                               |
| ------------------ | --------------------------------------------------- |
| Visitante          | Jugar, pausar, reanudar y terminar una partida      |
|                    | Guardar y continuar partidas                        |
|                    | Ver las fichas, las mejores puntuaciones y ajustes  |
| Docente            | Lo mismo que el visitante                           |
|                    | Explicar quién come a quién con fichas y océano     |
| Personal del museo | Entrar con PIN y cambiarlo                          |
|                    | Consultar, filtrar, crear, editar y borrar especies |
|                    | Guardar el catálogo, revisar el registro, exportar  |

## 3. Funcionalidades

- Océano del Campaniano (Cretácico tardío) con superficie, aguas intermedias y fondo, dibujado por completo con Graphics2D.
- Ocho especies con fuentes ([fuentes](fuentes.md)) y cinco tipos de criatura: cazadores, reptiles cazadores que respiran aire, peces en grupo, amonites a la deriva y almejas fijas.
- Mecánica de sobrevivir y crecer: el visitante guía una cría de _Archelon_ por cuatro etapas. Gana al llegar a adulto; pierde si su vida llega a cero por mordidas o por falta de aire. Los puntos premian comida, tiempo, etapas y victoria, según la dificultad.
- Eventos ambientales: corriente, agua fría, lluvia de comida y llegada de un depredador.
- Teclado y ratón; ajustes persistentes; catálogo CSV; partidas XML; registro en texto plano; exportación XML.

## 4. Decisiones de diseño

### 4.1 Un solo periodo, explicado

Se eligió un único mar y periodo porque permite una cadena alimenticia coherente con las especies sugeridas. La explicación de por qué las especies aparecen juntas no es texto fijo: `Coexistence` calcula, a partir de las edades del catálogo, la época que comparten, y `CoexistenceNote` la redacta. Si el personal añade una especie de otra época, la nota la nombra y la administración avisa al guardarla.

### 4.2 Modelo de clases

- **Herencia y abstracción.** `Entity` y `Creature` son abstractas. `Creature.update` es un método plantilla (percibir, decidir, moverse, comer, gastar energía, reproducirse). Cada subclase redefine sobre todo `steer` y sus parámetros.
- **Interfaces para capacidades transversales.** `Edible` la implementan la comida y todas las criaturas. `AirBreather` la implementan `PlayerCreature` y `MarineReptile`, que están en ramas distintas. `Surroundings` desacopla las criaturas de la escena concreta y permite probarlas aisladas.
- **Composición.** `Ocean` contiene criaturas, comida y eventos. `GameSession` contiene el océano y al jugador. La respiración vive en `Lungs`, compartida por clases sin herencia común.
- **Polimorfismo sin `instanceof`.** El bucle llama `update` sobre `List<Creature>`. Que el jugador sea mordido en vez de devorado es una sobrescritura de `consume()`. Los pintores de cada forma están en un `EnumMap`.
- **Sobrecarga y sobrescritura.** `Species.livedAt(double)` (un momento) y `livedAt(Range<Double>)` (un periodo); constructores de `SpeciesCatalog` (vacío y copia) y de las excepciones (con y sin mensaje amigable o causa); `Hunter.maxSpeed` y `PlayerCreature.nutrition` sobrescriben métodos de la clase base.
- **Encapsulamiento.** El enunciado pide atributos protegidos y que el estado de cada objeto cambie solo con sus propios métodos. Por eso todos los atributos de instancia son `private`, la forma más estricta de protegerlos, y las subclases nunca los tocan directamente: cambian el estado heredado con métodos `protected` de la clase base, varios de ellos `final` para que no se puedan redefinir (`moveTo`, `spendEnergy`, `setSizeMeters`). Las constantes públicas son `static final`, y los objetos de valor (`Species`, `Range`, `Vector2`, instantáneas y _records_) no tienen métodos que cambien su estado.

### 4.3 Genéricos, colecciones y lambdas

- `Range<T extends Comparable<? super T>>` se reutiliza en edades, bandas de profundidad, rango de criaturas y coexistencia. `LoadResult<T>` lleva el valor cargado y sus problemas en cuatro cargadores.
- `Leaderboard` extiende `AbstractCollection<ScoreEntry>`: ordenada, limitada e iterable con _for-each_ y _streams_.
- `LinkedHashMap` (catálogo por id con el orden del archivo), `EnumMap`, `EnumSet`, `ConcurrentLinkedQueue` (comandos entre hilos) y `ArrayList` (entidades recorridas en cada paso).
- Lambdas en filtros y ordenamientos, comandos al motor, fábricas de eventos y oyentes de Swing.

### 4.4 Concurrencia

Un único hilo de simulación (`GameEngine`, 60 pasos por segundo) es dueño de `GameSession`, y el EDT nunca toca el estado del juego:

1. La entrada se convierte en comandos (lambdas) en una `ConcurrentLinkedQueue`, que se ejecutan entre pasos.
2. Tras cada paso se publica una instantánea inmutable (`GameSnapshot`) en un campo `volatile` que el EDT dibuja.
3. Guardar toma la copia del estado en el hilo de simulación y escribe en un `SwingWorker`; cargar y exportar también van en segundo plano. Las escrituras pequeñas (ajustes, historial, catálogo, PIN) ocurren fuera del juego y tardan milisegundos, así que se hacen en el EDT.

Así, movimiento, tiempo, eventos, entrada, dibujo y disco avanzan en paralelo sin candados. Un hilo por criatura obligaría a sincronizar cada interacción. Las acciones tras un diálogo quedan ligadas al motor que estaba en pantalla. Un error inesperado detiene el motor, se registra y se avisa. Los eventos usan tiempo de simulación, así que la pausa los congela.

### 4.5 Reglas ecológicas

Comer depende de la dieta y del tamaño: la presa mide como máximo el 55 % del depredador, y ningún animal come crías de su propia especie. Los cazadores solo cazan con hambre y vuelven a tenerla en menos de un minuto. Solo los que comen reptiles y son lo bastante grandes (_Tylosaurus_ y _Cretoxyrhina_) pueden atacar a la tortuga. Un cazador con hambre persigue a la presa más cercana que pueda comer, y esa puede ser la tortuga: quedarse quieta no la protege. Las especies se reproducen con energía suficiente y hasta su límite de población. Si una especie cae por debajo de una cuarta parte de su límite, llegan individuos nuevos desde el borde más alejado del jugador, en promedio uno cada 10 s.

El equilibrio se ajustó midiendo con pruebas automáticas acotadas (`EcosystemBalanceTest`, tres semillas):

- Antes, los adultos se comían a sus propias crías, _Baculites_ (que come plancton) podía comerse peces pequeños y los _Baculites_ desaparecían en 3 a 4 minutos. Además, los cazadores tenían hambre solo entre el 1 y el 14 % del tiempo, y un jugador quieto recibía su primera mordida después de 1 a 3 minutos.
- Ahora, en 5 minutos ninguna especie desaparece, las presas no bajan de 30 de 36 y nunca se supera el límite total. Un jugador automático sensato gana 8 de 9 partidas. En Fácil y Normal gana las tres (en unos 90 s y 2 min) y en Difícil pierde una de cada tres.

### 4.6 Persistencia y errores

| Dato          | Formato        | Motivo                                   |
| ------------- | -------------- | ---------------------------------------- |
| Catálogo      | CSV            | El personal usa hojas de cálculo         |
| Partidas      | XML versionado | Anidado; detecta versiones incompatibles |
| Ajustes y PIN | `.properties`  | Pares clave-valor                        |
| Historial     | CSV            | Una línea por partida                    |
| Exportación   | XML            | Publicación web sin bibliotecas externas |
| Registro      | Texto plano    | Legible por el personal                  |

Cada partida guardada lleva el atributo `formatVersion`, que permite reconocer los archivos de versiones incompatibles.

**Guardar y restaurar.** Una partida guarda todo el estado del océano, incluida la posición de la secuencia aleatoria (`ResumableRandom` reproduce `java.util.Random` y deja leer su estado), para que la partida restaurada continúe igual que la original. También guarda el punto al que el visitante mandó nadar con un clic. Las teclas pulsadas no se guardan, porque la partida vuelve en pausa y nadie las mantiene pulsadas.

**Validación al restaurar.** Se rechazan las partidas imposibles y las de especies que el personal cambió después. "Continuar" prueba de la más reciente a la más antigua.

**Escritura segura.** Las escrituras son atómicas, y el lector XML bloquea `DOCTYPE` y entidades externas.

La jerarquía de excepciones tiene raíz comprobada `AncientSeasException`, con mensaje amigable en español:

- `InvalidScientificValueException`, `DuplicateSpeciesException`, `HabitatViolationException` y `CatalogFormatException`;
- `SaveGameException`, con sus subclases `CorruptSaveException` e `IncompatibleSaveException`.

Se capturan donde se puede actuar (omitir la fila, ofrecer otra partida, mostrar el error junto al campo). No hay `catch` vacíos.

### 4.7 Accesibilidad

| Medida                                              | Comprobación        |
| --------------------------------------------------- | ------------------- |
| Contraste mínimo 4,5:1 en textos, botones y paneles | `ThemeContrastTest` |
| Foco visible; el juego se juega sin ratón           | Recorrido real      |
| Confirmaciones claras; la opción segura por defecto | Recorrido real      |
| Nada parpadea; barras con símbolo, nombre y aviso   | Revisión y capturas |
| "Movimiento reducido" detiene la decoración         | 0 píxeles distintos |
| Interfaz al 125 % y 150 % sin cortar texto          | Recorrido real      |

## 5. Supuestos

- Pantallas de 1280 x 720 o más.
- El PIN inicial es `1234`; el manual pide cambiarlo.
- La zona de cada especie es una decisión de diseño salvo donde las fuentes la precisan.
- Sin sonido: el enunciado lo menciona solo como ajuste posible.
- Un solo idioma, el español, en la interfaz y en los documentos. El enunciado pide que los ajustes básicos se conserven entre sesiones y nombra el idioma dentro de una lista de ejemplos ("and so on"), igual que el sonido "si lo hay". Con un único idioma no hay nada que elegir ni que guardar, así que no hay ajuste de idioma; los ajustes que sí existen se conservan.
