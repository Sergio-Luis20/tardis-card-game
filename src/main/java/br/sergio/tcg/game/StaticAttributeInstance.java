package br.sergio.tcg.game;

public class StaticAttributeInstance extends AttributeInstance {

    public StaticAttributeInstance(double base) {
        super(base);
    }

    @Override
    public StaticAttributeInstance setBase(double base) {
        return this;
    }

    @Override
    public StaticAttributeInstance addBase(double base) {
        return this;
    }

    @Override
    public StaticAttributeInstance subtractBase(double base) {
        return this;
    }

    @Override
    public StaticAttributeInstance setFlat(double flat) {
        return this;
    }

    @Override
    public StaticAttributeInstance addFlat(double flat) {
        return this;
    }

    @Override
    public StaticAttributeInstance subtractFlat(double flat) {
        return this;
    }

    @Override
    public StaticAttributeInstance clearFlat() {
        return this;
    }

    @Override
    public StaticAttributeInstance setMultiplier(int layer, double value) {
        return this;
    }

    @Override
    public StaticAttributeInstance addMultiplier(int layer, double value) {
        return this;
    }

    @Override
    public StaticAttributeInstance subtractMultiplier(int layer, double value) {
        return this;
    }

    @Override
    public StaticAttributeInstance setMultipliers(double[] multipliers) {
        return this;
    }

    @Override
    public StaticAttributeInstance addNewMultiplier(double value) {
        return this;
    }

    @Override
    public StaticAttributeInstance clearMultipliers() {
        return this;
    }

    @Override
    public StaticAttributeInstance clone() {
        return new StaticAttributeInstance(base);
    }

}
