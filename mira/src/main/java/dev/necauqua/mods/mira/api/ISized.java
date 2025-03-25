/*
 * Copyright (c) 2017-2022 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.mira.api;

import dev.necauqua.mods.mira.data.SimpleAttribute;

/**
 * An interface describing something <i>sized</i>, meaning it has a size
 * attribute of type double.
 * This is usually implemented on vanilla types (Entities, Particles etc.) by a
 * mixin.
 * <p>
 * See {@link IRenderSized} for more description and for example usage as a soft
 * dependency.
 *
 * @author necauqua
 * @see IRenderSized
 */
public interface ISized {

    /**
     * @return the size multiplier of this thing
     */
    @Deprecated
    double getSizeCM();

    default double getSizeCM(SimpleAttribute attribute) {
        return getSizeCM();
    }

    /**
     * Sets the size of this thing.
     * <p>
     * <b>NO CHECKS ARE DONE FOR SIZE CORRECTNESS</b>
     * (e.g. you can set it to NaN or negative and get a bunch of unexpected
     * behavior).
     *
     * @param size the size to be set
     */
    @Deprecated
    void setSizeCM(double size);
}
