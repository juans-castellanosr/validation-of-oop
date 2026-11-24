package co.edu.autonoma.ancientseas.ui;

import java.awt.Graphics2D;

@FunctionalInterface
interface CreaturePainter {

    void paint(Graphics2D g, CreaturePose pose);
}
