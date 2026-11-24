package co.edu.autonoma.ancientseas.ui;

record CreaturePose(double radius, double phase, double time, double hurt) {

    static CreaturePose still(double radius) {
        return new CreaturePose(radius, 0.6, 0.6, 0);
    }
}
