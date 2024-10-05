package com.muhaimen.arenax.dataClasses
data class gamesData(
    val id: Int,
    val gameName: String,
    val genre: List<String> = emptyList(),
    val iconUrl: String,
    val publisher: String,
    val releaseDate: String
) {
    companion object {
        fun fromApiResponse(apiResponse: ApiResponse): gamesData {
            return gamesData(
                id = apiResponse.id,
                gameName = apiResponse.name,
                genre = parseGenres(apiResponse.genres),
                iconUrl = formatLogoUrl(apiResponse.logo_url), // Ensure valid URL
                publisher = apiResponse.publisher ?: "Unknown",
                releaseDate = apiResponse.release_date ?: "Unknown"
            )
        }

        private fun parseGenres(genres: String): List<String> {
            return genres.split(",").map { it.trim() }
        }

        // Function to format the logo URL
        private fun formatLogoUrl(logoUrl: String?): String {
            return if (logoUrl != null && logoUrl.startsWith("//")) {
                "https:$logoUrl" // Prepend https
            } else {
                logoUrl ?: "https://via.placeholder.com/150" // Fallback placeholder URL
            }
        }
    }
}

data class ApiResponse(
    val id: Int,
    val name: String,
    val logo_url: String?,
    val publisher: String?,
    val genres: String,
    val platforms: String?,
    val release_date: String?
)