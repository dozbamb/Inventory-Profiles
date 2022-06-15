/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2022 Plamen K. Kosseff <p.kosseff@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anti_ad.mc.ipnext.event

import org.anti_ad.mc.common.Log
import org.anti_ad.mc.common.math2d.Point
import org.anti_ad.mc.common.math2d.Rectangle
import org.anti_ad.mc.common.vanilla.Vanilla
import org.anti_ad.mc.common.vanilla.alias.ContainerScreen
import org.anti_ad.mc.common.vanilla.alias.MatrixStack
import org.anti_ad.mc.common.vanilla.alias.RenderSystem
import org.anti_ad.mc.common.vanilla.render.b
import org.anti_ad.mc.common.vanilla.render.g
import org.anti_ad.mc.common.vanilla.render.glue.rFillRect
import org.anti_ad.mc.common.vanilla.render.r
import org.anti_ad.mc.common.vanilla.render.rDisableDepth
import org.anti_ad.mc.common.vanilla.render.rEnableDepth
import org.anti_ad.mc.ipnext.config.ModSettings
import org.anti_ad.mc.ipnext.ingame.`(containerBounds)`
import org.anti_ad.mc.ipnext.ingame.`(focusedSlot)`
import org.anti_ad.mc.ipnext.ingame.`(id)`
import org.anti_ad.mc.ipnext.ingame.`(itemStack)`
import org.anti_ad.mc.ipnext.ingame.`(slots)`
import org.anti_ad.mc.ipnext.ingame.`(topLeft)`
import org.anti_ad.mc.ipnext.ingame.vPlayerSlotOf
import org.anti_ad.mc.ipnext.item.EMPTY
import org.anti_ad.mc.ipnext.item.ItemType
import org.anti_ad.mc.ipnext.item.isEmpty

object SlotHighlightHandler {

    private var toHighlight: ItemType = ItemType.EMPTY

    private var ticksSinceLastFocusChange = 3

    private val slotLocations: Map<Int, Point> // id, location // ref: LockSlotsHandler
        get() {
            val screen = Vanilla.screen() as? ContainerScreen<*> ?: return mapOf()
            return Vanilla.container().`(slots)`.mapNotNull { slot ->
                val playerSlot = vPlayerSlotOf(slot,
                                               screen)
                return@mapNotNull if (!playerSlot.`(itemStack)`.isEmpty() && playerSlot.`(itemStack)`.itemType == toHighlight) {
                    playerSlot.`(id)` to slot.`(topLeft)`
                } else {
                    null
                }
            }.toMap()
        }

    fun onBackgroundRender() {
        if (ModSettings.HIGHLIGHT_FOUSED_ITEMS.booleanValue && !ModSettings.HIGHLIGHT_FOUSED_ITEMS_FOREGROUND.booleanValue) {
           drawSprite()
        }
    }

    fun onForegroundRender() {
        if (ModSettings.HIGHLIGHT_FOUSED_ITEMS.booleanValue && ModSettings.HIGHLIGHT_FOUSED_ITEMS_FOREGROUND.booleanValue) {
            val screen = Vanilla.screen() as? ContainerScreen<*> ?: return
            val matrixStack2: MatrixStack = RenderSystem.getModelViewStack()
            matrixStack2.push()  // see HandledScreen.render()
            //rMatrixStack = matrixStack2
            val topLeft = screen.`(containerBounds)`.topLeft
            matrixStack2.translate(-topLeft.x.toDouble(),
                                   -topLeft.y.toDouble(),
                                   0.0)
            RenderSystem.applyModelViewMatrix()
            drawSprite()
            matrixStack2.pop()
            RenderSystem.applyModelViewMatrix()
        }
    }

    fun postRender() {

    }

    val defaultAlpha: Int
        get() {
            return if (ModSettings.HIGHLIGHT_FOUSED_ITEMS_FOREGROUND.booleanValue) 140 else 180
        }

    var tick = 0
    var alphaChannel = 10
    var step = 10

    private fun drawSprite() {
        //if (!enabled) return
        val screen = Vanilla.screen() as? ContainerScreen<*> ?: return
        //    rClearDepth() // use translate or zOffset
        val localSlotLocations: MutableMap<Int, Point> = mutableMapOf()
        localSlotLocations.putAll(slotLocations)
        if (localSlotLocations.isNotEmpty()) {
            if (ModSettings.HIGHLIGHT_FOUSED_ITEMS_ANIMATED.booleanValue) {
                tick++
                if (tick >= 1) {
                    tick = 0
                    alphaChannel += step
                    step = if (alphaChannel > defaultAlpha) -10 else if (alphaChannel < 10) 10 else step
                    //Log.trace("alphaChannel = $alphaChannel")
                }
            } else {
                alphaChannel = defaultAlpha
            }
            rDisableDepth()
            RenderSystem.enableBlend()
            val topLeft = screen.`(containerBounds)`.topLeft
            for ((_, slotTopLeft) in localSlotLocations) {
                val tl = topLeft + slotTopLeft
                rFillRect(Rectangle(tl.x,
                                    tl.y,
                                    16,
                                    16),
                          alphaChannel.r(1).g(0x96).b(0xb))
            }
            RenderSystem.disableBlend()
            rEnableDepth()
        } else {
            tick = 0
            alphaChannel = 10
            step = 10
        }
    }

    fun onTickInGame() {
        if (ModSettings.HIGHLIGHT_FOUSED_ITEMS.booleanValue) {
            val screen = Vanilla.screen() as? ContainerScreen<*> ?: return
            screen.`(focusedSlot)`?.let { slot ->
                if (toHighlight != slot.`(itemStack)`.itemType) {
                    ticksSinceLastFocusChange--
                    if (ticksSinceLastFocusChange < 0) {
                        toHighlight = slot.`(itemStack)`.itemType
                        ticksSinceLastFocusChange = ModSettings.HIGHLIGHT_FOUSED_WAIT_TICKS.integerValue
                    }
                } else {
                    ticksSinceLastFocusChange = ModSettings.HIGHLIGHT_FOUSED_WAIT_TICKS.integerValue
                }
                return
            }
            toHighlight = ItemType.EMPTY
        }
    }
}
