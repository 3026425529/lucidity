package ml.mypals.lucidity.config.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ml.mypals.lucidity.config.LucidityConfigScreen;

public final class LucidityModMenuImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return LucidityConfigScreen::new;
    }
}

