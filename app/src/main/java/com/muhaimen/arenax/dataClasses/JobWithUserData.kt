package com.muhaimen.arenax.dataClasses

data class JobWithUserDetails(
    val job: Job,
    val user: UserData?
)