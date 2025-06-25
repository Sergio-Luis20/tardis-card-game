package br.sergio.tcg.game;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@ToString
public class AttributeInstance implements Cloneable {

    protected double base, flat;
    protected double[] multipliers;

    public AttributeInstance(double base) {
        this.base = base;
        multipliers = new double[0];
    }

    public AttributeInstance(AttributeInstance other) {
        this.base = other.base;
        this.flat = other.flat;
        this.multipliers = Arrays.copyOf(other.multipliers, other.multipliers.length);
    }

    public AttributeInstance setBase(double base) {
        this.base = base;
        return this;
    }

    public AttributeInstance addBase(double base) {
        this.base += base;
        return this;
    }

    public AttributeInstance subtractBase(double base) {
        this.base -= base;
        return this;
    }

    public AttributeInstance setFlat(double flat) {
        this.flat = flat;
        return this;
    }

    public AttributeInstance addFlat(double flat) {
        this.flat += flat;
        return this;
    }

    public AttributeInstance subtractFlat(double flat) {
        this.flat -= flat;
        return this;
    }

    public AttributeInstance clearFlat() {
        flat = 0;
        return this;
    }

    public AttributeInstance clear() {
        return clearFlat().clearMultipliers();
    }

    public double getMultiplier(int layer) {
        return layer >= multipliers.length ? 0 : multipliers[layer];
    }

    public double getLastMultiplier() {
        return multipliers.length == 0 ? 0 : multipliers[multipliers.length - 1];
    }

    public AttributeInstance setMultiplier(int layer, double value) {
        growMultipliersArrayIfNeeded(layer + 1);
        multipliers[layer] = value;
        return this;
    }

    public AttributeInstance addMultiplier(int layer, double value) {
        growMultipliersArrayIfNeeded(layer + 1);
        multipliers[layer] += value;
        return this;
    }

    public AttributeInstance subtractMultiplier(int layer, double value) {
        growMultipliersArrayIfNeeded(layer + 1);
        multipliers[layer] -= value;
        return this;
    }

    public AttributeInstance setMultipliers(double[] multipliers) {
        this.multipliers = multipliers == null ? new double[0] : multipliers;
        return this;
    }

    public AttributeInstance addNewMultiplier(double value) {
        multipliers = Arrays.copyOf(multipliers, multipliers.length + 1);
        multipliers[multipliers.length - 1] = value;
        return this;
    }

    public AttributeInstance clearMultipliers() {
        multipliers = new double[0];
        return this;
    }

    private void growMultipliersArrayIfNeeded(int newLength) {
        if (multipliers.length < newLength) {
            multipliers = Arrays.copyOf(multipliers, newLength);
        }
    }

    public double calculate() {
        double value = base;

        for (double multiplier : multipliers) {
            value *= 1 + multiplier;
        }

        return value + flat;
    }

    @Override
    public AttributeInstance clone() {
        return new AttributeInstance(this);
    }

}
