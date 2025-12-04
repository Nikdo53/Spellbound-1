package com.ombremoon.spellbound.common.init;

import com.ombremoon.spellbound.main.CommonClass;
import net.minecraft.resources.ResourceLocation;

public class SBPageScraps {
    public static final ResourceLocation UNLOCKED_SOLAR_RAY = scrap("solar_ray_unlock");

    private static ResourceLocation scrap(String path) {
        return CommonClass.customLocation(path);
    }
}
