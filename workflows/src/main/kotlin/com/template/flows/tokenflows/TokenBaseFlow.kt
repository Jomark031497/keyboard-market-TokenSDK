package com.template.flows.tokenflows

import com.template.states.FrameTokenState
import com.template.states.SwitchTokenState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FlowLogic
import net.corda.core.node.services.queryBy

abstract class TokenBaseFlow : FlowLogic<String>() {

    // get frame states on ledger
    fun getFrameStates(serial: String): StateAndRef<FrameTokenState> {
        return serviceHub.vaultService.queryBy<FrameTokenState>().states
            .filter { it.state.data.serialNum == serial }[0]
    }

    // get switch states on ledger
    fun getSwitchStates(serial: String): StateAndRef<SwitchTokenState> {
        return serviceHub.vaultService.queryBy<SwitchTokenState>().states
            .filter { it.state.data.serialNum == serial }[0]
    }

}