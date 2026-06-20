package com.misaka.gtlleisureaddon.registry;

import com.misaka.gtlleisureaddon.GTLLeisureAddon;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

public final class LeisureRegistration {

    public static final GTRegistrate REGISTRATE = GTRegistrate.create(GTLLeisureAddon.MOD_ID);

    private LeisureRegistration() {}
}