package com.template.contracts

import com.r3.corda.lib.tokens.contracts.EvolvableTokenContract
import com.template.states.FrameTokenState
import com.template.states.SwitchTokenState
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

class FrameContract : EvolvableTokenContract(), Contract {
    override fun additionalCreateChecks(tx: LedgerTransaction) {
        val newToken = tx.outputStates.single() as FrameTokenState
        newToken.apply {
            require(serialNum != "") {"serialNum cannot be empty"}
        }
    }

    override fun additionalUpdateChecks(tx: LedgerTransaction) {
        /*This additional check does not apply to this use case.
         *This sample does not allow token update */
    }

    companion object {
        const val CONTRACT_ID = "com.template.contracts.FrameContract"
    }
}