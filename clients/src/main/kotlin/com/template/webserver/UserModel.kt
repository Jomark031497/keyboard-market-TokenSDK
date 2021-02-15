package com.template.webserver

import com.template.states.utils.GenderEnums
import com.template.states.utils.StatusEnums
import com.template.states.utils.UserTypeEnums
import net.corda.core.contracts.UniqueIdentifier

data class UserModel(
    val linearId: UniqueIdentifier,
    val userType: UserTypeEnums,
    val name: String,
    val age: Int,
    val gender: GenderEnums,
    val status: StatusEnums
)

fun toUserModel(
    linearId: UniqueIdentifier,
    userType: UserTypeEnums,
    name: String,
    age: Int,
    gender: GenderEnums,
    status: StatusEnums
): UserModel {

    return UserModel(
        linearId = linearId,
        userType = userType,
        name = name,
        age = age,
        gender = gender,
        status = status
    )
}


