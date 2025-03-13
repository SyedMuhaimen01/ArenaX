package com.muhaimen.arenax.dataClasses

import android.os.Parcel
import android.os.Parcelable

data class OrganizationData(
    var organizationId: String = "",
    var organizationName: String = "",
    var organizationLogo: String? = null,
    var organizationDescription: String? = null,
    var organizationLocation: String? = null,
    var organizationEmail: String? = null,
    var organizationPhone: String? = null,
    var organizationWebsite: String? = null,
    var organizationType: String? = null,
    var organizationIndustry: String? = null,
    var organizationSize: String? = null,
    var organizationTagline: String? = null,
    var organizationOwner: String? = null,
    var organizationMembers: List<String>? = emptyList(),
    var organizationAdmins: List<String>? = emptyList(),
) : Parcelable {

    constructor(parcel: Parcel) : this(
        organizationId = parcel.readString() ?: "",
        organizationName = parcel.readString() ?: "",
        organizationLogo = parcel.readString(),
        organizationDescription = parcel.readString(),
        organizationLocation = parcel.readString(),
        organizationEmail = parcel.readString(),
        organizationPhone = parcel.readString(),
        organizationWebsite = parcel.readString(),
        organizationType = parcel.readString(),
        organizationIndustry = parcel.readString(),
        organizationSize = parcel.readString(),
        organizationTagline = parcel.readString(),
        organizationOwner = parcel.readString(),
        organizationMembers = parcel.createStringArrayList(),
        organizationAdmins = parcel.createStringArrayList(),

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(organizationId)
        parcel.writeString(organizationName)
        parcel.writeString(organizationLogo)
        parcel.writeString(organizationDescription)
        parcel.writeString(organizationLocation)
        parcel.writeString(organizationEmail)
        parcel.writeString(organizationPhone)
        parcel.writeString(organizationWebsite)
        parcel.writeString(organizationType)
        parcel.writeString(organizationIndustry)
        parcel.writeString(organizationSize)
        parcel.writeString(organizationTagline)
        parcel.writeString(organizationOwner)
        parcel.writeStringList(organizationMembers)
        parcel.writeStringList(organizationAdmins)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrganizationData> {
        override fun createFromParcel(parcel: Parcel): OrganizationData {
            return OrganizationData(parcel)
        }

        override fun newArray(size: Int): Array<OrganizationData?> {
            return arrayOfNulls(size)
        }
    }
}
