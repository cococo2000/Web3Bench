package com.olxpbenchmark.distributions;

public class PoissonGenerator {

    public PoissonGenerator() {
    }

    public int nextPoisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= Math.random();
        } while (p > L);

        return k - 1;
    }
}
