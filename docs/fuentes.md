# Fuentes científicas del catálogo de especies

Respaldo de `src/main/resources/data/species.csv`. Escenario: Mar Interior Occidental de Norteamérica, Campaniano (Cretácico tardío, aprox. 83,6 a 72,1 millones de años, Ma).

Leyenda: **V** = verificado en fuente primaria o base de datos científica; **E** = afirmación de una fuente secundaria (Wikipedia, sitio divulgativo) que cita literatura, no contrastada con el artículo original; **D** = dato desconocido o decisión de diseño.

## Especies

| Especie                  | Long. (m) | Dieta         | Zona         | Aire |
| ------------------------ | --------- | ------------- | ------------ | ---- |
| _Tylosaurus proriger_    | 9,0       | Carnívoro     | Aguas medias | Sí   |
| _Cretoxyrhina mantelli_  | 5,0       | Carnívoro     | Aguas medias | No   |
| _Elasmosaurus platyurus_ | 10,3      | Piscívoro     | Aguas medias | Sí   |
| _Xiphactinus audax_      | 5,0       | Piscívoro     | Aguas medias | No   |
| _Archelon ischyros_      | 3,5       | Omnívoro      | Aguas medias | Sí   |
| _Enchodus petrosus_      | 0,8       | Piscívoro     | Aguas medias | No   |
| _Baculites_              | 1,0       | Come plancton | Aguas medias | No   |
| _Platyceramus platinus_  | 2,0       | Filtrador     | Fondo marino | No   |

Nombre común, rango hallado, nivel de evidencia y referencias de cada especie:

- **_Tylosaurus proriger_ (Tilosaurio).** Rango de longitud: 3,9 a 9,5 m (revisión reciente) y hasta unos 11 a 14 m (estimaciones antiguas). Evidencia: rango V; longitud E; zona D.
  - Paleobiology Database: <https://paleobiodb.org/data1.2/taxa/single.json?name=Tylosaurus%20proriger&show=app>
  - Everhart, _Oceans of Kansas_: <https://oceansofkansas.com/Tylo-prey.html>
  - Wikipedia: <https://en.wikipedia.org/wiki/Tylosaurus>
- **_Cretoxyrhina mantelli_ (Tiburón Ginsu).** Rango: 5 a 8 m (hasta 9 en estimaciones extremas). Evidencia: esqueleto de 5 m V; último registro E; zona D.
  - Shimada (2008), _J. Vertebr. Paleontol._ 28(1): <https://bioone.org/journals/journal-of-vertebrate-paleontology/volume-28/issue-1/0272-4634(2008)28[21:OPALHS]2.0.CO;2/Ontogenetic-Parameters-and-Life-History-Strategies-of-the-Late-Cretaceous/10.1671/0272-4634(2008)28[21:OPALHS]2.0.CO;2.full>
  - Wikipedia: <https://en.wikipedia.org/wiki/Cretoxyrhina>
- **_Elasmosaurus platyurus_ (Elasmosaurio).** Rango: 10,3 a 13 m. Dieta: peces e invertebrados, probable. Evidencia: edad y 72 vértebras V; longitud E; dieta y zona D.
  - Sachs, Kear & Everhart (2013), _PLoS ONE_ 8(8): e70877: <https://doi.org/10.1371/journal.pone.0070877>
  - Wikipedia: <https://en.wikipedia.org/wiki/Elasmosaurus>
- **_Xiphactinus audax_ (Pez colmillos gigante).** Rango: 4,2 a 6 m. Evidencia: rango V; longitud y dieta E (con especímenes con contenido estomacal).
  - Paleobiology Database: <https://paleobiodb.org/data1.2/taxa/single.json?name=Xiphactinus%20audax&show=app>
  - "Another Sternberg Fish-within-a-Fish Discovery", _Trans. Kansas Acad. Sci._ 113(3-4): <https://complete.bioone.org/journals/transactions-of-the-kansas-academy-of-science/volume-113/issue-3_2f_4/062.113.0306/Another-Sternberg-Fish-within-a-Fish-Discovery--First-Report/10.1660/062.113.0306.full>
  - Wikipedia: <https://en.wikipedia.org/wiki/Xiphactinus>
- **_Archelon ischyros_ (Archelón, el jugador).** Longitud usada: la del holotipo; rango 3,5 a 4,6 m (ejemplar mayor). Dieta: omnívoro, incierta. Evidencia: formación y rango V en PBDB y E en Wikipedia; dieta D (hipótesis).
  - Wikipedia, que cita a Wieland 1896 (holotipo del Pierre Shale de Dakota del Sur): <https://en.wikipedia.org/wiki/Archelon>
  - Paleobiology Database: <https://paleobiodb.org/data1.2/taxa/single.json?name=Archelon%20ischyros&show=app>
- **_Enchodus petrosus_ (Pez sable de colmillos).** Rango: 0,77 a 1,5 m. Evidencia: E (solo fuentes secundarias).
  - Wikipedia: <https://en.wikipedia.org/wiki/Enchodus>
- **_Baculites_ (Baculites).** Rango: 0,07 a 2 m según la especie. Dieta: zooplancton. Evidencia: E; el valor de 1,0 m es D (intermedio).
  - Cobban y colaboradores (2006), USGS Open-File Report 2006-1250: <https://pubs.usgs.gov/publication/ofr20061250>
  - Wikipedia: <https://en.wikipedia.org/wiki/Baculites>
