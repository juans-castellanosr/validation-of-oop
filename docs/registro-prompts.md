# Registro de prompts y uso de asistentes de IA

Ancient Seas es un proyecto que concebí y empecé yo: la idea, los diseños y prototipos de la interfaz, los diagramas iniciales de clases, el planteamiento de la estructura y el funcionamiento, las primeras pruebas manuales y los borradores de la documentación son trabajo mío. Después incorporé asistentes de IA para continuar el desarrollo: Claude Code (modelo Claude Opus 5.5, en la terminal de WSL) para implementar, investigar y corregir, Codex (GPT-6.1 Sol) como revisor independiente y mis modelos locales NES/FMI para generar bloques de código. Gran parte del código Java y de las pruebas automatizadas lo escribieron estos asistentes a partir de mis materiales e instrucciones.

Este registro recoge los prompts más representativos de siete temas, con lo que pedí, cómo refiné la instrucción, qué respondió el asistente, qué acepté, qué descarté y por qué. Las citas son textuales y el resto son resúmenes fieles. Cuando una instrucción la escribió Claude Code para uno de sus subagentes, la entrada lo indica.

## Técnicas de ingeniería de prompts

Desde el primer prompt usé un rol, el contexto completo con rutas exactas, restricciones claras, el trabajo dividido en incrementos, un formato de salida concreto y criterios para saber cuándo detenerse. Con la práctica añadí lo que más mejoró los resultados: explicar el porqué de cada decisión, describir lo observado frente a lo esperado cuando algo fallaba y mostrar ejemplos de antes y después cuando pedía cambios de redacción.

## 1. Desarrollo del código a partir de mis diagramas

**Qué pedí.** Con mis diagramas y el planteamiento del sistema como base, escribí una instrucción maestra que asigna un rol: "Actúa con el criterio de un ingeniero de software sénior extraordinariamente experimentado, pragmático, preciso y orientado a resultados". Le di el contexto con rutas exactas (enunciado y syllabus en `/mnt/c/Users/juans/Desktop/POO`, repositorio en `~/university/oop`), fijé restricciones como Java 17 o superior, Swing, sin motores de juego y sin sobreingeniería, y dividí el trabajo en comprender, implementar, compilar, probar, corregir y verificar. También pide "No inventes información paleontológica ni fuentes", "Pregunta solamente cuando exista una ambigüedad crítica" y "No debilites pruebas para obtener métricas favorables".

**Refinamiento.** Antes de programar, el asistente hizo cuatro preguntas que cambiaban el alcance. Respondí cada una con la decisión y su razón, por ejemplo un solo mar del Cretácico tardío con una mecánica de supervivencia y crecimiento, y la interfaz en español con el código en inglés. Explicar el _qué_ junto con el _por qué_ funcionó mejor que escoger una opción sin justificarla, porque el asistente aplicó esa intención en decisiones posteriores.

**Respuesta y decisiones.** Siguiendo mis diagramas, Claude Code implementó las clases del modelo, la simulación y la persistencia con sus relaciones. Para la persistencia le pasó a un subagente (instrucción de Claude Code) contratos explícitos de cada clase y sus excepciones, los archivos autorizados y un criterio de terminado: compilación sin advertencias y pruebas que intenten romper el sistema. Acepté el resultado, incluido el rechazo de XML malicioso, que nadie había pedido. Descarté una cadena de `instanceof` para mostrar los nombres en español, porque el enunciado pide evitarla, y se reemplazó por una interfaz. También descarté cambiar el generador aleatorio en cada guardado, porque tomar una copia del estado alteraba el juego, y se usó un generador cuyo estado se puede leer sin efectos.

## 2. Interfaz gráfica con Swing y Graphics2D

**Qué pedí.** Partí de mis propios diseños y prototipos iniciales. Después utilicé Claude Code para continuar el trabajo en Figma, donde quedaron diez pantallas con dos componentes reutilizables (botón y medidor), y para desarrollar la interfaz en Java Swing con Graphics2D. Más adelante corregí en Figma un modal que ocultaba un botón.

**Refinamiento.** Con la aplicación funcionando pedí mejorar la experiencia visual, la accesibilidad y las animaciones con restricciones claras como "No quiero rehacer el proyecto" y "no declares certificaciones ni conformidad completa sin evaluaciones formales". Al ver la ejecución real escribí otro prompt porque "todavía se nota que el acabado visual no está al nivel que debería tener una aplicación educativa e interactiva de museo", y pedí pulir las criaturas, el océano y los efectos sin cambiar la jugabilidad.

**Respuesta y decisiones.** El asistente midió primero los problemas (contraste, foco de teclado, parpadeo, tamaño de ventana y costo de dibujo), propuso un plan y lo implementó después de mi aprobación. Descarté su primera idea para agrandar la interfaz, la escala propia de Java, porque al probarla en Linux ignoraba los factores fraccionarios, y se reemplazó por una escala dentro del programa. Cuando pedí corregir "definitivamente los defectos de legibilidad, renderizado tipográfico y distribución de textos", el asistente buscó la causa en vez de cambiar fuentes al azar: bajo WSLg, Java no recibía la indicación de suavizar el texto.

