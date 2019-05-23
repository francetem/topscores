package org.gmd.model

import io.swagger.annotations.ApiModelProperty


class Party() {
    lateinit var team: Team
    lateinit var members: List<TeamMember>
    lateinit var metrics: List<Metric>

    @ApiModelProperty(notes = "The score of the team for this game. The higher the better. Zero means no scoring.")
    var score: Int = 1

    constructor(
            team: Team,
            members: List<TeamMember>,
            metrics: List<Metric>,
            score: Int
            ): this() {
        this.team = team
        this.members = members
        this.metrics = metrics
        this.score = score
    }
}
