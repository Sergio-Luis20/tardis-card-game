package br.sergio.tcg.game;

import lombok.ToString;

import java.util.Arrays;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

@ToString
public class IncreaseAbsOnlyAttributeInstance extends AttributeInstance {

    public IncreaseAbsOnlyAttributeInstance(double base) {
        super(base);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance setBase(double base) {
        double baseSignum = signum(this.base);
        if (baseSignum != 0 && (signum(base) != baseSignum || abs(base) <= abs(this.base))) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.setBase(base);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance addBase(double base) {
        if (signum(base) != signum(this.base)) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.addBase(base);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance subtractBase(double base) {
        if (signum(base) == signum(this.base)) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.subtractBase(base);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance setFlat(double flat) {
        if (signum(flat) != signum(base) || abs(flat) <= abs(this.flat)) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.setFlat(flat);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance addFlat(double flat) {
        if (signum(flat) != signum(this.flat)) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.addFlat(flat);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance subtractFlat(double flat) {
        if (signum(flat) == signum(this.flat)) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.subtractFlat(flat);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance clearFlat() {
        return this;
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance setMultiplier(int layer, double value) {
        if (value <= getMultiplier(layer)) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.setMultiplier(layer, value);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance addMultiplier(int layer, double value) {
        if (value <= 0) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.addMultiplier(layer, value);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance subtractMultiplier(int layer, double value) {
        if (value >= 0) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.subtractMultiplier(layer, value);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance setMultipliers(double[] multipliers) {
        if (multipliers.length < this.multipliers.length) {
            return this;
        }
        for (int i = 0; i < multipliers.length; i++) {
            if (multipliers[i] < getMultiplier(i)) {
                return this;
            }
        }
        return (IncreaseAbsOnlyAttributeInstance) super.setMultipliers(multipliers);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance addNewMultiplier(double value) {
        if (value <= 0) {
            return this;
        }
        return (IncreaseAbsOnlyAttributeInstance) super.addNewMultiplier(value);
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance clearMultipliers() {
        return this;
    }

    @Override
    public IncreaseAbsOnlyAttributeInstance clone() {
        var copy = new IncreaseAbsOnlyAttributeInstance(base);
        copy.flat = flat;
        copy.multipliers = Arrays.copyOf(multipliers, multipliers.length);
        return copy;
    }

}