## 3. Pruebas manuales y correcciones

Mis pruebas manuales consistían en usar la aplicación como un visitante o como el personal del museo, y con lo que encontraba escribía el prompt de corrección. Las pruebas automatizadas (JUnit) las desarrolló después Claude Code, y algunas cambiaron cuando el comportamiento esperado cambió a propósito. La regla era corregir el código y conservar los criterios de validación, no ajustar una prueba para que una implementación incorrecta pareciera funcionar.

**Qué pedí.** Después de jugar escribí: "He probado personalmente la aplicación y encontrado dos problemas funcionales importantes". Los depredadores no atacaban a Archelon aunque se quedara quieta y el ecosistema perdía sus presas. Pedí corregir las reglas existentes sin crear sistemas nuevos y "No presupongas una causa sin reproducirla".

**Respuesta y decisiones.** El asistente reprodujo y midió las causas: los depredadores pasaban casi todo el tiempo saciados y las crías nacían junto a adultos de su especie, que se las comían. Acepté que los cazadores tuvieran hambre más rápido, que ninguna especie se comiera a la propia, más plancton y la llegada ocasional de individuos cuando una especie queda casi extinta. El refinamiento de este prompt fue científico y está en el tema 6.

**Refinamiento con el comportamiento esperado.** Al revisar un informe del asistente detecté que "Continuar" abría la partida de ejemplo en una instalación nueva, lo que contradecía lo que habíamos decidido. Esta vez el prompt describía lo observado, lo esperado ("«Continuar» debe indicar que no existen partidas personales guardadas") y lo que no se podía tocar: "No implementes sistemas nuevos de importación, migración o administración". La partida de ejemplo pasó a una carpeta aparte y una prueba del entregable comprueba ese comportamiento.

## 4. Revisiones de código con Claude Code y Codex

**Qué pedí.** Auditar el proyecto completo contra el enunciado, con un agente de Claude y con Codex trabajando por separado y sin modificar el código. El prompt fija criterios: "El enunciado oficial es la máxima autoridad para esta auditoría" y "No declares el proyecto terminado sin evidencia". Antes, Claude Code ya les había pedido a dos revisores (instrucción de Claude Code) un formato fijo: gravedad, archivo y línea, escenario de fallo y causa raíz, sin opiniones de estilo.

**Respuesta.** Las revisiones encontraron defectos reales que las pruebas no cubrían, como partidas que no se restauraban igual o filas del catálogo que se perdían al guardar. Los defectos que se corrigieron se reprodujeron antes y cada uno quedó con una prueba de regresión.

**Refinamiento y decisiones.** No acepté los informes completos. Respondí autorizando solo lo que revisé: "Autorizo únicamente las siguientes acciones" y "No introduzcas nuevas jerarquías de excepciones, clases, abstracciones o comentarios explicativos". También rechacé cambios que no aportaban, como eliminar `Leaderboard.capacity()`, porque "Su retirada no aporta un beneficio suficiente para modificar código estable". Dos hallazgos se resolvieron documentando la decisión en vez de cambiar el código. En la revisión de los diagramas UML el asistente empezó corrigiendo su propio generador en Java, así que precisé que "Los archivos `.mmd` serán la única fuente editable y autoritativa de los diagramas" y que "No debe existir ningún otro generador de diagramas". El generador se eliminó y Codex comprobó que los diagramas coinciden con el código.

## 5. Modelos locales NES/FMI

Usé mis modelos locales NES/FMI como apoyo para generar bloques de código durante el desarrollo. Esos bloques se integraron con el resto del proyecto y pasaron por las mismas pruebas y revisiones que el código escrito con Claude Code.

## 6. Investigación científica y comportamiento de las especies

**Qué se pidió.** Pedí apoyo a Claude Code y a Codex para investigar las especies y fundamentar sus comportamientos, con la condición de la instrucción maestra: "No inventes información paleontológica ni fuentes". Claude Code delegó la búsqueda en un subagente (instrucción de Claude Code) que exigía fuentes revisadas por pares o bases de datos científicas, no aceptar datos generados por IA sin fuente, descartar especies que no convivieran y marcar las incertidumbres.

**Respuesta y decisiones.** Entregó ocho especies con referencias y advirtió que no todas coinciden en el mismo instante y que un dato de _Enchodus_ tiene una fuente débil. Al revisar ese resultado se corrigieron errores: _Tylosaurus_ estaba en la superficie aunque su zona era una decisión de diseño, así que pasó a aguas intermedias; _Elasmosaurus_ figuraba como carnívoro general y habría podido cazar tortugas, pero las fuentes indican peces e invertebrados; _Baculites_ se dibujaba enrollado aunque su concha es recta. Como pedí distinguir los datos de las decisiones del juego, [fuentes.md](fuentes.md) separa lo que dicen las fuentes de lo que se decidió para la simulación.

