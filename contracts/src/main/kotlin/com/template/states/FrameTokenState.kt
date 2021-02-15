package com.template.states

import com.r3.corda.lib.tokens.contracts.states.EvolvableTokenType
import com.template.contracts.FrameContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party

@BelongsToContract(FrameContract::class)
data class FrameTokenState(
    val maintainer: Party,
    override val linearId: UniqueIdentifier,
    override val fractionDigits: Int,
    val serialNum: String,
    override val maintainers: List<Party> = listOf(maintainer)
) : EvolvableTokenType()
