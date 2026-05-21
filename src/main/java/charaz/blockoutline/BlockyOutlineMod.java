package charaz.blockoutline;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockyOutlineMod implements ModInitializer {
    public static final String MOD_ID = "blocky-outline";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Blocky Outline (Java) has been initialized!");
    }
}
