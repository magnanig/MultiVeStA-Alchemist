/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.components

import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import it.unibo.alchemist.common.utility.Action.PLAY
import it.unibo.alchemist.common.utility.Action.PAUSE
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import react.FC
import react.Props
import it.unibo.alchemist.client.adapters.reactBootstrap.buttons.Button
import it.unibo.alchemist.client.api.SimulationApi.pauseSimulation
import it.unibo.alchemist.client.api.SimulationApi.playSimulation
import react.useState
import it.unibo.alchemist.client.state.ClientStore.store
import it.unibo.alchemist.client.state.actions.SetPlayButton
import it.unibo.alchemist.common.model.surrogate.StatusSurrogate

private val scope = MainScope()

/**
 * Props for the [it.unibo.alchemist.client.components.PlayButtonKt.getPlayButton] component.
 */
external interface PlayButtonProps : Props {
    /**
     * Status of the simulation.
     */
    var status: StatusSurrogate
}

/**
 * Play Button component. Used to start and pause the simulation.
 */
val PlayButton: FC<PlayButtonProps> = FC { props ->

    var showWarningModal: Boolean by useState(false)
    var warningModalTitle: String by useState { "" }
    var warningModalMessage: String by useState { "" }

    val isSimulationRunning: (StatusSurrogate) -> Boolean = { status ->
        when (status) {
            StatusSurrogate.RUNNING -> true
            else -> false
        }
    }

    Button {
        disabled = when (props.status) {
            StatusSurrogate.INIT -> true
            StatusSurrogate.TERMINATED -> true
            else -> false
        }
        variant = if (isSimulationRunning(props.status)) "danger" else "success"
        onClick = {
            scope.launch {
                val response = if (isSimulationRunning(props.status)) pauseSimulation() else playSimulation()
                if (response.status == HttpStatusCode.OK) {
                    store.dispatch(
                        SetPlayButton(if (isSimulationRunning(props.status)) PLAY else PAUSE)
                    )
                } else {
                    warningModalTitle = "Error ${response.status}"
                    warningModalMessage = response.body() ?: "Unknown error"
                    showWarningModal = true
                }
            }
        }
        +if (isSimulationRunning(props.status)) "Pause" else "Play"
    }

    WarningModal {
        show = showWarningModal
        onHide = { showWarningModal = false }
        title = warningModalTitle
        message = warningModalMessage
    }
}
