package com.kevin.secretsanta.util

import com.kevin.secretsanta.data.ExclusionEntity
import com.kevin.secretsanta.data.ParticipantEntity

object DerangementUtil {

    /**
     * Returns (giverId, receiverId) pairs such that:
     *  - No one is assigned to themselves
     *  - No assignment violates a two-way or one-way exclusion
     *  - No assignment repeats a previous year's pairing (via previousYearForbidden)
     *  - Everyone gives exactly once and receives exactly once
     */
    fun generate(
        participants: List<ParticipantEntity>,
        exclusions: List<ExclusionEntity> = emptyList(),
        previousYearForbidden: Set<Pair<Long, Long>> = emptySet()
    ): List<Pair<Long, Long>> {
        require(participants.size >= 2) { "Need at least 2 participants" }

        val forbidden = buildSet<Pair<Long, Long>> {
            // Explicit exclusions
            exclusions.forEach { e ->
                add(Pair(e.participantIdA, e.participantIdB))
                if (e.twoWay) add(Pair(e.participantIdB, e.participantIdA))
            }
            // Previous year's assignments (one-way: don't repeat the same pairing)
            addAll(previousYearForbidden)
        }

        val ids = participants.map { it.id }

        repeat(10_000) {
            val receivers = ids.shuffled()
            val pairs = ids.zip(receivers)
            val valid = pairs.none { (giver, receiver) ->
                giver == receiver || forbidden.contains(Pair(giver, receiver))
            }
            if (valid) return pairs
        }

        error(
            "Could not find a valid assignment after 10,000 attempts. " +
            "Your exclusions (or previous-year rules) may make it impossible to assign everyone. " +
            "Try removing some exclusions and regenerating."
        )
    }
}
