package com.muhaimen.arenax.dataClasses

class esportsNotificationData(
    var notificationId:String,
    var userId: String,
    var content: String,
    var organizationName: String,
    var eventId:String
) {
    constructor() : this("","", "", "","")
}