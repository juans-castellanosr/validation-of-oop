# Prototipos y comparación con la interfaz final

Los prototipos se diseñaron en Figma antes de programar las pantallas en Swing.

- Archivo: <https://www.figma.com/design/DTPk4klHXuG0AKrn12FBri/OOP-103018-Ancient-Seas> (página "Prototipos Swing", 2026-11-21).
- Exportaciones: los archivos `01` a `10` de esta carpeta. Las capturas finales están en `../capturas/`.

En el prototipo las criaturas son óvalos de referencia; en el juego se dibujan con Graphics2D según la forma de cada especie.

| #   | Prototipo                       | Final                       |
| --- | ------------------------------- | --------------------------- |
| 1   | ![Menú: prototipo][p1]          | ![Menú: final][f1]          |
| 2   | ![Juego con HUD: prototipo][p2] | ![Juego con HUD: final][f2] |
| 3   | ![Pausa: prototipo][p3]         | ![Pausa: final][f3]         |
| 4   | ![Ficha: prototipo][p4]         | ![Ficha: final][f4]         |
| 5   | ![Ajustes: prototipo][p5]       | ![Ajustes: final][f5]       |
| 6   | ![Resultados: prototipo][p6]    | ![Resultados: final][f6]    |
| 7   | ![Confirmación: prototipo][p7]  | ![Confirmación: final][f7]  |
| 8   | ![PIN: prototipo][p8]           | ![PIN: final][f8]           |
| 9   | ![Catálogo: prototipo][p9]      | ![Catálogo: final][f9]      |
| 10  | ![Formulario: prototipo][p10]   | ![Formulario: final][f10]   |

Cambios de cada pantalla y su motivo, con el número de la tabla:

1. **Menú principal.** Se añadieron un emblema y "Cómo jugar" en tres pasos con símbolos, para entender el juego sin leer. El foco empieza en "Jugar". Con poco espacio, el menú pasa a dos columnas.
2. **Juego con HUD.** Cada barra muestra su nombre, símbolo y porcentaje, con la siguiente etapa o un aviso debajo. Los botones indican sus atajos (P, G y Esc) y en la parte inferior se explican los controles.
3. **Juego en pausa.** El panel con tres botones pasó a un velo con texto: la barra superior ya tiene "Seguir", "Guardar" y "Terminar".
4. **Ficha de especie.** Secciones "Datos" (de las fuentes), "En el juego" (decisiones de diseño), "¿Sabías que...?" y la fuente, más la explicación de por qué las especies aparecen juntas.
5. **Ajustes.** Agrupados en "Jugador", "Partida" y "Accesibilidad"; se añadieron "Movimiento reducido" y "Tamaño de la interfaz".
6. **Resultados.** Cabecera dibujada con la tortuga en la etapa alcanzada; si se pierde, un consejo. Las puntuaciones pasaron de lista a tabla.
7. **Confirmación.** Los botones dicen lo que hacen ("Sí, terminar", "No, seguir jugando") y la opción segura es la predeterminada.
8. **Acceso de administración.** `JOptionPane` en español con `JPasswordField`, que recibe el foco.
9. **Catálogo.** Filas alternadas, encabezado oscuro, números a la derecha y columnas del ancho de su contenido. Se añadió "Cambiar PIN" y se quitó "Exportar XML" (las exportaciones están en Puntuaciones y Resultados).
10. **Formulario de especie.** Mismos campos; los errores aparecen con el mensaje del propio modelo y el primer campo recibe el foco.

Además, en toda la interfaz:

- el contraste cumple WCAG AA (lo comprueba una prueba);
- el foco del teclado es visible y nada parpadea;
- el texto se ve suavizado también en Linux;
- las ventanas se adaptan al 100, 125 y 150 % sin cortar texto.

[p1]: 01-menu-principal.png
[p2]: 02-juego-hud.png
[p3]: 03-juego-en-pausa.png
[p4]: 04-ficha-especie.png
[p5]: 05-ajustes.png
[p6]: 06-resultados.png
[p7]: 07-confirmacion.png
[p8]: 08-acceso-administracion.png
[p9]: 09-catalogo-administracion.png
[p10]: 10-formulario-especie.png
[f1]: ../capturas/01-menu.png
[f2]: ../capturas/02-juego.png
[f3]: ../capturas/03-pausa.png
[f4]: ../capturas/04-ficha.png
[f5]: ../capturas/05-ajustes.png
[f6]: ../capturas/06-resultados.png
[f7]: ../capturas/07-confirmacion.png
[f8]: ../capturas/08-pin.png
[f9]: ../capturas/09-catalogo.png
[f10]: ../capturas/10-formulario.png
