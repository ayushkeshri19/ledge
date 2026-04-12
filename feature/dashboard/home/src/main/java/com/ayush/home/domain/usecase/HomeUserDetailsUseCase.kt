package com.ayush.home.domain.usecase

import com.ayush.home.domain.models.HomeUserDetails
import com.ayush.home.domain.repository.HomeRepository
import java.util.Calendar
import javax.inject.Inject

class HomeUserDetailsUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(): HomeUserDetails? {
        val user = homeRepository.getCurrentUser() ?: return null
        val initials = user.fullName
            .trim()
            .split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
        return HomeUserDetails(
            name = user.fullName,
            initials = initials,
            greeting = buildGreeting(),
        )
    }

    private fun buildGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}