- **_Platyceramus platinus_ (Almeja gigante).** Rango: 1 a 3 m. Dieta: filtrador, con posible quimiosimbiosis. Evidencia: paleoecología V; longitud E.
  - Kauffman et al. (2007), _J. Paleontol._ 81(1): <https://bioone.org/journals/journal-of-paleontology/volume-81/issue-1/0022-3360(2007)81[64:POGIPO]2.0.CO;2/PALEOECOLOGY-OF-GIANT-INOCERAMIDAE-PLATYCERAMUS-ON-A-SANTONIAN-CRETACEOUS-SEAFLOOR/10.1666/0022-3360(2007)81[64:POGIPO]2.0.CO;2.full>
  - Walliser et al. (2018), _Cretaceous Research_ 86:73

Sobre la columna de dieta: _Baculites_ usa `PLANKTIVORE` ("Come plancton"): comía organismos planctónicos pequeños, como crustáceos y larvas (Kruta y colegas, 2011), así que en el juego solo come plancton y nunca peces ni crías de tortuga. _Platyceramus_ se clasifica como filtrador aunque los isótopos sugieren que pudo depender de bacterias simbiontes.

## Datos que son decisiones de diseño

- Las columnas `behavior` (comportamiento en el juego), `shape` (forma del dibujo) y, salvo _Baculites_ y _Platyceramus_, `zone` no vienen de las fuentes. La ficha muestra la forma junto al nombre científico, y la zona y el comportamiento aparte, en "En el juego".
- La ficha muestra una cita corta en español; las direcciones completas están en la lista anterior.

## Coexistencia: formaciones y solapamiento

- **Formación Niobrara, Miembro Smoky Hill Chalk** (Kansas): Coniaciano a Campaniano temprano; el USGS Geolex lo da como Coniaciano a Campaniano y el Kansas Geological Survey (Boletín 225) sitúa la sedimentación hacia ~80 Ma. Aquí aparecen _Tylosaurus_, _Cretoxyrhina_, _Xiphactinus_, _Platyceramus_ y _Enchodus_.
- **Pierre Shale, Miembro Sharon Springs** (Kansas): Campaniano temprano; es el origen del holotipo de _Elasmosaurus_ (Sachs et al. 2013). Se apoya conformablemente sobre el Niobrara, por lo que el solapamiento con el Smoky Hill superior es directo.
- **Pierre Shale superior** (Dakota del Sur): origen de _Archelon_ (Campaniano tardío, ~80 a 74 Ma según Wikipedia). También hay _Tylosaurus_ grandes con contenido estomacal (peces, el ave _Hesperornis_, un mosasaurio pequeño, posiblemente un tiburón; Martin y Bjork 1987, citado en Everhart).
- **Baculites** es un fósil guía de las zonas de amonites del Campaniano del interior occidental (Cobban et al. 2006).

## Incertidumbres declaradas

1. **No toda la lista convive en un mismo instante.** _Elasmosaurus_ (~80,6 a 77 Ma) y _Platyceramus_ (hasta el Campaniano temprano) no coinciden en el tiempo con _Archelon_ (~80 a 74 Ma) de forma comprobada. Todos pertenecen al mismo mar y al Campaniano en sentido amplio, pero el juego representa una "escena compuesta". Para el rigor temporal estricto, la ventana común más sólida es el Campaniano temprano (~83,6 a 80 Ma).
2. **_Cretoxyrhina_**: el registro más joven conocido (73,2 Ma, Formación Bearpaw, Alberta) proviene de Wikipedia que cita literatura; no se pudo contrastar con el artículo original. Se sugiere que se extinguió durante el Campaniano.
3. **_Tylosaurus proriger_**: las longitudes divergen mucho. Estimaciones antiguas llegan a 11 a 14 m, pero muchos ejemplares grandes fueron reasignados a _T. rex_; la revisión citada por Wikipedia da 3,9 a 9,5 m. Se usó 9 m (ejemplar de Kansas con plesiosaurio en el estómago). Verificar el artículo original antes de citarlo en clase.
4. **Rangos en Ma**: para varios taxones son los límites de los pisos geológicos (Paleobiology Database), no edades de fósiles concretos. Los de _Enchodus_, _Baculites_ y _Platyceramus_ son pisos/aproximaciones, no mediciones.
5. **_Enchodus petrosus_**: solo hay fuentes secundarias (Wikipedia y vendedores de fósiles). El dato curioso usa solo lo que esa fuente sostiene con claridad (el apodo "arenque dientes de sable" y que no era pariente de los arenques). Los datos del Paleobiology Database para el género _Enchodus_ mezclan edades que no son coherentes y no se usaron.
6. **_Archelon_**: la dieta es incierta. Hipótesis: moluscos de concha delgada (Williston), medusas, cefalópodos y peces; la forma del pico es compatible con cortar carne. El ejemplar mayor ("Brigitta", 4,6 m) es una cifra de Wikipedia.
7. **Zona de la columna de agua**: ninguna de las fuentes asigna zonas SURFACE/MIDWATER/SEAFLOOR salvo para _Baculites_ (aguas medias) y _Platyceramus_ (fondo). El resto es una decisión de diseño razonada, no un dato.
8. **Pruebas de mordeduras en amonites** (_Placenticeras_, Kauffman y Kesling 1960) fueron cuestionadas (Kase et al. 1998; Seilacher 1998: serían marcas de lapas). Por eso no se usan en las relaciones de caza.

## Recursos gráficos y sonoros

Todos los gráficos del juego se dibujan por programa con Java2D. No se usan imágenes, sonidos, fuentes ni otros recursos externos, por lo que no hay licencias de terceros que atribuir.
