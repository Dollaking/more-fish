package me.elsiff.morefish.configuration.loader;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import me.elsiff.morefish.fishing.FishRarity;
import me.elsiff.morefish.fishing.FishType;
import me.elsiff.morefish.fishing.catchhandler.CatchCommandExecutor;
import me.elsiff.morefish.fishing.catchhandler.CatchFireworkSpawner;
import me.elsiff.morefish.fishing.catchhandler.CatchHandler;
import org.bukkit.configuration.ConfigurationSection;

public final class FishTypeMapLoader implements CustomLoader<Map<FishRarity, Set<FishType>>> {

    private final CustomItemStackLoader customItemStackLoader;
    private final FishConditionSetLoader fishConditionSetLoader;
    private final FishRaritySetLoader fishRaritySetLoader;
    private final PlayerAnnouncementLoader playerAnnouncementLoader;

    public FishTypeMapLoader(@Nonnull FishRaritySetLoader fishRaritySetLoader, @Nonnull CustomItemStackLoader customItemStackLoader, @Nonnull FishConditionSetLoader fishConditionSetLoader, @Nonnull PlayerAnnouncementLoader playerAnnouncementLoader) {
        this.fishRaritySetLoader = fishRaritySetLoader;
        this.customItemStackLoader = customItemStackLoader;
        this.fishConditionSetLoader = fishConditionSetLoader;
        this.playerAnnouncementLoader = playerAnnouncementLoader;
    }

    private FishRarity findRarity(Set<FishRarity> rarities, String name) {
        return rarities.stream().filter(fishRarity -> name.equals(fishRarity.getName())).findFirst().orElseThrow(() -> new IllegalStateException("Rarity " + name + " doesn't exist."));
    }

    @Nonnull
    public Map<FishRarity, Set<FishType>> loadFrom(@Nonnull ConfigurationSection section, @Nonnull String path) {
        Set<FishRarity> rarities = fishRaritySetLoader.loadFrom(section, "rarity-list");
        ConfigurationSection fishList = section.getConfigurationSection("fish-list");
        return fishList.getKeys(false).stream().map(fishList::getConfigurationSection).map(groupByRarity -> {
            FishRarity rarity = findRarity(rarities, groupByRarity.getName());
            Set<FishType> fishTypes = groupByRarity.getKeys(false).stream().map(groupByRarity::getConfigurationSection).map(cs -> {
                List<CatchHandler> catchHandlers = new ArrayList<>(rarity.getCatchHandlers());
                if (cs.contains("commands")) {
                    catchHandlers.add(new CatchCommandExecutor(cs.getStringList("commands")));
                }
                else if (cs.getBoolean("firework", false)) {
                    catchHandlers.add(new CatchFireworkSpawner());
                }

                return new FishType(cs.getName(), rarity, cs.getString("display-name"), cs.getDouble("length-min"), cs.getDouble("length-max"), customItemStackLoader.loadFrom(cs, "icon"), catchHandlers, playerAnnouncementLoader.loadIfExists(cs, "catch-announce").orElse(rarity.getCatchAnnouncement()), fishConditionSetLoader.loadFrom(cs, "conditions"), cs.getBoolean("skip-item-format", rarity.hasNotFishItemFormat()), cs.getBoolean("no-display", rarity.getNoDisplay()), cs.getBoolean("firework", rarity.hasCatchFirework()), rarity.getAdditionalPrice() + cs.getDouble("additional-price", 0D));
            }).collect(Collectors.toSet());
            return new SimpleEntry<>(rarity, fishTypes);
        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
}
