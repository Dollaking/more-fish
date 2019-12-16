package me.elsiff.morefish.fishing;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import me.elsiff.morefish.fishing.competition.FishingCompetition;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public final class FishTypeTable {

    private final BiMap<FishRarity, Set<FishType>> map = HashBiMap.create();

    public void clear() {
        map.clear();
    }

    @Nonnull
    public Optional<FishRarity> getDefaultRarity() {
        Set<FishRarity> defaultRarities = getRarities().stream().filter(FishRarity::isDefault).collect(Collectors.toSet());
        if (defaultRarities.size() <= 1) {
            if (!defaultRarities.isEmpty()) {
                return Optional.of(defaultRarities.iterator().next());
            }
        }

        return Optional.empty();
    }

    @Nonnull
    public Set<FishRarity> getRarities() {
        return map.keySet();
    }

    @Nonnull
    public Set<FishType> getTypes() {
        return map.values().stream().flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Nonnull
    public FishRarity pickRandomRarity() {
        double probabilitySum = getRarities().stream().filter(rarity -> !rarity.isDefault()).mapToDouble(FishRarity::getProbability).sum();
        if (probabilitySum >= 1.0) {
            throw new IllegalStateException("Sum of rarity probabilities must not be bigger than 1.0");
        }

        Set<FishRarity> rarities = getRarities();
        double randomVal = new Random().nextDouble();
        double chanceSum = 0.0;
        for (FishRarity rarity : rarities) {
            if (!rarity.isDefault()) {
                chanceSum += rarity.getProbability();
                if (randomVal <= chanceSum) {
                    return rarity;
                }
            }
        }

        return getDefaultRarity().orElseThrow(() -> new IllegalStateException("Default rarity doesn't exist"));
    }

    @Nonnull
    public FishType pickRandomType(@Nonnull FishRarity fishRarity) {
        if (!map.containsKey(fishRarity)) {
            throw new IllegalStateException("Rarity must be contained in the table");
        }

        return new ArrayList<>(map.get(fishRarity)).get(new Random().nextInt(map.size()));
    }

    @Nonnull
    public FishType pickRandomType(@Nonnull Item caught, @Nonnull Player fisher, @Nonnull FishingCompetition competition) {
        return pickRandomType(caught, fisher, competition, pickRandomRarity());
    }

    @Nonnull
    public FishType pickRandomType(@Nonnull Item caught, @Nonnull Player fisher, @Nonnull FishingCompetition competition, @Nonnull FishRarity rarity) {
        if (!map.containsKey(rarity)) {
            throw new IllegalStateException("Rarity must be contained in the table");
        }

        List<FishType> types = map.get(rarity).stream().filter(type -> type.getConditions().stream().allMatch(condition -> condition.check(caught, fisher, competition))).collect(Collectors.toList());
        if (types.isEmpty()) {
            throw new IllegalStateException("No fish type matches given condition");
        }

        return types.get(new Random().nextInt(types.size()));
    }

    public void putAll(Map<FishRarity, Set<FishType>> map) {
        this.map.putAll(map);
    }
}
