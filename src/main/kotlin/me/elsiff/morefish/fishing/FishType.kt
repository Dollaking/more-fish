package me.elsiff.morefish.fishing

import me.elsiff.morefish.fishing.catcheffect.CatchHandler
import me.elsiff.morefish.fishing.condition.FishCondition
import org.bukkit.inventory.ItemStack
import kotlin.math.floor
import kotlin.random.Random

/**
 * Created by elsiff on 2018-12-20.
 */
data class FishType(
    val name: String,
    val rarity: FishRarity,
    val displayName: String,
    val lengthMin: Double,
    val lengthMax: Double,
    val icon: ItemStack,
    val catchHandlers: Set<CatchHandler>,
    val conditions: Set<FishCondition> = emptySet(),
    val skipItemFormat: Boolean = false
) {
    fun generateFish(): Fish {
        check(lengthMin <= lengthMax) { "Max-length must not be smaller than min-length" }

        val length = lengthMin + Random.nextDouble() * (lengthMax - lengthMin)
        return Fish(this, floor(length * 10) / 10)
    }
}