**Refinamiento.** Mi pedido del tema 3 podía llevar a forzar ataques contra la tortuga, así que envié otro prompt: "La fidelidad científica y el cumplimiento del enunciado oficial tienen prioridad sobre aumentar artificialmente la dificultad del juego", "No inventes relaciones depredador-presa" y "La ausencia de ataques durante una partida concreta no constituye por sí misma un defecto".

**Resultado.** Al revisar cada relación de alimentación contra las fuentes apareció una contradicción real: _Baculites_ podía comer peces y morder a una Archelon recién nacida, aunque se alimentaba de zooplancton, así que su dieta pasó a solo plancton. Se descartó la prueba que exigía que un jugador quieto fuera mordido en la mayoría de las partidas, porque ese criterio venía de mi primer pedido y no de la ciencia. La nueva prueba comprueba que solo las especies capaces de cazar reptiles pueden atacar a la tortuga.

## 7. Corrección y mejora de la documentación

**Qué pedí.** Los asistentes trabajaron sobre mis borradores para corregir ortografía, puntuación, vocabulario, claridad y fluidez, y para ordenar algunas explicaciones. Algunas secciones que se agregaron durante el desarrollo, como la comparación de prototipos y las explicaciones de los diagramas UML, se redactaron con su apoyo y luego las revisé. Primero pedí quitar los caracteres `«` y `»` de los textos visibles, y como el resultado se quedó corto refiné: "mi solicitud no era eliminar únicamente `«` y `»`". Después pedí que los textos "parezcan escritos naturalmente por un estudiante colombiano de Ingeniería de Sistemas", con un ejemplo concreto del problema y la condición de "No quiero que reescribas todo por obligación. Quiero que corrijas únicamente lo que realmente necesite mejorar".

**Refinamiento.** Esa revisión partió frases largas y dejó párrafos con demasiados puntos. En el siguiente prompt describí ese problema con un ejemplo de antes y después, pedí "un equilibrio natural entre puntos, comas, conjunciones y longitud de las oraciones" y que Codex actuara como revisor independiente de solo lectura.

**Respuesta y decisiones.** Claude Code hizo pocos cambios puntuales y Codex coincidió en los pasajes problemáticos. Al revisar las ediciones, Codex rechazó una porque perdía información (cuántas partidas ganaba el jugador automático) y acepté su corrección.

## Verificación y cuidado de la información

No di por bueno ningún resultado solo porque el asistente lo afirmara. Los cambios de código se comprobaron compilando, ejecutando las pruebas automatizadas y usando la aplicación real en Linux y en Windows. La información científica se contrastó con las referencias de [fuentes.md](fuentes.md) antes de entrar al catálogo.

Con los asistentes compartí los archivos del proyecto, el enunciado, mis diseños, mis borradores y lo que observaba en mis pruebas. No les di datos personales ni credenciales, y las consultas externas se limitaron a fuentes científicas públicas.

## Reflexión personal

La IA me ayudó a avanzar más rápido en la implementación, a encontrar errores que mis pruebas no detectaban y a revisar el proyecto desde dos puntos de vista distintos con Claude Code y Codex.

También fue muy útil para trabajar en aspectos que todavía se me dificultan. Por mi experiencia laboral y liderando equipos, he aprendido a aplicar convenciones de UI/UX y tengo criterios claros sobre cómo quiero que funcione y se vea una interfaz. Sin embargo, llevar mis prototipos y mockups a una aplicación funcional fue uno de los mayores retos, especialmente con Graphics2D, una herramienta que no había utilizado directamente. Aunque me desenvuelvo en el desarrollo de aplicaciones web y de escritorio, la creación de entornos visuales sigue siendo una de mis principales debilidades. Con ayuda de la IA pude avanzar mucho más rápido en esa parte, implementar mis diseños y corregir errores visuales que por mi cuenta me habrían tomado bastante más tiempo.

También cometió errores. En la información científica apareció un dato que contradecía las fuentes, como la dieta de _Baculites_, y algunas características se presentaban como datos cuando eran decisiones de diseño. Por eso revisé cada especie contra sus referencias antes de aceptarla en el catálogo.

Una limitación es que el asistente hace lo que se le pide aunque el pedido esté mal planteado. Cuando pedí que los depredadores atacaran más, la primera solución incluía una prueba que forzaba ataques, y tuve que corregir mi propia instrucción para que la ciencia tuviera prioridad sobre la dificultad. Aprendí a dar contexto y razones, poner restricciones claras, mostrar ejemplos del problema, dividir el trabajo y pedir criterios de verificación antes de aceptar un resultado.

Fui cuidadoso con la información que compartí. Gran parte del código la generaron los asistentes a partir de mis diseños y diagramas, pero lo revisé, lo probé y decidí qué aceptar o rechazar, así que la responsabilidad de entenderlo y defenderlo es mía.
