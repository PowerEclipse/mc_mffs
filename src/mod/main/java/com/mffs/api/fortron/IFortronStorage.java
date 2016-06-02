package com.mffs.api.fortron;

/**
 * A grid MFFS uses to search for machines with frequencies that can be linked and spread Fortron
 * energy.
 *
 * @author Calclavia
 */
public interface IFortronStorage {
    void setFortronEnergy(int paramInt);

    int getFortronEnergy();

    int getFortronCapacity();

    int requestFortron(int paramInt, boolean paramBoolean);

    int provideFortron(int paramInt, boolean paramBoolean);
}