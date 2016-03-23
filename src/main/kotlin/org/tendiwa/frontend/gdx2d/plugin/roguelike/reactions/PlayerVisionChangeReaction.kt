package org.tendiwa.frontend.gdx2d.plugin.roguelike.reactions

import org.tendiwa.backend.existence.aspect
import org.tendiwa.backend.modules.roguelike.aspects.PlayerVision
import org.tendiwa.backend.space.aspects.Position
import org.tendiwa.backend.space.realThing.realThings
import org.tendiwa.backend.space.realThing.viewOfArea
import org.tendiwa.backend.space.walls.hasWallAt
import org.tendiwa.backend.space.walls.walls
import org.tendiwa.frontend.gdx2d.GameReaction
import org.tendiwa.frontend.gdx2d.TendiwaGame
import org.tendiwa.frontend.gdx2d.plugin.roguelike.updateFieldOfView

internal class PlayerVisionChangeReaction(
    game: TendiwaGame
) : GameReaction<PlayerVision.Change>(game) {

    override fun invoke(stimulus: PlayerVision.Change, done: () -> Unit) {
        game.vicinity.updateFieldOfView(stimulus.new)
        stimulus.old
            .difference(stimulus.new)
            .let { difference ->
                showSeenWalls(difference)
                showSeenThings(difference)
                destroyUnseenWalls(difference)
                destroyUnseenThings(difference)
            }
        done()
    }

    private fun showSeenWalls(difference: PlayerVision.VisionDifference) {
        difference.seen
            .filter { game.reality.space.walls.hasWallAt(it) }
            .forEach { game.gridActorRegistry.spawnWall(it) }
    }

    private fun destroyUnseenWalls(difference: PlayerVision.VisionDifference) {
        difference.unseen
            .filter { game.reality.space.walls.hasWallAt(it) }
            .forEach { game.gridActorRegistry.removeWallActor(it) }
    }

    private fun showSeenThings(difference: PlayerVision.VisionDifference) {
        game.apply {
            reality.space.realThings
                .viewOfArea(vicinity.tileBounds)
                .things
                .filter {
                    val tile = it.aspect<Position>().tile
                    difference.seen.any { it == tile }
                }
                .forEach { gridActorRegistry.spawnRealThing(it) }
        }
    }

    private fun destroyUnseenThings(difference: PlayerVision.VisionDifference) {
        game.reality.space.realThings
            .viewOfArea(game.vicinity.tileBounds)
            .things
            .filter { thing ->
                difference.unseen.any { it == thing.aspect<Position>().tile }
            }
            .forEach { game.gridActorRegistry.removeActor(it.aspect<Position>().tile, it) }
    }

}
